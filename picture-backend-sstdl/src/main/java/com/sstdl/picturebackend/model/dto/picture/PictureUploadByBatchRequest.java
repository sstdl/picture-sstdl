package com.sstdl.picturebackend.model.dto.picture;

import lombok.Data;

/**
 * @author SSTDL
 * @description
 */
@Data
public class PictureUploadByBatchRequest {
    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 图片数量
     */
    private Integer count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix;
}
