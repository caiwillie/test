package com.brandnewdata.mop.poc.bff.vo.homepage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据概览
 *
 * @author jekyll 2022-12-13 17:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataBriefVo {


    private Integer sceneInProgress;

    private Integer sceneTotal;


    private Integer weeklyRuntimeTotal;

    private Integer weeklyRuntimeFail;


    private Integer apiServiceCount;

    private Integer apiPathCount;


    private Integer connectorBaseCount;

    private Integer connectorDevCount;



}
