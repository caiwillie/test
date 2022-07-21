package com.brandnewdata.mop.poc.group.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.dao.BusinessSceneDao;
import com.brandnewdata.mop.poc.group.dto.BusinessScene;
import com.brandnewdata.mop.poc.group.entity.BusinessSceneEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caiwillie
 */
@Service
public class BusinessSceneService implements IBusinessSceneService {

    @Resource
    private BusinessSceneDao businessSceneDao;

    @Override
    public Page<BusinessScene> page(int pageNumber, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BusinessSceneEntity> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(pageNumber, pageSize);
        QueryWrapper<BusinessSceneEntity> queryWrapper = new QueryWrapper<>();
        page = businessSceneDao.selectPage(page, queryWrapper);
        List<BusinessSceneEntity> entities = page.getRecords();
        List<BusinessScene> dtos = new ArrayList<>();
        if(CollUtil.isNotEmpty(entities)) {
            for (BusinessSceneEntity entity : entities) {
                BusinessScene dto = toDto(entity);
                dtos.add(dto);
            }
        }
        return new Page<>(page.getTotal(), dtos);
    }

    @Override
    public BusinessScene detail(Long id) {
        BusinessScene ret = null;
        BusinessSceneEntity entity = businessSceneDao.selectById(id);
        if(entity != null) {
            ret = toDto(entity);
        }
        return ret;
    }


    public BusinessScene toDto(BusinessSceneEntity entity) {
        BusinessScene dto = new BusinessScene();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreateTime(LocalDateTimeUtil.formatNormal(entity.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.formatNormal(entity.getUpdateTime()));
        return dto;
    }
}
