package com.brandnewdata.mop.poc.scene.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2022-11-30
 */
@Getter
@Setter
@TableName("mop_scene_release_deploy")
public class SceneReleaseDeployPo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Long sceneId;

    private String sceneName;

    private Long versionId;

    private String versionName;

    private String processId;

    private String processName;

    private Long envId;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String SCENE_ID = "scene_id";

    public static final String SCENE_NAME = "scene_name";

    public static final String VERSION_ID = "version_id";

    public static final String VERSION_NAME = "version_name";

    public static final String PROCESS_ID = "process_id";

    public static final String PROCESS_NAME = "process_name";

    public static final String ENV_ID = "env_id";

}
