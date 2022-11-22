package com.brandnewdata.mop.poc.env.po;

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
 * @since 2022-11-22
 */
@Getter
@Setter
@TableName("mop_env_service")
public class EnvServicePo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String name;

    private Long envId;

    private String clusterIp;

    private String ports;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String NAME = "name";

    public static final String ENV_ID = "env_id";

    public static final String CLUSTER_IP = "cluster_ip";

    public static final String PORTS = "ports";

}
