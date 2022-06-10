package com.brandnewdata.mop.modeler.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2022-06-09
 */
@Getter
@Setter
@TableName("mop_de_model")
public class DeModelEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createBy;

    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String name;

    private String modelKy;

    private String editorJson;


    public static final String ID = "id";

    public static final String CREATE_BY = "create_by";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_BY = "update_by";

    public static final String UPDATE_TIME = "update_time";

    public static final String NAME = "name";

    public static final String MODEL_KY = "model_ky";

    public static final String EDITOR_JSON = "editor_json";

}
