package com.sstdl.picturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.sstdl.picturebackend.common.ErrorCode;
import com.sstdl.picturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author SSTDL
 * @description
 */
@Service
@Slf4j
public class FilePictureUpload extends PictureUploadTemplate{

    @Override
    protected String validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        // 文件大小限制
        long fileSize = multipartFile.getSize();
        final long MAX_FILE_SIZE = 2 * 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过2M");
        // 校验后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        List<String> suffixList = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        ThrowUtils.throwIf(!suffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "上传文件格式不正确");
        return suffix;
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void handlePicture(Object inputSource, File file) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
