package com.sstdl.picturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.sstdl.picturebackend.common.ErrorCode;
import com.sstdl.picturebackend.config.CosClientConfig;
import com.sstdl.picturebackend.model.dto.file.UploadPictureResult;
import com.sstdl.picturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author SSTDL
 * @description 图片文件上传
 */
@Service
@Slf4j
public class FileManager {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param uploadFilePrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadFilePrefix) {
        // 校验图片
        validPicture(multipartFile);
        // 构建图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("/%s/%s", uploadFilePrefix, uploadFileName);
        // 上传图片
        File tempFile = null;
        try {
            // 临时文件
            tempFile = File.createTempFile(uploadFilePath, null);
            multipartFile.transferTo(tempFile);
            // 上传到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, tempFile);
            // 返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadFilePath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.info("上传图片失败");
            throw new RuntimeException("上传图片失败", e);
        } finally {
            this.deletePicture(tempFile);
        }

    }

    /**
     * 校验图片
     *
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        // 文件大小限制
        long fileSize = multipartFile.getSize();
        final long MAX_FILE_SIZE = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过2M");
        // 校验后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        List<String> suffixList = Arrays.asList("jpg", "jpeg", "png", "gif");
        ThrowUtils.throwIf(!suffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "上传文件格式不正确");
    }

    /**
     * 删除图片
     *
     * @param file
     */
    public void deletePicture(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        ThrowUtils.throwIf(!delete, ErrorCode.SYSTEM_ERROR, "删除文件失败");
        log.info("删除文件失败：{}", file.getAbsoluteFile());
    }
}
