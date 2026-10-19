package com.smartcampus.servlet;

import com.smartcampus.util.RememberMeUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 注销 Servlet：销毁 Session、清除「记住我」Cookie，并跳回登录页。
 *
 * 访问方式：GET /logout
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 同时清掉自动登录 Cookie，否则会被立刻自动登录回来
        RememberMeUtil.clear(resp);
        resp.sendRedirect("login.html");
    }
}
