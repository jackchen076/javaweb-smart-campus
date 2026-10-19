package com.smartcampus.filter;

import com.smartcampus.util.RememberMeUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 自动登录过滤器：拦截对受保护资源的请求，
 * 若当前 Session 中没有登录用户，则尝试用「记住我」Cookie 自动恢复登录态。
 *
 * 只拦 main.html 与 /current-user，避免对登录/注册/静态资源产生无谓开销。
 */
@WebFilter(filterName = "AutoLoginFilter", urlPatterns = {"/main.html", "/current-user"})
public class AutoLoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        boolean loggedIn = session != null && session.getAttribute("user") != null;
        if (!loggedIn) {
            String username = RememberMeUtil.tryAutoLogin(req);
            if (username != null) {
                System.out.println("[AutoLoginFilter] 通过「记住我」Cookie 自动登录：" + username);
            }
        }
        chain.doFilter(request, response);
    }
}
