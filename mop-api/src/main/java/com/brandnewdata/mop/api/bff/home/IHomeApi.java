package com.brandnewdata.mop.api.bff.home;

import com.brandnewdata.mop.api.bff.home.dto.HomeSceneDto;
import com.brandnewdata.mop.api.bff.home.dto.HomeStaticOverviewCountDto;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

// @FeignClient(name = "poc", contextId = "bffHomeApi")
public interface IHomeApi {

    @RequestMapping("/api/bff/home/staticOverviewCount")
    HomeStaticOverviewCountDto staticOverviewCount();

    @RequestMapping("/api/bff/home/sceneList")
    List<HomeSceneDto> sceneList();

}
