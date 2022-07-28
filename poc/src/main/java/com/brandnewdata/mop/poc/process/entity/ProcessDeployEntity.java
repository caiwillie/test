package com.brandnewdata.mop.poc.process.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2022-07-28
 */
@Getter
@Setter
@TableName("mop_process_deploy")
public class ProcessDeployEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String processId;

    private String processName;

    private String processXml;

    private Integer version;

    private Integer type;

    private Long zeebeKey;

    private String zeebeXml;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String PROCESS_ID = "process_id";

    public static final String PROCESS_NAME = "process_name";

    public static final String PROCESS_XML = "process_xml";

    public static final String VERSION = "version";

    public static final String TYPE = "type";

    public static final String ZEEBE_KEY = "zeebe_key";

    public static final String ZEEBE_XML = "zeebe_xml";

}
