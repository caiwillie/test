package com.brandnewdata.mop.poc.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.PageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PageEnhancedUtil extends PageUtil {

    public static <T> List<T> slice(int pageNo, int pageSize, List<T> list) {
       return slice(pageNo, pageSize, list, Function.identity());
    }

    public static <T, R> List<R> slice(int pageNo, int pageSize, List<T> list, Function<T, R> mapper) {
        int[] startEnd = transToStartEnd(pageNo, pageSize);
        if(CollUtil.isEmpty(list)) {
            return ListUtil.empty();
        }
        List<R> ret = new ArrayList<>();
        for (int i = startEnd[0]; i < startEnd[1] && i < list.size(); i++) {
            if(mapper != null) {
                ret.add(mapper.apply(list.get(i)));
            }
        }
        return ret;
    }

}
