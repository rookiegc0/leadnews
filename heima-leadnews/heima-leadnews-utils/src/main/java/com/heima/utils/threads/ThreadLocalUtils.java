package com.heima.utils.threads;

import com.heima.model.wemedia.entity.WmUser;

/**
 * Created on 2022/9/9.
 *为线程填入当前用户ID，和探花的userholder很类似
 * @author Chen Gao
 */

public class ThreadLocalUtils {

    public static final ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 从ThreadLocal取值
     */
    public static WmUser getFromThreadLocal(){
        return WM_USER_THREAD_LOCAL.get();
    }

    /**
     * 往ThreadLocal填值
     */
    public static void setWmUserThreadLocal(WmUser wmUser){
        WM_USER_THREAD_LOCAL.set(wmUser);
    }

    /**
     * 清楚ThreaLocal的值
     */
    public static void cleanThreadLocal(){
        WM_USER_THREAD_LOCAL.remove();
    }
}
