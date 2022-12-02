package com.brandnewdata.mop.poc.proxy.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
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
public interface ProxyDao extends BaseMapper<ProxyPo> {

    @Select("select distinct tag from mop_reverse_proxy where tag is not null")
    List<String> listTags();

}
