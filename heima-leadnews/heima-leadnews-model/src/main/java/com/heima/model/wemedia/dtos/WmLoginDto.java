package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * Created on 2022/9/9.
 *
 * @author Chen Gao
 */
@Data
public class WmLoginDto {

    /**
     * 用户名
     */
    private String name;
    /**
     * 密码
     */
    private String password;
}
