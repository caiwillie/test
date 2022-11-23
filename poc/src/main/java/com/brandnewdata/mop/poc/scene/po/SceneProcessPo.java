package com.brandnewdata.mop.poc.scene.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2022-07-29
 */
@Getter
@Setter
@TableName("mop_scene_process")
public class SceneProcessPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long businessSceneId;

    private String processId;

    public static final String ID = "id";

    public static final String BUSINESS_SCENE_ID = "business_scene_id";

    public static final String PROCESS_ID = "process_id";

}
