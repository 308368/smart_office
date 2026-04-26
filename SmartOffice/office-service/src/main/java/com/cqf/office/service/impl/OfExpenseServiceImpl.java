package com.cqf.office.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.common.result.Result;
import com.cqf.common.utils.SnowflakeIdGenerator;
import com.cqf.office.enums.ExpenseStatusEnum;
import com.cqf.office.model.dto.AddExpenseDTO;
import com.cqf.office.model.dto.ExpenseQueryParam;
import com.cqf.office.model.po.OfExpense;
import com.cqf.office.model.po.OfExpenseItem;
import com.cqf.office.model.vo.ExpenseDetailVo;
import com.cqf.office.model.vo.ExpenseVo;
import com.cqf.office.mapper.OfExpenseMapper;
import com.cqf.office.mapper.OfExpenseItemMapper;
import com.cqf.office.service.IOfExpenseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 报销申请表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfExpenseServiceImpl extends ServiceImpl<OfExpenseMapper, OfExpense> implements IOfExpenseService {
    private final OfExpenseMapper expenseMapper;
    private final OfExpenseItemMapper expenseItemMapper;
    private final MinioClient minioClient;
    private final AuthClient authClient;
    @Value("${minio.bucket.files}")
    private String bucketFiles;
    @Resource
    @Lazy
    private IOfExpenseService currentProxy;

    @Override
    @Transactional
    public ExpenseDetailVo createExpense(AddExpenseDTO dto, MultipartFile[] files) {
        ArrayList<String> filesUrl = new ArrayList<>();
        if (files != null && files.length > 0){
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                //获取文件绝对路径
                String absolutePath = getAbsolutePath(file);
                String fileName = file.getOriginalFilename();
                //获取文件扩展名
                String extension = fileName.substring(fileName.lastIndexOf("."));
                //获取文件类型
                String contentType = file.getContentType();
                //获取文件目标地址
                String objectName = "expense/" + getDefaultFolderPath() + SnowflakeIdGenerator.generateIdStr() + extension;
                addMediaFilesToMinIO(absolutePath, objectName, contentType, bucketFiles);
                //获取访问地址
                String fileUrl = getFileUrl(bucketFiles, objectName);
                filesUrl.add(fileUrl);
            }
        }
        //添加报销单
        OfExpense ofExpense = currentProxy.addExpenseToDb(dto);
        //添加报销单明细
        List<OfExpenseItem> ofExpenseItems = currentProxy.addExpenseItemToDb(dto, ofExpense, filesUrl);
        //封装返回数据
        ExpenseDetailVo expenseDetailVo = new ExpenseDetailVo();
        BeanUtil.copyProperties(ofExpense, expenseDetailVo);
        List<ExpenseDetailVo.ExpenseItemVo> expenseItemVos = BeanUtil.copyToList(ofExpenseItems, ExpenseDetailVo.ExpenseItemVo.class);
        expenseDetailVo.setItems(expenseItemVos);
        expenseDetailVo.setStatusText(ofExpense.getStatus().getLabel());
        expenseDetailVo.setStatus(ofExpense.getStatus().getCode());
        return expenseDetailVo;
    }

    @Override
    public List<OfExpenseItem> addExpenseItemToDb(@NotNull AddExpenseDTO dto, OfExpense ofExpense, ArrayList<String> filesUrl) {
        Long expenseId = ofExpense.getId();
        List<AddExpenseDTO.ExpenseItemDTO> items = dto.getItems();
        List<OfExpenseItem> list = items.stream().map(item -> {
            OfExpenseItem ofExpenseItem = new OfExpenseItem();
            BeanUtil.copyProperties(item, ofExpenseItem);
            ofExpenseItem.setExpenseId(expenseId);
            if (item.getFileIndex() != null && item.getFileIndex()!= -1) {
                ofExpenseItem.setReceiptUrl(filesUrl.get(item.getFileIndex()));
            }
            return ofExpenseItem;
        }).toList();
        boolean saveBatch = expenseItemMapper.savebatch(list);
        if (!saveBatch) {
            throw new RuntimeException("保存报销单明细失败");
        }
        return list;
    }

    @Override
    public OfExpense addExpenseToDb(AddExpenseDTO dto) {
        OfExpense ofExpense = new OfExpense();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(userName);
        Long userId = user.getId();
        String nickname = user.getNickname();
        Result<List<DeptVo>> deptList = authClient.list();
        deptList.getData().forEach(dept -> {
            if (dept.getId().equals(user.getDeptId())) {
                ofExpense.setDeptId(dept.getId());
                ofExpense.setDeptName(dept.getName());
            }
        });
        String expenseNo = "EX" + SnowflakeIdGenerator.generateIdStr();
        ofExpense.setUserId(userId);
        ofExpense.setUserName(nickname);
        ofExpense.setExpenseNo(expenseNo);
        ofExpense.setExpenseType(dto.getExpenseType());
        ofExpense.setTotalAmount(dto.getTotalAmount());
        ofExpense.setDescription(dto.getDescription());
        ofExpense.setStatus(ExpenseStatusEnum.PENDING);
        int insert = expenseMapper.insert(ofExpense);
        if (insert <= 0) {
            throw new RuntimeException("创建报销单失败");
        }
        return ofExpense;
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
        String absolutePath;
        try {
            File tempFile = File.createTempFile("expense", ".temp");
            file.transferTo(tempFile);
            absolutePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    @Override
    public ExpenseDetailVo getExpenseDetail(Long id) {
        OfExpense expense = this.getById(id);
        if (expense == null) {
            throw new RuntimeException("报销记录不存在");
        }

        ExpenseDetailVo vo = new ExpenseDetailVo();
        BeanUtil.copyProperties(expense, vo);
        vo.setStatus(expense.getStatus().getCode());
        vo.setStatusText(expense.getStatus().getLabel());

        // 查询费用明细
        List<OfExpenseItem> items = expenseItemMapper.selectList(
                new LambdaQueryWrapper<OfExpenseItem>().eq(OfExpenseItem::getExpenseId, id)
        );
        List<ExpenseDetailVo.ExpenseItemVo> itemVos = items.stream().map(item -> {
            ExpenseDetailVo.ExpenseItemVo itemVo = new ExpenseDetailVo.ExpenseItemVo();
            BeanUtil.copyProperties(item, itemVo);
            return itemVo;
        }).toList();
        vo.setItems(itemVos);

        return vo;
    }

    @Override
    public void cancel(Long id) {
        OfExpense expense = this.getById(id);
        if (expense == null) {
            throw new RuntimeException("报销记录不存在");
        }
        if (expense.getStatus() != ExpenseStatusEnum.PENDING) {
            throw new RuntimeException("只有待审批状态可以取消");
        }
        expense.setStatus(ExpenseStatusEnum.CANCELED);
        this.updateById(expense);
        List<OfExpenseItem> ofExpenseItems = expenseItemMapper.selectList(new LambdaQueryWrapper<OfExpenseItem>().eq(OfExpenseItem::getExpenseId, id));
        for (OfExpenseItem ofExpenseItem : ofExpenseItems) {
            removeFileMinIO(ofExpenseItem.getReceiptUrl());
        }
    }

    @Override
    public PageResult<ExpenseVo> getPendingList(ExpenseQueryParam param) {
        Page<OfExpense> page = this.lambdaQuery()
                .eq(OfExpense::getStatus, ExpenseStatusEnum.PENDING)
                .orderByDesc(OfExpense::getCreateTime)
                .page(new Page<>(param.getCurrent(), param.getSize()));

        PageResult<ExpenseVo> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords().stream().map(expense -> {
            ExpenseVo vo = new ExpenseVo();
            BeanUtil.copyProperties(expense, vo);
            vo.setStatus(expense.getStatus().getCode());
            vo.setStatusText(expense.getStatus().getLabel());
            return vo;
        }).toList());
        pageResult.setTotal((int) page.getTotal());
        pageResult.setPages((int) page.getPages());
        pageResult.setSize((int) page.getSize());
        pageResult.setCurrent((int) page.getCurrent());
        return pageResult;
    }

    @Override
    @Transactional
    public void approve(Long id, Boolean approved, String comment) {
        OfExpense expense = this.getById(id);
        if (expense == null) {
            throw new RuntimeException("报销记录不存在");
        }
        if (expense.getStatus() != ExpenseStatusEnum.PENDING) {
            throw new RuntimeException("只有待审批状态可以审批");
        }

        expense.setStatus(approved ? ExpenseStatusEnum.APPROVED : ExpenseStatusEnum.REJECTED);
        expense.setApproveTime(LocalDateTime.now());
        expense.setApproveComment(comment);
        this.updateById(expense);
    }
    private void removeFileMinIO(String fileUrl) {
        String objectName = fileUrl.substring(13);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketFiles).object(objectName).build());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("minio文件删除失败,{}", e.getMessage());
            throw new RuntimeException("minio文件删除失败");
        }
    }
}
