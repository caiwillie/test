package com.brandnewdata.mop.poc.pojo.vo;

import lombok.Data;

/**
 * The type Model vo.
 *
 * @author caiwillie
 */
@Data
public class ModelVo {

    /**
     * id
     */
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型标识
     */
    private String modelKey;

    /**
     * 模型定义
     */
    private String editorXML;
}
