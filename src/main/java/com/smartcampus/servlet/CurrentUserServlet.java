package com.smartcampus.servlet;

import com.smartcampus.model.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 当前登录用户查询 Servlet：供主页判断登录状态、展示用户名。
 *
 * 访问方式：GET /current-user
 * 返回：JSON { "loggedIn": true/false, "username": "..." }
 */
@WebServlet(name = "CurrentUserServlet", urlPatterns = {"/current-user"})
public class CurrentUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        PrintWriter out = resp.getWriter();
        if (user == null) {
            out.print("{\"loggedIn\":false}");
        } else {
            out.print("{\"loggedIn\":true,\"username\":\"" + escape(user.getUsername()) + "\"}");
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
