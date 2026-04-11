package com.vagrant.nanoblog.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.mapper.UserRoleMapper;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.pojo.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        User loginUser = (User) request.getSession().getAttribute("LOGIN_USER");

        //未登录
        if (loginUser == null) {
            handleUnauthorized(request, response, "请先登录");
            return false;
        }

        //查角色，roleId=2是管理员
        UserRole userRole = userRoleMapper.selectOne(
                new QueryWrapper<UserRole>().eq("user_id", loginUser.getId())
        );

        if (userRole == null || !userRole.getRoleId().equals(2L)) {
            handleUnauthorized(request, response, "无管理员权限");
            return false;
        }

        return true;
    }

    private void handleUnauthorized(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String msg) throws Exception {
        String accept = request.getHeader("Accept");
        //Ajax 请求返回 JSON
        if (accept != null && accept.contains("application/json")
                || request.getHeader("X-Requested-With") != null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}"
            );
        } else {
            //页面请求直接重定向到登录页
            response.sendRedirect(request.getContextPath() + "/pages/front/login.html");
        }
    }
}