package com.cqf.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cqf.api.client.AuthClient;
import com.cqf.common.utils.SnowflakeIdGenerator;
import com.cqf.knowledge.mapper.KbKnowledgeBaseMapper;
import com.cqf.knowledge.model.po.KbDocument;
import com.cqf.knowledge.mapper.KbDocumentMapper;
import com.cqf.knowledge.model.po.KbKnowledgeBase;
import com.cqf.knowledge.model.vo.DocumentVo;
import com.cqf.knowledge.service.IKbDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 知识库文档表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KbDocumentServiceImpl extends ServiceImpl<KbDocumentMapper, KbDocument> implements IKbDocumentService {
    private final KbDocumentMapper kbDocumentMapper;
    private final KbKnowledgeBaseMapper kbKnowledgeBaseMapper;
    private final MinioClient minioClient;
    private final AuthClient authClient;
    @Resource
    @Lazy
    private IKbDocumentService currentProxy;
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Override
    @GlobalTransactional
    public DocumentVo upload(Long kbId, MultipartFile file) {
        //获取文件名称
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //获取默认文件夹路径
        String objectName = getDefaultFolderPath() +kbId+"/"+ SnowflakeIdGenerator.generateIdStr() + extension;
        //创建临时文件
        File tempFile = createTempFile(file);
        String absolutePath = tempFile.getAbsolutePath();
        //获取文件内容类型
        String contentType = getContentType(extension);
        //添加文件到minio
        addMediaFilesToMinIO(absolutePath, objectName, contentType, bucketFiles);
        tempFile.delete();
        //保存到数据库
        return currentProxy.addMediaFilesToDb(
                kbId,file.getSize(),
                fileName.substring(0, fileName.lastIndexOf(".")),
                fileName.substring(fileName.lastIndexOf(".")+1),
                objectName,
                bucketFiles
        );
    }
    @Override
    @Transactional
    public DocumentVo addMediaFilesToDb(Long kbId,Long fileSize,String fileName,String extension,String objectName,String bucket) {
        KbKnowledgeBase kbKnowledgeBase = kbKnowledgeBaseMapper.selectById(kbId);
        if (kbKnowledgeBase == null)throw new RuntimeException("知识库不存在");
        KbDocument kbDocument = new KbDocument();
        kbDocument.setKbId(kbId);
        kbDocument.setTitle(fileName);
        kbDocument.setFileType( extension);
        kbDocument.setFileSize(fileSize);
        kbDocument.setChunkCount(0);
        kbDocument.setStatus(0);
        Long userId = authClient.getUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        kbDocument.setCreateBy(userId);
        kbDocument.setFileUrl("/" + bucket + "/" + objectName);
        int insert = kbDocumentMapper.insert(kbDocument);
        if (insert < 0) {
            log.error("保存文件信息到数据库失败,{}", kbDocument.toString());
            throw new RuntimeException("保存文件信息到数据库失败");
        }
        log.info("保存文件信息到数据库成功,{}", kbDocument.toString());
        kbKnowledgeBase.setDocCount(kbKnowledgeBase.getDocCount() + 1);
        int i = kbKnowledgeBaseMapper.updateById(kbKnowledgeBase);
        if (i < 0) {
            log.error("更新知识库文档数量失败,{}", kbKnowledgeBase.toString());
            throw new RuntimeException("更新知识库文档数量失败");
        }
        log.info("更新知识库文档数量成功,{}", kbKnowledgeBase.toString());
        return BeanUtil.copyProperties(kbDocument, DocumentVo.class);
    }

    private Boolean addMediaFilesToMinIO(String absolutePath, String objectName, String contentType, String bucketFiles) {
        try {
            UploadObjectArgs objectArgs = UploadObjectArgs.builder()
                    .contentType(contentType)
                    .filename(absolutePath)
                    .bucket(bucketFiles)
                    .object(objectName).build();
            minioClient.uploadObject(objectArgs);
            log.info("上传文件到文件系统成功,bucket:{},objectName:{}", bucketFiles, objectName);
            System.out.println("上传文件到文件系统成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucketFiles, objectName, e.getMessage(), e);
            throw new RuntimeException("文件上传到minIO失败");

        }

    }

    /**
     * 根据文件扩展名获取ContentType（MIME类型）
     * 使用Apache Tika自动识别文件类型
     * @param extension 文件扩展名，如 .pdf, .docx
     * @return ContentType值
     */
    private String getContentType(String extension) {
        // 参数为空时返回默认二进制类型
        if (extension == null || extension.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        // 创建Tika实例，用于识别文件MIME类型
        Tika tika = new Tika();
        try {
            // Tika根据扩展名自动识别MIME类型
            // 传入"test" + extension构造虚拟文件名让Tika识别类型
            String contentType = tika.detect("test" + extension);
            return contentType;
        } catch (Exception e) {
            // 识别失败返回默认二进制类型
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    @NotNull
    private static File createTempFile(MultipartFile file)  {
        //创建临时文件夹
        File tempFile = null;
        try {
            tempFile = File.createTempFile("minio", ".temp");
            file.transferTo(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("创建临时文件失败");
        }
        return tempFile;
    }

    /**
     * 根据当前日期获取文件路径
     * @return
     */
    private static String getDefaultFolderPath() {
        //创建文件路径
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(new Date()) + "/";

    }
}
