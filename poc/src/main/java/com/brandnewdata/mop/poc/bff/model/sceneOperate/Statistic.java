package com.brandnewdata.mop.poc.bff.model.sceneOperate;

import com.brandnewdata.mop.poc.bff.model.sceneOperate.charts.ChartOption;
import lombok.Getter;
import lombok.Setter;
import org.icepear.echarts.Bar;
import org.icepear.echarts.Line;
import org.icepear.echarts.Pie;

@Getter
@Setter
public class Statistic {
    /**
     * 运行次数
     */
    private int executionCount;

    /**
     * 成功次数
     */
    private int successCount;

    /**
     * 失败次数
     */
    private int failCount;

    /**
     * 场景运行次数排名
     */
    private ChartOption executionSceneRanking;

    /**
     * 场景运行次数趋势
     */
    private ChartOption executionSceneTendency;

    /**
     * 触发次数分布图
     */
    private ChartOption executionTriggerDis;

    /**
     * 触发次数趋势图
     */
    private ChartOption executionTriggerTendency;
}
