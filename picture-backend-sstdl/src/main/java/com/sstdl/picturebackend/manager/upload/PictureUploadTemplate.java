package com.sstdl.picturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.sstdl.picturebackend.common.ErrorCode;
import com.sstdl.picturebackend.config.CosClientConfig;
import com.sstdl.picturebackend.manager.CosManager;
import com.sstdl.picturebackend.model.dto.file.UploadPictureResult;
import com.sstdl.picturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

/**
 * @author SSTDL
 * @description
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    public final UploadPictureResult uploadPicture(Object inputSource, String uploadFilePrefix) {
        // 校验图片 （使用缩略图获取不到后缀名时，使用校验返回的后缀名）
        String suffix = validPicture(inputSource);
        // 构建图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadFilePath = String.format("/%s/%s", uploadFilePrefix, uploadFileName);
        // 上传图片
        File tempFile = null;
        try {
            // 临时文件
            tempFile = File.createTempFile(uploadFilePath, null);
            // 处理图片
            handlePicture(inputSource, tempFile);
            // 上传到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, tempFile);
            // 返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            return returnResult(uploadFileName, tempFile, uploadFilePath, imageInfo);
        } catch (Exception e) {
            log.info("上传图片失败");
            throw new RuntimeException("上传图片失败", e);
        } finally {
            this.deletePicture(tempFile);
        }
    }

    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract String validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理图片
     */
    protected abstract void handlePicture(Object inputSource, File file);

    private UploadPictureResult  returnResult(String originalFilename, File file, String uploadFilePath, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadFilePath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    public void deletePicture(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        ThrowUtils.throwIf(!delete, ErrorCode.SYSTEM_ERROR, "删除文件失败");
    }
}
