package cn.lzr.ecommerce.filter;

import cn.lzr.ecommerce.constant.CommonConstant;
import cn.lzr.ecommerce.util.TokenParseUtil;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h1>用户身份统一登录拦截器</h1>
 * */
@Slf4j
@Component
public class LoginUserInfoInterceptor implements HandlerInterceptor {
    /**
     * 前置拦截器，返回值如果为false则不通过，如果通过则放行
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 检查请求是否在白名单（例如Swagger请求），如果是则直接放行
        if (checkWhiteListUrl(request.getRequestURI())) {
            return true;
        }

        // 先尝试从 http header 里面拿到 token
        String token = request.getHeader(CommonConstant.JWT_USER_INFO_KEY);

        LoginUserInfo loginUserInfo = null;

        try {
           loginUserInfo = TokenParseUtil.parseUserInfoFromToken(token);
        } catch (Exception exception) {
            log.error("parse login user info error: [{}]", exception.getMessage(), exception);
        }

        // 实际上这里应该不会为null，因为如果为null，则网关层面都不会通过
        if (null == loginUserInfo) {
            throw new RuntimeException("can not parse current login user");
        }

        log.info("set login user info: [{}]", request.getRequestURI());
        // 注意打断点查看这里的工作线程是否均为一个
        // 设置当前请求上下文, 把用户信息填充进去
        AccessContext.setLoginUserInfo(loginUserInfo);
        return true;
    }

    /**
     * <h2>后置拦截器，我们不做任何处理</h2>
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     * (can also be {@code null})
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * <h2>请求完全结束后的处理</h2>
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param ex any exception thrown on handler execution, if any; this does not
     * include exceptions that have been handled through an exception resolver
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 还是注意看工作线程是否为同一个
        AccessContext.clearLoginUserInfo();
    }

    /**
     * <h2>校验是否是白名单接口，白名单包含如下</h2>
     * swagger2 接口
     * */
    private boolean checkWhiteListUrl(String url){
        return StringUtils.containsAny(
                url,
                "springfox",
                "swagger",
                "v2",
                "webjars",
                "doc.html"
        );
    }
}
