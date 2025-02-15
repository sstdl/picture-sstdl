package com.sstdl.picturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author SSTDL
 * @description 删除请求
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 删除信息 id
     */
    private Long id;
}
