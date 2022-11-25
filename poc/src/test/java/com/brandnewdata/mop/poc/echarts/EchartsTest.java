package com.brandnewdata.mop.poc.echarts;

import org.icepear.echarts.Line;
import org.icepear.echarts.Option;
import org.icepear.echarts.charts.line.LineSeries;
import org.icepear.echarts.components.coord.cartesian.CategoryAxis;
import org.icepear.echarts.render.Engine;
import org.junit.jupiter.api.Test;

public class EchartsTest {

    @Test
    void test1() {
        Line lineChart = new Line()
                .addXAxis(new CategoryAxis()
                        .setData(new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" }))
                .addSeries(new LineSeries()
                        .setData(new Number[] { 820, 932, 901, 934, 1290, 1330, 1320 }))
                .addSeries(new LineSeries()
                .setData(new Number[] { 820, 932, 901, 934, 1290, 1330, 1320 }));

        Option option = new Option();

        Engine engine = new Engine();
        // It is recommended that you can get the serialized version of Option in the representation of JSON, which can be used directly in the template or in the RESTful APIs.
        String jsonStr = engine.renderJsonOption(lineChart);
        System.out.println(jsonStr);
    }
}
