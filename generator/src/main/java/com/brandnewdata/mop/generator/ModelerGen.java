package com.brandnewdata.mop.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author caiwillie
 */
public class ModelerGen {

    private static final String IP = "localhost";

    private static final String PORT = "3306";

    private static final String DB_NAME = "test";

    private static final String URL = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&useSSL=false&characterEncoding=utf8", IP, PORT, DB_NAME);

    private static final String USERNAME = "root";

    private static final String PASSWORD = "caiwillie";

    private static final String OUTPUT = "modeler/src/main/java";

    public static void main(String[] args) {

        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                // 全局配置
                .globalConfig((scanner, builder) -> builder
                        .author(scanner.apply("请输入作者名称？"))
                        .outputDir(System.getProperty("user.dir") + "/" + OUTPUT)
                        .dateType(DateType.TIME_PACK)
                        .fileOverride() // 覆盖已生成的文件
                        .disableOpenDir() // 生成完成后不打开目录
                )

                // 模板配置
                .templateConfig(builder -> builder
                        // 禁用controller, xml, service ,service_impl
                        .disable(TemplateType.XML, TemplateType.CONTROLLER, TemplateType.SERVICE, TemplateType.SERVICEIMPL)
                )

                // 包配置
                .packageConfig(builder -> builder
                        .parent("com.brandnewdata.mop.modeler")
                        // entity的包名称
                        .entity("pojo.entity")
                        // dao层的包名称
                        .mapper("dao")
                )

                // 策略配置
                .strategyConfig((scanner, builder) -> builder
                        .addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .addTablePrefix("mop_")

                        // 实体类的配置
                        .entityBuilder()
                        .formatFileName("%sEntity") // 修改名称后缀
                        .enableLombok() // 加上lombok注解
                        .addTableFills(
                                // 添加自动更新的标记
                                new Column("create_by", FieldFill.INSERT),
                                new Column("create_time", FieldFill.INSERT),
                                new Column("update_by", FieldFill.INSERT_UPDATE),
                                new Column("update_time", FieldFill.INSERT_UPDATE)
                        )

                        // mapper文件配置
                        .mapperBuilder()
                        .formatMapperFileName("%sDao")
                )

                .execute();

    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}
