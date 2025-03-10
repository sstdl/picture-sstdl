package com.sstdl.picturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sstdl.picturebackend.annotation.AuthCheck;
import com.sstdl.picturebackend.common.BaseResponse;
import com.sstdl.picturebackend.common.DeleteRequest;
import com.sstdl.picturebackend.common.ErrorCode;
import com.sstdl.picturebackend.constant.UserConstant;
import com.sstdl.picturebackend.exception.BusinessException;
import com.sstdl.picturebackend.model.dto.picture.*;
import com.sstdl.picturebackend.model.entity.Picture;
import com.sstdl.picturebackend.model.entity.User;
import com.sstdl.picturebackend.model.enums.PictureReviewStatusEnum;
import com.sstdl.picturebackend.model.vo.PictureTagCategory;
import com.sstdl.picturebackend.model.vo.PictureVO;
import com.sstdl.picturebackend.service.PictureService;
import com.sstdl.picturebackend.service.UserService;
import com.sstdl.picturebackend.utils.ResultUtils;
import com.sstdl.picturebackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author SSTDL
 * @description
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

//    @Resource
//    private CosManager cosManager;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> caffeineCache = Caffeine.newBuilder().initialCapacity(1024)
            .maximumSize(1024 * 10)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * 上传图片
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) throws IOException {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 url 上传图片
     */
    @PostMapping("/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest request) throws IOException {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest byBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(byBatchRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        Integer res = pictureService.uploadPictureByBatch(byBatchRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 删除图片
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        // 用户只能看到已经过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取图片列表（封装类，通过缓存），多级缓存，Caffine + redis
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        // 用户只能看到已经过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 构建缓存
        String questionQueryStr = JSONUtil.toJsonStr(pictureQueryRequest);
        // 通过哈希转存为较短的缓存键
        String hashKey = DigestUtils.md5DigestAsHex(questionQueryStr.getBytes());
        String cacheKey = "sstdl_picture:listPageVOByPage:" + hashKey;

        // 1.从本地缓存查询
        String cacheValue = caffeineCache.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            Page<PictureVO> page = JSONUtil.toBean(cacheValue, Page.class);
            return ResultUtils.success(page);
        }

        // 2.本地缓存没有，从redis查询
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        cacheValue = ops.get(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            // 并存入本地缓存
            caffeineCache.put(cacheKey,cacheValue);
            Page<PictureVO> page = JSONUtil.toBean(cacheValue, Page.class);
            return ResultUtils.success(page);
        }

        // 3.若两个缓存中都没有查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        cacheValue = JSONUtil.toJsonStr(picturePage);

        // 查询后存入本地缓存
        caffeineCache.put(cacheKey,cacheValue);

        // 查询后存入 redis
        // 5-10 分钟随机缓存，防止雪崩
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        ops.set(cacheKey, cacheValue, expireTime, TimeUnit.SECONDS);

        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "壁纸", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }


//    /**
//     * 测试上传
//     *
//     * @param multipartFile
//     * @return
//     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    @PostMapping("/test/upload")
//    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
//        String fileName = multipartFile.getOriginalFilename();
//        String filePath = String.format("/test/%s", fileName);
//        File file = null;
//        try {
//            // 上传文件
//            file = File.createTempFile(filePath, null);
//            multipartFile.transferTo(file);
//            cosManager.putObject(filePath, file);
//            // 返回文件访问路径
//            return ResultUtils.success(filePath);
//        } catch (Exception e) {
//            log.error("文件上传失败，filePath " + filePath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
//        } finally {
//            if (file != null) {
//                // 删除临时文件
//                boolean delete = file.delete();
//                if (!delete) {
//                    log.error("临时文件删除失败，filePath = {}", filePath);
//                }
//            }
//        }
//    }
//
//    /**
//     * 测试下载
//     *
//     * @param filepath 文件路径
//     * @param response 响应对象
//     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    @GetMapping("/test/download/")
//    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
//        COSObjectInputStream cosObjectInput = null;
//        try {
//            COSObject cosObject = cosManager.getObject(filepath);
//            cosObjectInput = cosObject.getObjectContent();
//            // 处理下载到的流
//            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
//            // 设置响应头
//            response.setContentType("application/octet-stream;charset=UTF-8");
//            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
//            // 写入响应
//            response.getOutputStream().write(bytes);
//            response.getOutputStream().flush();
//        } catch (Exception e) {
//            log.error("file download error, filepath = " + filepath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
//        } finally {
//            if (cosObjectInput != null) {
//                cosObjectInput.close();
//            }
//        }
//    }
}
