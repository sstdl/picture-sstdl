package com.sstdl.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sstdl.picturebackend.model.dto.picture.PictureQueryRequest;
import com.sstdl.picturebackend.model.dto.picture.PictureReviewRequest;
import com.sstdl.picturebackend.model.dto.picture.PictureUploadRequest;
import com.sstdl.picturebackend.model.entity.Picture;
import com.sstdl.picturebackend.model.entity.User;
import com.sstdl.picturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author WSH
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-19 21:16:45
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 构建查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片VO
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片VO分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     */
    void fillReviewParams(Picture picture, User loginUser);
}
