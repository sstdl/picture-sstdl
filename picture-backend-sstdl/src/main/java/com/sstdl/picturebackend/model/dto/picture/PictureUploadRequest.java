package com.sstdl.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;  
  
    /**  
     * 文件地址  
     */  
    private String fileUrl;

    /**
     * 文件名
     */
    private String pictureName;
  
    private static final long serialVersionUID = 1L;  
}
