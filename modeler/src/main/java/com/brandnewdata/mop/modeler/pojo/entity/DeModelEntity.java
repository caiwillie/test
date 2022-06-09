package com.brandnewdata.mop.modeler.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2022-06-09
 */
@Getter
@Setter
@TableName("mop_de_model")
public class DeModelEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdBy;

    private String created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String lastUpdatedBy;

    private LocalDateTime lastUpdated;


}
