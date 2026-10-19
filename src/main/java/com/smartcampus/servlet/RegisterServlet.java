package com.smartcampus.servlet;

import com.smartcampus.dao.UserDao;
import com.smartcampus.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 注册 Servlet：接收注册页表单，做服务端校验后写入用户数据。
 *
 * 访问方式：POST /register
 * 参数：username, email, password, confirm, phone
 * 返回：JSON { "success": true/false, "message": "...", "redirect": "login.html" }
 *
 * 设计：前端已做一轮 JS 校验，这里做「服务端二次校验」，
 *       防止绕过前端直接提交（真实项目必须这么做，前端校验不可信）。
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final UserDao userDao = UserDao.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 统一按 UTF-8 解析请求体（Tomcat 9 默认 ISO-8859-1）
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String username = trim(req.getParameter("username"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");
        String phone = trim(req.getParameter("phone"));

        PrintWriter out = resp.getWriter();

        // ---------- 服务端校验 ----------
        if (isEmpty(username) || isEmpty(email) || isEmpty(password)) {
            writeJson(out, false, "用户名、邮箱、密码均不能为空", null);
            return;
        }
        if (username.length() < 3 || username.length() > 20) {
            writeJson(out, false, "用户名长度需为 3-20 个字符", null);
            return;
        }
        if (!username.matches("^[\\u4e00-\\u9fa5\\w]+$")) {
            writeJson(out, false, "用户名只能包含中文、字母、数字或下划线", null);
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            writeJson(out, false, "邮箱格式不正确", null);
            return;
        }
        if (password.length() < 6 || password.length() > 20) {
            writeJson(out, false, "密码长度需为 6-20 个字符", null);
            return;
        }
        if (!(password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*"))) {
            writeJson(out, false, "密码须同时包含字母和数字", null);
            return;
        }
        if (!password.equals(confirm)) {
            writeJson(out, false, "两次输入的密码不一致", null);
            return;
        }
        if (!isEmpty(phone) && !phone.matches("^1\\d{10}$")) {
            writeJson(out, false, "请输入有效的 11 位手机号", null);
            return;
        }
        // 用户名唯一性校验（服务端权威判断）
        if (userDao.existsByUsername(username)) {
            writeJson(out, false, "该用户名已被注册，请更换", null);
            return;
        }

        // ---------- 写入 ----------
        User user = userDao.add(username, password, email, phone);
        System.out.println("[RegisterServlet] 注册成功 -> " + user + "，当前用户数=" + userDao.count());

        writeJson(out, true, "注册成功，正在跳转登录页…", "login.html");
    }

    /** 直接 GET 访问时给出提示，避免 405 困惑 */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().println("<h3>注册接口</h3><p>请通过注册页面的表单以 POST 方式提交。</p>");
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /** 输出 JSON（手动拼接，避免引入额外依赖；真实项目建议用 Jackson/Gson） */
    private static void writeJson(PrintWriter out, boolean success, String message, String redirect) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":").append(success).append(",");
        sb.append("\"message\":\"").append(escape(message)).append("\"");
        if (redirect != null) {
            sb.append(",\"redirect\":\"").append(escape(redirect)).append("\"");
        }
        sb.append("}");
        out.print(sb.toString());
    }

    /** 转义 JSON 字符串中的特殊字符 */
    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
