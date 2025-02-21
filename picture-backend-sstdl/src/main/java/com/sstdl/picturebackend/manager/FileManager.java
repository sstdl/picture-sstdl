package com.sstdl.picturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.sstdl.picturebackend.common.ErrorCode;
import com.sstdl.picturebackend.config.CosClientConfig;
import com.sstdl.picturebackend.exception.BusinessException;
import com.sstdl.picturebackend.model.dto.file.UploadPictureResult;
import com.sstdl.picturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author SSTDL
 * @description 图片文件上传
 */
@Deprecated
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

    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadFilePrefix) {
        // 校验图片
        validPicture(fileUrl);
        // 构建图片上传地址
        String uuid = RandomUtil.randomString(16);
//        String originalFilename = multipartFile.getOriginalFilename();
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("/%s/%s", uploadFilePrefix, uploadFileName);
        // 上传图片
        File tempFile = null;
        try {
            // 临时文件
            tempFile = File.createTempFile(uploadFilePath, null);
//            multipartFile.transferTo(tempFile);
            HttpUtil.downloadFile(fileUrl, tempFile);
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
     * 校验图片
     *
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StringUtils.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        try {
            // 1. 验证 URL 格式
            new URL(fileUrl); // 验证是否是合法的 URL
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 验证 URL 是否有效
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            final List<String> suffixList = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg");
            ThrowUtils.throwIf(!suffixList.contains(contentType), ErrorCode.PARAMS_ERROR, "上传文件格式不正确");
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                long contentLength = Long.parseLong(contentLengthStr);
                final long maxFileSize = 2 * 1024 * 1024L; // 2MB
                ThrowUtils.throwIf(contentLength > maxFileSize, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过2MB");
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
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
