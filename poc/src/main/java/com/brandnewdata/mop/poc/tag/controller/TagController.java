package com.brandnewdata.mop.poc.tag.controller;

import com.brandnewdata.common.webresult.Result;
import com.brandnewdata.mop.poc.tag.resp.TagResp;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签相关的接口
 *
 * @author caiwillie
 */
@RestController
public class TagController {

    /**
     * 列表（不分页）
     *
     * @return the result
     */
    public Result<List<TagResp>> list() {
        return null;
    }



}
