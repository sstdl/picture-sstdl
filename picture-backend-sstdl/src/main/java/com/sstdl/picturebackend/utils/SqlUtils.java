package com.sstdl.picturebackend.utils;

import cn.hutool.core.util.StrUtil;

/**
 * @author SSTDL
 * @description SQL 校验工具
 */
public class SqlUtils {
    /**
     * 验证排序字段是否合法
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StrUtil.isBlank(sortField)) {
            return false;
        }
        return !StrUtil.containsAny(sortField, "=", "(", ")", " ");
    }
}
