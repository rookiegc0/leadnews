package com.heima.model.wemedia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 敏感词信息表(WmSensitive)表实体类
 *
 * @author makejava
 * @since 2022-09-14 20:32:41
 */
@ApiModel("WmSensitive")
@Data
@TableName("wm_sensitive")
public class WmSensitive implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @ApiModelProperty(name = "主键")
    @TableField("id")
    private Integer id;
    /**
     * 敏感词
     */
    @ApiModelProperty(name = "敏感词")
    @TableField("sensitives")
    private String sensitives;
    /**
     * 创建时间
     */
    @ApiModelProperty(name = "创建时间")
    @TableField("created_time")
    private Date createdTime;
}

