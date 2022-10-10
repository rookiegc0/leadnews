package com.heima.model.wemedia.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 自媒体用户信息表(WmUser)表实体类
 *
 * @author makejava
 * @since 2022-09-09 11:45:52
 */
@ApiModel("WmUser")
@Data
@TableName("wm_user")
public class WmUser implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @ApiModelProperty(name = "主键")
    @TableField("id")
    private Integer id;
    @ApiModelProperty(name = "${column.comment}")
    @TableField("ap_user_id")
    private Integer apUserId;
    @ApiModelProperty(name = "${column.comment}")
    @TableField("ap_author_id")
    private Integer apAuthorId;
    /**
     * 登录用户名
     */
    @ApiModelProperty(name = "登录用户名")
    @TableField("name")
    private String name;
    /**
     * 登录密码
     */
    @ApiModelProperty(name = "登录密码")
    @TableField("password")
    private String password;
    /**
     * 盐
     */
    @ApiModelProperty(name = "盐")
    @TableField("salt")
    private String salt;
    /**
     * 昵称
     */
    @ApiModelProperty(name = "昵称")
    @TableField("nickname")
    private String nickname;
    /**
     * 头像
     */
    @ApiModelProperty(name = "头像")
    @TableField("image")
    private String image;
    /**
     * 归属地
     */
    @ApiModelProperty(name = "归属地")
    @TableField("location")
    private String location;
    /**
     * 手机号
     */
    @ApiModelProperty(name = "手机号")
    @TableField("phone")
    private String phone;
    /**
     * 状态
     0 暂时不可用
     1 永久不可用
     9 正常可用
     */
    @ApiModelProperty(name = "状态 0暂时不可用 1永久不可用 9正常可用")
            @TableField("status")
            private Integer status;
            /**
             * 邮箱
            */
            @ApiModelProperty(name = "邮箱")
            @TableField("email")
            private String email;
            /**
             * 账号类型
            0 个人 
            1 企业
            2 子账号
            */
            @ApiModelProperty(name = "账号类型 0个人 1企业 2子账号")
                    @TableField("type")
                    private Boolean type;
                    /**
                     * 运营评分
                    */
                    @ApiModelProperty(name = "运营评分")
                    @TableField("score")
                    private Integer score;
                    /**
                     * 最后一次登录时间
                    */
                    @ApiModelProperty(name = "最后一次登录时间")
                    @TableField("login_time")
                    private Date loginTime;
                    /**
                     * 创建时间
                    */
                    @ApiModelProperty(name = "创建时间")
                    @TableField("created_time")
                    private Date createdTime;
}

