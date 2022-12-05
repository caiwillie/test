package com.brandnewdata.mop.poc.proxy.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
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


    public Proxy getOne(long id) {
        Assert.notNull(id, "proxy id 不能为空");
        ProxyPo entity = proxyDao.selectById(id);
        Assert.notNull(entity, "id 不存在");
        return toDTO(entity);
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
                && (state == ProxyConst.PROXY_STATE__STOPPED || state == ProxyConst.PROXY_STATE__RUNNING),
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
        proxy.setState(ProxyConst.PROXY_STATE__DEVELOPING);
        // proxy = save(proxy);
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

}
