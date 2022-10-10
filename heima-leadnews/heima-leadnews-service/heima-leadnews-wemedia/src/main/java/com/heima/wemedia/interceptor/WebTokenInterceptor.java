package com.heima.wemedia.interceptor;

import com.heima.model.wemedia.entity.WmUser;
import com.heima.utils.threads.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
@Slf4j

/**
 * Created on 2022/9/9.
 *作用就是解析http请求头中的userId并且放入到threadlocal当中去
 * @author Chen Gao
 */
public class WebTokenInterceptor implements HandlerInterceptor {

    /**
     * 在请求controller之前就获取头中的userID然后存到threadlocal当中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //得到header中的信息
        String userId = request.getHeader("userId");
        Optional<String> optional = Optional.ofNullable(userId);
        if(optional.isPresent()){
            //把用户id存入threadloacl中
            WmUser wmUser = new WmUser();
            wmUser.setId(Integer.valueOf(userId));
            ThreadLocalUtils.setWmUserThreadLocal(wmUser);
            log.info("wmTokenFilter设置用户信息到threadlocal中...");
        }

        return true;
    }

    /**
     * 在请求controller之后，把线程threadlocal的内容清除一下
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        ThreadLocalUtils.cleanThreadLocal();
    }
}
