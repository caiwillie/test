package com.brandnewdata.mop.poc.scene.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2022-11-10
 */
@Getter
@Setter
@TableName("mop_scene_load")
public class SceneLoadPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private byte[] zipBytes;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public static final String ID = "id";

    public static final String ZIP_BYTES = "zip_bytes";

    public static final String CREATE_BY = "create_by";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_BY = "update_by";

    public static final String UPDATE_TIME = "update_time";

}
