package com.brandnewdata.mop.api.bff.home;

import com.brandnewdata.mop.api.bff.home.dto.HomeSceneDto;
import com.brandnewdata.mop.api.bff.home.dto.HomeStaticOverviewCountDto;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(name = "poc", contextId = "bffHomeApi")
public interface IHomeApi {

    HomeStaticOverviewCountDto staticOverviewCount();

    List<HomeSceneDto> sceneList();

}
