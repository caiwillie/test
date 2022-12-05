package com.brandnewdata.mop.poc.bff.service.env;

import cn.hutool.core.util.NumberUtil;
import com.brandnewdata.mop.poc.bff.converter.env.EnvVoConverter;
import com.brandnewdata.mop.poc.bff.vo.env.EnvVo;
import com.brandnewdata.mop.poc.env.service.EnvService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.brandnewdata.mop.poc.constant.EnvConst.ENV_TYPE__NORMAL;

@Service
public class EnvBffService {

    @Resource
    private EnvService service;

    public List<EnvVo> list() {
        return service.fetchEnvList().stream().filter(envDto -> NumberUtil.equals(envDto.getType(), ENV_TYPE__NORMAL))
                .map(EnvVoConverter::createFrom).collect(Collectors.toList());
    }

}
