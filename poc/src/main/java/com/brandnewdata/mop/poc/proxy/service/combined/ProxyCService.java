package com.brandnewdata.mop.poc.proxy.service.combined;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.proxy.dao.ProxyDao;
import com.brandnewdata.mop.poc.proxy.dto.ImportDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.filter.ProxyEndpointFilter;
import com.brandnewdata.mop.poc.proxy.dto.openapi.OpenAPI;
import com.brandnewdata.mop.poc.proxy.po.ProxyPo;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.util.SwaggerUtil2;
import com.dxy.library.json.jackson.JacksonUtil;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ProxyCService implements IProxyCService {

    @Resource
    private ProxyDao proxyDao;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyAService proxyAService;

    public ProxyCService(IProxyEndpointAService proxyEndpointAService, IProxyAService proxyAService) {
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyAService = proxyAService;
    }


    @Override
    @Transactional
    public void deleteById(Long id) {
        Assert.notNull(id, "proxy id must not null");
        ProxyPo proxyPo = proxyDao.selectById(id);
        Assert.notNull(proxyPo, "proxy id not exist: {}", id);
        Integer state = proxyPo.getState();
        Assert.isTrue(NumberUtil.equals(ProxyConst.PROXY_STATE__STOPPED, state), "api状态异常");

        UpdateWrapper<ProxyPo> update = new UpdateWrapper<>();
        update.setSql(StrUtil.format("{} = {}", ProxyPo.DELETE_FLAG, ProxyPo.ID));
        update.eq(ProxyPo.ID, id);
        proxyDao.update(null, update);
        proxyEndpointAService.deleteByProxyId(id);
    }

    @SneakyThrows
    public String inspect(Long proxyId, String format) {
        String ret = null;
        Assert.notNull(proxyId);
        Assert.isTrue(StrUtil.equalsAny(format, ProxyConst.FORMAT_JSON, ProxyConst.FORMAT_YAML));

        OpenAPI openAPI = getOpenAPI(proxyId);

        if(StrUtil.equals(format, ProxyConst.FORMAT_JSON)) {
            // 格式化输出
            ret = JacksonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(openAPI);
        } else if (StrUtil.equals(format, ProxyConst.FORMAT_YAML)) {
            String temp = JacksonUtil.to(openAPI);
            Map<String, Object> tempMap = JacksonUtil.fromMap(temp);
            Yaml yaml = new Yaml();
            ret = yaml.dump(tempMap);
        }

        return ret;
    }

    @Override
    public void importProxy(String content, String format) {
        ImportDto importDto = SwaggerUtil2.parse(content);
        ProxyDto proxyDto = importDto.getProxy();
        List<ProxyEndpointDto> endpointList = importDto.getEndpointList();

        proxyDto = proxyAService.save(proxyDto, true);
        for (ProxyEndpointDto proxyEndpointDto : endpointList) {

        }

    }

    private OpenAPI getOpenAPI(Long proxyId) {
        OpenAPI ret = new OpenAPI();

        Info info = getOpenAPIInfo(proxyId);
        Paths paths = getOpenAPIPaths(proxyId);

        ret.setInfo(info);
        ret.setPaths(paths);

        return ret;
    }
    private Info getOpenAPIInfo(Long proxyId) {
        Info ret = new Info();
        ProxyDto proxyDto = proxyAService.fetchById(ListUtil.of(proxyId)).get(proxyId);
        ret.setTitle(proxyDto.getName());
        ret.setVersion(proxyDto.getVersion());
        ret.setDescription(proxyDto.getDescription());
        return ret;
    }

    private Paths getOpenAPIPaths(Long proxyId) {
        Paths ret = new Paths();
        List<ProxyEndpointDto> proxyEndpointDtoList = proxyEndpointAService
                .fetchListByProxyIdAndFilter(ListUtil.of(proxyId), new ProxyEndpointFilter()).get(proxyId);
        if(CollUtil.isEmpty(proxyEndpointDtoList)) {
            return ret;
        }

        for (ProxyEndpointDto proxyEndpointDto : proxyEndpointDtoList) {
            String location = proxyEndpointDto.getLocation();
            PathItem pathItem = new PathItem();
            ret.put(location, pathItem);
        }

        return ret;
    }
}
