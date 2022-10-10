package com.heima.common.constants;

/**
 * Created on 2022/9/13.
 *
 * @author Chen Gao
 */
public interface WmNewsCoverType {
    /**
     * 当选择封面为自动：没有给封面设置图片
     */
    public static final Integer COVER_AUTO = -1;
    /**
     * 封面没有图片
     */
    public static final Integer NO_PICTURE = 0;
    /**
     * 封面有一张图片
     */
    public static final Integer ONE_PICTURE = 1;
    /**
     * 封面有多张图片
     */
    public static final Integer MORE_PICTURE = 3;
}
