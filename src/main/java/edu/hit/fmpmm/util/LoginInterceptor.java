package edu.hit.fmpmm.util;

import edu.hit.fmpmm.domain.web.User;
import edu.hit.fmpmm.dto.UserDTO;
import edu.hit.fmpmm.service.web.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            // 重定向到登录页面
            response.sendRedirect("/user/login");  // 前端的普通用户登录页面路由 request.getContextPath() +  TODO
            return false;
        }
        Integer cookieId = null;
        for (Cookie item : cookies) {
            // System.out.println("item: " + item.getName());
            if (Constant.COOKIE4USER.equals(item.getName())) {
                cookieId = Integer.valueOf(item.getValue());
            }
        }
        // 如果cookie中没有包含用户登录信息，重定向到登录页面
        if (cookieId == null) {  // StringUtils.isEmpty(cookieNickname)
            response.sendRedirect("/user/login");
            return false;
        }
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute(Constant.SESSION4USER);  // UserDTO类型
        if (user == null) {  // session已经过期了
            // 将cookie中的信息保存到session
            User u = userService.getById(cookieId);
            if (u != null) {
                user = new UserDTO(u.getId(), u.getPhone(), u.getNickname(), u.getPassword());
                session.setAttribute(Constant.SESSION4USER, user);
            } else {  // 没有查询到这个用户
                return false;
            }
        }
        UserHolder.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
