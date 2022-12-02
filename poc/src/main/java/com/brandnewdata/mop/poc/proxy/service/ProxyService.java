package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.old.APIDefinition;
import com.brandnewdata.mop.poc.proxy.dto.old.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.old.ImportDTO;
import com.brandnewdata.mop.poc.proxy.dto.old.Proxy;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import com.brandnewdata.mop.poc.proxy.req.ImportFromFileReq;
import com.brandnewdata.mop.poc.proxy.resp.ApiResp;
import com.brandnewdata.mop.poc.proxy.resp.VersionSpecifiedResp;
import com.brandnewdata.mop.poc.proxy.util.SwaggerUtil;
import com.brandnewdata.mop.poc.util.PageEnhancedUtil;
import com.dxy.library.json.jackson.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author caiwillie
 */
@Service
public class ProxyService {

    @Resource
    private ProxyDao proxyDao;

    @Resource
    private EndpointService endpointService;

    @Value("${brandnewdata.api.domainPattern}")
    private String domainPattern;

    private static VersionComparator VERSION_COMPARATOR = new VersionComparator();

    public Proxy save(Proxy proxy) {

        ProxyPo entity = toEntity(proxy);
        Long id = entity.getId();
        if(id == null) {
            String name = entity.getName();
            String version = entity.getVersion();
            Assert.notNull(name, "API 名称不能为空");
            Assert.notNull(version, "API 版本不能为空");

            // 判断 名称和版本 是否唯一
            ProxyPo exist = exist(name, version);
            Assert.isNull(exist, "版本 {} 已存在", name, version);

            String domain = DigestUtil.md5Hex(StrUtil.format("{}:{}", name, version));
            entity.setDomain(domain);
            // 设置默认状态是 停止
            entity.setState(Optional.ofNullable(proxy.getState()).orElse(ProxyConst.STATE_STOP));
            if(!NumberUtil.equals(entity.getState(), ProxyConst.STATE_DEVELOPING)) {
                // 如果不是开发中，就需要校验协议
                Assert.notNull(entity.getProtocol(), "API 协议不能为空");
            }
            proxyDao.insert(entity);
        } else {
            Assert.notNull(entity.getProtocol(), "协议不能为空");

            ProxyPo oldEntity = proxyDao.selectById(id);
            // 将新对象的值拷贝到旧对象，只能修改 protocol，tag, description
            oldEntity.setProtocol(entity.getProtocol());
            oldEntity.setTag(entity.getTag());
            oldEntity.setDescription(entity.getDescription());
            proxyDao.updateById(entity);
        }
        return toDTO(entity);
    }

    private ProxyPo exist(String name, String version) {
        QueryWrapper<ProxyPo> query = new QueryWrapper<>();
        query.eq(ProxyPo.NAME, name);
        query.eq(ProxyPo.VERSION, version);
        return proxyDao.selectOne(query);
    }

    public Proxy getOne(long id) {
        Assert.notNull(id, "proxy id 不能为空");
        ProxyPo entity = proxyDao.selectById(id);
        Assert.notNull(entity, "id 不存在");
        return toDTO(entity);
    }

    public Page<Proxy> page(int pageNum, int pageSize) {
        Assert.isTrue(pageNum > 0);
        Assert.isTrue(pageSize > 0);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProxyPo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        QueryWrapper<ProxyPo> queryWrapper = new QueryWrapper<>();
        page = proxyDao.selectPage(page, queryWrapper);
        List<Proxy> records = Optional.ofNullable(page.getRecords()).orElse(ListUtil.empty())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new Page<>(page.getTotal(), records);
    }

    public Page<ApiResp> pageV2(int pageNum, int pageSize, String name, String tags) {
        QueryWrapper<ProxyPo> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(name)) {
            queryWrapper.like(ProxyPo.NAME, name);
        }

        if(StrUtil.isNotBlank(tags)) {
            String[] tagArr = tags.split(",");
            queryWrapper.in(ProxyPo.TAG, tagArr);
        }

        List<ProxyPo> entities = proxyDao.selectList(queryWrapper);
        Map<String, List<ProxyPo>> nameMap = new HashMap<>();
        if(CollUtil.isEmpty(entities)) {
            return new Page<>(0, ListUtil.empty());
        }

        for (ProxyPo entity : entities) {
            List<ProxyPo> list = nameMap.computeIfAbsent(entity.getName(), key -> new ArrayList<>());
            list.add(entity);
        }

        // 对nameMap进行排序
        List<Map.Entry<String, List<ProxyPo>>> sortedList = nameMap.entrySet().stream().sorted((o1, o2) -> {
            List<ProxyPo> list1 = o1.getValue();
            list1.sort(VERSION_COMPARATOR);
            List<ProxyPo> list2 = o2.getValue();
            list2.sort(VERSION_COMPARATOR);
            Date date1 = Optional.ofNullable(list1.get(0).getUpdateTime()).orElse(DateUtil.date(0));
            Date date2 = Optional.ofNullable(list2.get(0).getUpdateTime()).orElse(DateUtil.date(0));
            return -DateUtil.compare(date1, date2);
        }).collect(Collectors.toList());

        List<Map.Entry<String, List<ProxyPo>>> slice = PageEnhancedUtil.slice(pageNum, pageSize, sortedList);

        // flat map 获取分页获得到的所有api列表
        List<Long> apiIdList = slice.stream().flatMap(entry -> entry.getValue().stream().map(ProxyPo::getId))
                .collect(Collectors.toList());
        List<Endpoint> endpoints = endpointService.listByProxyIdList(apiIdList);

        // endpoints 根据 api id 分组
        Map<Long, Long> apiMap = endpoints.stream().collect(Collectors.groupingBy(Endpoint::getProxyId, Collectors.counting()));

        // 组装成 api resp
        List<ApiResp> records = new ArrayList<>();
        for (Map.Entry<String, List<ProxyPo>> entry : slice) {
            String apiName = entry.getKey();
            List<ProxyPo> list = entry.getValue();
            List<VersionSpecifiedResp> versions = list.stream().map(entity -> {
                VersionSpecifiedResp versionSpecifiedResp = new VersionSpecifiedResp();
                Long apiId = entity.getId();
                versionSpecifiedResp.setId(apiId);
                versionSpecifiedResp.setVersion(entity.getVersion());
                versionSpecifiedResp.setDomain(StrUtil.format(domainPattern, entity.getDomain()));
                versionSpecifiedResp.setState(entity.getState());
                versionSpecifiedResp.setProtocol(entity.getProtocol());
                versionSpecifiedResp.setUpdateTime(DateUtil.formatDateTime(entity.getUpdateTime()));
                versionSpecifiedResp.setTag(entity.getTag());
                // 设置 endpoint 的总数
                versionSpecifiedResp.setEndpointTotal(Optional.ofNullable(apiMap.get(apiId)).orElse(0L));
                return versionSpecifiedResp;
            }).collect(Collectors.toList());

            ApiResp apiResp = new ApiResp();
            apiResp.setName(apiName);
            apiResp.setVersions(versions);
            records.add(apiResp);
        }

        return new Page(sortedList.size(), records);
    }

    public void delete(Long id) {
        Assert.notNull(id, "api id 不能为空");
        List<Endpoint> endpoints = endpointService.listByProxyIdList(ListUtil.of(id));
        List<Long> endpointIdList = endpoints.stream().map(Endpoint::getId).collect(Collectors.toList());
        endpointService.deleteByIdList(endpointIdList);
        proxyDao.deleteById(id);
    }

    public void changeState(Long id, Integer state) {
        Assert.notNull(id, "api id 不能为空");
        Assert.isTrue(state != null
                && (state == ProxyConst.STATE_STOP || state == ProxyConst.STATE_RUNNING),
                "状态不能为空，且只能是 停止或运行");
        ProxyPo oldEntity = proxyDao.selectById(id);
        oldEntity.setState(state);
        proxyDao.updateById(oldEntity);
    }

    public List<String> listTags() {
        return proxyDao.listTags();
    }

    public List<String> listEndpointTags(Long proxyId) {
        List<String> ret = new ArrayList<>();
        getOne(proxyId);
        List<Endpoint> endpoints = endpointService.listByProxyIdList(ListUtil.of(proxyId));
        if(CollUtil.isEmpty(endpoints)) return ret;
        ret.addAll(endpoints.stream().map(Endpoint::getTag).filter(StrUtil::isNotEmpty).distinct().collect(Collectors.toList()));
        return ret;
    }

    public void importFromFile(ImportFromFileReq req) {
        String content = req.getFileContent();
        ImportDTO dto = SwaggerUtil.parse(content);
        // 保存 proxy
        Proxy proxy = dto.getProxy();

        // 从文件导入的状态都设置为开发中
        proxy.setState(ProxyConst.STATE_DEVELOPING);
        proxy = save(proxy);
        Long proxyId = proxy.getId();

        // 保存endpoints
        List<Endpoint> endpointList = dto.getEndpointList();
        if(CollUtil.isNotEmpty(endpointList)) {
            for (Endpoint endpoint : endpointList) {
                endpoint.setProxyId(proxyId);
                endpointService.save(endpoint);
            }
        }
    }

    public String inspect(Long proxyId, String format) {
        APIDefinition apiDefinition = new APIDefinition();

        Info info = getInfo(proxyId);

        Paths paths = getPaths(proxyId);

        apiDefinition.setInfo(info);
        apiDefinition.setPaths(paths);

        String ret = null;
        if(StrUtil.equals(format, ProxyConst.FORMAT_JSON)) {
            try {
                // 格式化输出
                ret = JacksonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(apiDefinition);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else if (StrUtil.equals(format, ProxyConst.FORMAT_YAML)) {
            String temp = JacksonUtil.to(apiDefinition);
            Map<String, Object> tempMap = JacksonUtil.fromMap(temp);
            Yaml yaml = new Yaml();
            ret = yaml.dump(tempMap);
        }

        return ret;
    }

    private Info getInfo(Long proxyId) {
        Info ret = new Info();
        Proxy proxy = getOne(proxyId);
        ret.setTitle(proxy.getName());
        ret.setVersion(proxy.getVersion());
        ret.setDescription(proxy.getDescription());
        return ret;
    }

    private Paths getPaths(Long proxyId) {
        Paths ret = new Paths();
        List<Endpoint> endpoints = endpointService.listByProxyIdList(ListUtil.of(proxyId));
        if(CollUtil.isEmpty(endpoints)) {
            return ret;
        }

        for (Endpoint endpoint : endpoints) {
            String location = endpoint.getLocation();
            PathItem pathItem = new PathItem();
            ret.put(location, pathItem);
        }
        return ret;
    }

    private static class VersionComparator implements Comparator<ProxyPo> {

        @Override
        public int compare(ProxyPo o1, ProxyPo o2) {
            Date date1 = Optional.ofNullable(o1.getUpdateTime()).orElse(DateUtil.date(0));
            Date date2 = Optional.ofNullable(o2.getUpdateTime()).orElse(DateUtil.date(0));
            // 倒序就取反
            return - DateUtil.compare(date1, date2);
        }
    }

    private Proxy toDTO(ProxyPo entity) {
        if(entity == null) return null;
        Proxy proxy = new Proxy();
        proxy.setId(entity.getId());
        proxy.setName(entity.getName());
        proxy.setProtocol(entity.getProtocol());
        proxy.setVersion(entity.getVersion());
        proxy.setDescription(entity.getDescription());
        proxy.setCreateTime(LocalDateTimeUtil.of(entity.getCreateTime()));
        proxy.setUpdateTime(LocalDateTimeUtil.of(entity.getUpdateTime()));
        proxy.setDomain(StrUtil.format(domainPattern, entity.getDomain()));
        proxy.setTag(entity.getTag());
        return proxy;
    }

    private ProxyPo toEntity(Proxy proxy) {
        Assert.notNull(proxy);
        ProxyPo entity = new ProxyPo();
        entity.setId(proxy.getId());
        entity.setName(proxy.getName());
        entity.setProtocol(proxy.getProtocol());
        entity.setVersion(proxy.getVersion());
        entity.setDescription(proxy.getDescription());
        entity.setTag(proxy.getTag());
        return entity;
    }

}
