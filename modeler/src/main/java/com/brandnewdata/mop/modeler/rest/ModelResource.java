package com.brandnewdata.mop.modeler.rest;

import com.brandnewdata.mop.modeler.pojo.vo.ModelVo;
import org.springframework.web.bind.annotation.*;

/**
 * 模型相关接口
 *
 * @author caiwillie
 */
@RestController(value = "/rest/model")
public class ModelResource {

    @GetMapping(value = "/list")
    public ModelVo list() {
        return null;
    }

    @PostMapping(value = "/create")
    public ModelVo create(@RequestBody ModelVo modelVo) {
        return null;
    }

}
