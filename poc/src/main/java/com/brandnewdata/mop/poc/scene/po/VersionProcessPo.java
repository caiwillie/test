package com.brandnewdata.mop.poc.scene.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("mop_version_process")
public class VersionProcessPo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Long versionId;

    private String processId;

    private String processName;

    private String processXml;

    private String processImg;

    private Long deleteFlag;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String VERSION_ID = "version_id";

    public static final String PROCESS_ID = "process_id";

    public static final String PROCESS_NAME = "process_name";

    public static final String PROCESS_XML = "process_xml";

    public static final String PROCESS_IMG = "process_img";

    public static final String DELETE_FLAG = "delete_flag";

}
