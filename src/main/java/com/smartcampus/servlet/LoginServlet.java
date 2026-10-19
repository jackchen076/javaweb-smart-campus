package com.smartcampus.servlet;

import com.smartcampus.dao.UserDao;
import com.smartcampus.model.User;
import com.smartcampus.util.RememberMeUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登录 Servlet：校验用户名密码，成功后写入 Session。
 *
 * 访问方式：POST /login
 * 参数：username, password
 * 返回：JSON { "success": true/false, "message": "...", "username": "..." }
 *
 * 登录成功后把用户放进 HttpSession，后续受保护页面可用
 * {@code session.getAttribute("user")} 判断是否已登录。
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserDao userDao = UserDao.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        // 「记住我」：勾选时为 "on" 或 "true"
        String remember = req.getParameter("remember");
        PrintWriter out = resp.getWriter();

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            writeJson(out, false, "用户名和密码不能为空", null);
            return;
        }
        username = username.trim();

        User user = userDao.findByUsername(username);

        // 统一提示「用户名或密码错误」，不暴露具体是哪一项错（安全实践）
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("[LoginServlet] 登录失败 -> username=" + username);
            writeJson(out, false, "用户名或密码错误", null);
            return;
        }

        // 登录成功：写入 Session
        HttpSession session = req.getSession();
        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());

        // 处理「记住我」：勾选则下发 7 天有效的签名 Cookie
        boolean rememberMe = "on".equalsIgnoreCase(remember)
                || "true".equalsIgnoreCase(remember)
                || "1".equals(remember);
        if (rememberMe) {
            RememberMeUtil.issue(resp, user);
        } else {
            RememberMeUtil.clear(resp);
        }

        System.out.println("[LoginServlet] 登录成功 -> " + user.getUsername()
                + "，rememberMe=" + rememberMe + "，sessionId=" + session.getId());

        writeJson(out, true, "登录成功，正在进入系统…", user.getUsername());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().println("<h3>登录接口</h3><p>请通过登录页面的表单以 POST 方式提交。</p>");
    }

    private static void writeJson(PrintWriter out, boolean success, String message, String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":").append(success).append(",");
        sb.append("\"message\":\"").append(escape(message)).append("\"");
        if (username != null) {
            sb.append(",\"username\":\"").append(escape(username)).append("\"");
        }
        sb.append("}");
        out.print(sb.toString());
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
