package com.sstdl.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author SSTDL
 * @description
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("REVIEWING", 0),
    PASS("PASS", 1),
    REJECT("REJECT", 2);

    private final String text;
    private final Integer value;

    PictureReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    // 根据value获取枚举
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum statusEnum : PictureReviewStatusEnum.values()) {
            if (statusEnum.getValue().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
