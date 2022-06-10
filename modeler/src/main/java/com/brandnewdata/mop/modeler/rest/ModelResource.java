package com.brandnewdata.mop.modeler.rest;

import com.brandnewdata.mop.modeler.common.service.result.PageVo;
import com.brandnewdata.mop.modeler.common.service.result.Result;
import com.brandnewdata.mop.modeler.pojo.vo.ModelVo;
import org.springframework.web.bind.annotation.*;

/**
 * 模型相关接口
 *
 * @author caiwillie
 */
@RestController(value = "/rest/model")
public class ModelResource {

    @GetMapping(value = "/page")
    public Result<PageVo<ModelVo>> page() {
        return null;
    }

    @PostMapping(value = "/save")
    public Result<ModelVo> save(@RequestBody ModelVo modelVo) {
        return null;
    }


    @GetMapping(value = "detail")
    public Result<ModelVo> detail(@RequestParam("id") String modelKey) {
        return null;
    }

    @PostMapping(value = "/deploy")
    public Result<ModelVo> deploy(@RequestBody ModelVo modelVo) {
        return null;
    }


}
