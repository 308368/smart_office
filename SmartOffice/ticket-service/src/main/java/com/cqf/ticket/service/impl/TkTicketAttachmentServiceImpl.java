package com.cqf.ticket.service.impl;

import com.cqf.api.client.AuthClient;
import com.cqf.common.utils.SnowflakeIdGenerator;
import com.cqf.ticket.model.po.TkTicketAttachment;
import com.cqf.ticket.mapper.TkTicketAttachmentMapper;
import com.cqf.ticket.service.ITkTicketAttachmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 工单附件表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TkTicketAttachmentServiceImpl extends ServiceImpl<TkTicketAttachmentMapper, TkTicketAttachment> implements ITkTicketAttachmentService {
    private final MinioClient minioClient;
    private final TkTicketAttachmentMapper tkTicketAttachmentMapper;
    private final AuthClient authClient;
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Override
    public String upload(Long ticketId, MultipartFile file) {
        //获取文件绝对路径
        String absolutePath = getAbsolutePath(file);
        String fileName = file.getOriginalFilename();
        //获取文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //获取文件目标地址
        String objectName = "ticket/" + getDefaultFolderPath() + SnowflakeIdGenerator.generateIdStr() + extension;
        //获取文件类型
        String contentType = file.getContentType();
        //上传到minio
        addMediaFilesToMinIO(absolutePath, objectName, contentType, bucketFiles);
        //获取访问地址
        String fileUrl = getFileUrl(bucketFiles, objectName);
        //获取文件大小
        long fileSize = file.getSize();
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        //添加文件信息到数据库
        addMediaFilesToDb(ticketId, name, fileUrl, fileSize, contentType);
        return fileUrl;
    }

    private void addMediaFilesToDb(Long ticketId, String name, String fileUrl, long fileSize, String contentType) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        TkTicketAttachment tkTicketAttachment = new TkTicketAttachment()
                .setTicketId(ticketId)
                .setFileName(name)
                .setFileUrl(fileUrl)
                .setFileSize(fileSize)
                .setFileType(contentType)
                .setUploadBy(userId);
        int insert = tkTicketAttachmentMapper.insert(tkTicketAttachment);
        if (insert <= 0) {
            log.info("添加文件信息失败");
            throw new RuntimeException("添加文件信息失败");
        }
    }

    private String getFileUrl(String bucketFiles, String objectName) {
        return "/" + bucketFiles + "/" + objectName;
    }


    /**
     * 根据当前日期获取文件路径
     *
     * @return
     */
    private static String getDefaultFolderPath() {
        //创建文件路径
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(new Date()) + "/";

    }

    @NotNull
    private static String getAbsolutePath(MultipartFile file) {
        String absolutePath = null;
        try {
            File tempFile = null;
            tempFile = File.createTempFile("ticketMinio", ".temp");
            file.transferTo(tempFile);
            absolutePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("创建临时文件失败");
        }
        return absolutePath;
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
}
