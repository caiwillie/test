package com.brandnewdata.mop.poc.process.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2022-07-27
 */
@Getter
@Setter
@TableName("mop_process_definition")
public class ProcessDefinitionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String xml;

    private String imgUrl;


    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String XML = "xml";

    public static final String IMG_URL = "img_url";

}
