package com.brandnewdata.mop.poc.scene.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2023-01-06
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

    private Double deployProgressPercentage;

    private String exceptionMessage;

    private Integer deployStatus;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String VERSION = "version";

    public static final String SCENE_ID = "scene_id";

    public static final String STATUS = "status";

    public static final String DELETE_FLAG = "delete_flag";

    public static final String DEPLOY_PROGRESS_PERCENTAGE = "deploy_progress_percentage";

    public static final String EXCEPTION_MESSAGE = "exception_message";

    public static final String DEPLOY_STATUS = "deploy_status";

}
