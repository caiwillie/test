package com.brandnewdata.mop.poc.papi.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brandnewdata.mop.poc.papi.entity.ReverseProxyEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author caiwillie
 * @since 2022-09-26
 */
public interface ReverseProxyDao extends BaseMapper<ReverseProxyEntity> {

    @Select("select distinct tag from mop_reverse_proxy where tag is not null")
    List<String> listTags();

}
