package com.brandnewdata.mop.poc.process.po;

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
 * @since 2022-11-25
 */
@Getter
@Setter
@TableName("mop_process_release_deploy")
public class ProcessReleaseDeployPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Long envId;

    private String processId;

    private Long processZeebeKey;

    private String processZeebeXml;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String ENV_ID = "env_id";

    public static final String PROCESS_ID = "process_id";

    public static final String PROCESS_ZEEBE_KEY = "process_zeebe_key";

    public static final String PROCESS_ZEEBE_XML = "process_zeebe_xml";

}