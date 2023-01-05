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
 * @since 2022-12-28
 */
@Getter
@Setter
@TableName("mop_scene_version")
public class SceneVersionPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String version;

    private Long sceneId;

    private Integer status;

    private Long deleteFlag;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String VERSION = "version";

    public static final String SCENE_ID = "scene_id";

    public static final String STATUS = "status";

    public static final String DELETE_FLAG = "delete_flag";

}
