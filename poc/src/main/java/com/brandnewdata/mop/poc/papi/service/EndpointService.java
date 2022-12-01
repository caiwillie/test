package com.brandnewdata.mop.poc.papi.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.papi.dao.ReverseProxyEndpointDao;
import com.brandnewdata.mop.poc.papi.dto.Endpoint;
import com.brandnewdata.mop.poc.papi.po.ReverseProxyEndpointPo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author caiwillie
 */
@Service
public class EndpointService {

    @Resource
    private ReverseProxyEndpointDao endpointDao;

    public Endpoint save(Endpoint endpoint) {
        ReverseProxyEndpointPo entity = toEntity(endpoint);
        Long id = entity.getId();
        if(id == null) {
            // endpoint 的唯一性校验
            ReverseProxyEndpointPo exist = exist(entity.getProxyId(), entity.getLocation());
            Assert.isNull(exist, "路径 {} 已存在", endpoint.getLocation());

            endpointDao.insert(entity);
            endpoint.setId(entity.getId());
        } else {
            ReverseProxyEndpointPo oldEntity = endpointDao.selectById(id);

            // 将新对象的值拷贝到旧对象，排除掉 proxyId
            BeanUtil.copyProperties(entity, oldEntity, "proxyId");
            endpointDao.updateById(oldEntity);
        }
        return endpoint;
    }

    private ReverseProxyEndpointPo exist(Long proxyId, String location) {
        QueryWrapper<ReverseProxyEndpointPo> query = new QueryWrapper<>();
        query.eq(ReverseProxyEndpointPo.PROXY_ID, proxyId);
        query.eq(ReverseProxyEndpointPo.LOCATION, location);
        return endpointDao.selectOne(query);
    }

    public Endpoint getOne(long id) {
        ReverseProxyEndpointPo entity = endpointDao.selectById(id);
        return Optional.ofNullable(entity).map(this::toDTO).orElse(null);
    }

    public Page<Endpoint> page(Long proxyId, int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 0);
        Assert.isTrue(pageSize > 0);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReverseProxyEndpointPo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        QueryWrapper<ReverseProxyEndpointPo> queryWrapper = new QueryWrapper<>();
        if(proxyId != null) queryWrapper.eq(ReverseProxyEndpointPo.PROXY_ID, proxyId);
        page = endpointDao.selectPage(page, queryWrapper);
        List<Endpoint> records = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    public void deleteByIdList(List<Long> idList) {
        if(CollUtil.isEmpty(idList)) {
            return;
        }
        // 根据 id list 删除
        endpointDao.deleteBatchIds(idList);
    }

    public List<Endpoint> listByProxyIdList(List<Long> proxyIdList) {
        if(CollUtil.isEmpty(proxyIdList)) {
            return ListUtil.empty();
        }
        QueryWrapper<ReverseProxyEndpointPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ReverseProxyEndpointPo.PROXY_ID, proxyIdList);
        List<ReverseProxyEndpointPo> list = endpointDao.selectList(queryWrapper);
        List<Endpoint> ret = list.stream().map(this::toDTO).collect(Collectors.toList());
        return ret;
    }

    private ReverseProxyEndpointPo toEntity(Endpoint dto) {
        Assert.notNull(dto);
        ReverseProxyEndpointPo entity = new ReverseProxyEndpointPo();
        entity.setId(dto.getId());
        entity.setProxyId(dto.getProxyId());
        entity.setLocation(dto.getLocation());
        entity.setDescription(dto.getDescription());
        entity.setBackendType(dto.getBackendType());
        entity.setBackendConfig(dto.getBackendConfig());
        entity.setTag(dto.getTag());
        return entity;
    }

    private Endpoint toDTO(ReverseProxyEndpointPo entity) {
        Assert.notNull(entity);
        Endpoint dto = new Endpoint();
        dto.setId(entity.getId());
        dto.setProxyId(entity.getProxyId());
        dto.setLocation(entity.getLocation());
        dto.setDescription(entity.getDescription());
        dto.setBackendType(entity.getBackendType());
        dto.setBackendConfig(entity.getBackendConfig());
        dto.setTag(entity.getTag());
        return dto;
    }



}
