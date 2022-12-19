package com.brandnewdata.mop.poc.process.po;

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
 * @since 2022-12-19
 */
@Getter
@Setter
@TableName("mop_process_deploy_task")
public class ProcessDeployTaskPo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String processId;

    private String processName;

    private String processXml;

    private String processDigest;

    private Long envId;

    private Integer deployStatus;

    private String errorMessage;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String PROCESS_ID = "process_id";

    public static final String PROCESS_NAME = "process_name";

    public static final String PROCESS_XML = "process_xml";

    public static final String PROCESS_DIGEST = "process_digest";

    public static final String ENV_ID = "env_id";

    public static final String DEPLOY_STATUS = "deploy_status";

    public static final String ERROR_MESSAGE = "error_message";

}
