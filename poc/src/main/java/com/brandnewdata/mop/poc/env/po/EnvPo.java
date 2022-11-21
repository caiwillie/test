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
 * @since 2022-11-21
 */
@Getter
@Setter
@TableName("mop_env")
public class EnvPo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Date deployTime;

    private String name;

    private Integer status;

    /**
     * 0 sandbox; 1 custom
     */
    private Integer type;

    private String description;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String DEPLOY_TIME = "deploy_time";

    public static final String NAME = "name";

    public static final String STATUS = "status";

    public static final String TYPE = "type";

    public static final String DESCRIPTION = "description";

}
