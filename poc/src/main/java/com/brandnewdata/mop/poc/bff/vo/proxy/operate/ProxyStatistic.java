package com.brandnewdata.mop.poc.bff.vo.proxy.operate;

import com.brandnewdata.mop.poc.bff.vo.operate.charts.ChartOption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProxyStatistic {
    /**
     * 请求次数
     */
    private int totalCount;

    /**
     * 成功次数
     */
    private int successCount;

    /**
     * 失败次数
     */
    private int failCount;

    /**
     * 平均耗时
     */
    private double averageTimeConsuming;

    /**
     * 请求次数排名（API服务）
     */
    private ChartOption callCountProxyRanking;

    /**
     * 请求耗时排名（API服务）
     */
    private ChartOption timeConsumingProxyRanking;

    /**
     * 请求次数排名（API路径）
     */
    private ChartOption callCountEndpointRanking;

    /**
     * 请求耗时排名（API路径）
     */
    private ChartOption timeConsumingEndpointRanking;

    /**
     * 请求次数趋势图
     */
    private ChartOption callCountTendency;

    /**
     * 平均请求耗时趋势图
     */
    private ChartOption timeConsumingTendency;

}
