package com.brandnewdata.mop.generator;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author caiwillie
 */
public class ProcessGenerator {

    private static final String IP = "10.101.53.4";

    private static final String PORT = "3306";

    private static final String DB_NAME = "mop";

    private static final String URL = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&useSSL=false&characterEncoding=utf8", IP, PORT, DB_NAME);

    private static final String USERNAME = "root";

    private static final String PASSWORD = "Brand@123456";

    private static final String OUTPUT = "poc/src/main/java";

    private static final String AUTHOR = System.getenv("AUTHOR"); // 通过环境变量获取值

    public static void main(String[] args) {

        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                // 全局配置
                .globalConfig((scanner, builder) -> builder
                        .author(StrUtil.isNotBlank(AUTHOR) ? AUTHOR : scanner.apply("请输入作者名称？"))
                        .outputDir(System.getProperty("user.dir") + "/" + OUTPUT)
                        .dateType(DateType.ONLY_DATE)
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
                        .parent("com.brandnewdata.mop.poc")
                        // entity的包名称
                        .entity("process.po")
                        // dao层的包名称
                        .mapper("process.dao")
                )

                // 策略配置
                .strategyConfig((scanner, builder) -> builder
                        .addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .addTablePrefix("mop_")

                        // 实体类的配置
                        .entityBuilder()
                        .formatFileName("%sPo") // 修改名称后缀
                        .enableLombok() // 加上lombok注解
                        .enableColumnConstant() // 加上字段名常量
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
