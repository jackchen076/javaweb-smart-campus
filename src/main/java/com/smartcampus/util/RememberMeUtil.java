package com.smartcampus.util;

import com.smartcampus.dao.UserDao;
import com.smartcampus.model.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 「记住我」工具类：签发 / 校验 / 清除自动登录 Cookie。
 *
 * Cookie 值格式：base64(username) + ":" + md5(username + ":" + SECRET)
 * 校验通过才认为有效，避免直接伪造用户名即可登录。
 *
 * 说明：演示实现；生产建议改用随机 token 存库（可撤销、可设过期、可单点下线）。
 */
public final class RememberMeUtil {

    /** Cookie 名称 */
    public static final String COOKIE_NAME = "SC_REMEMBER";

    /** 签名密钥（生产应放配置中心，不要硬编码） */
    private static final String SECRET = "SmartCampus#2026@JavaWeb";

    /** Cookie 有效期：7 天 */
    private static final int MAX_AGE = 7 * 24 * 60 * 60;

    private RememberMeUtil() {
    }

    /** 登录成功且勾选「记住我」时调用：下发 Cookie 并把用户身份绑定到 Session */
    public static void issue(HttpServletResponse resp, User user) {
        String username = user.getUsername();
        String value = encode(username);
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setMaxAge(MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);   // 禁止 JS 读取，降低 XSS 风险
        resp.addCookie(cookie);
    }

    /** 注销时调用：清除 Cookie */
    public static void clear(HttpServletResponse resp) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }

    /**
     * 尝试自动登录：校验 Cookie 有效性，有效则把用户写入 Session。
     *
     * @return 自动登录成功返回用户名，否则返回 null
     */
    public static String tryAutoLogin(HttpServletRequest req) {
        Cookie cookie = findCookie(req);
        if (cookie == null) {
            return null;
        }
        String username = decodeAndVerify(cookie.getValue());
        if (username == null) {
            return null;
        }
        User user = UserDao.getInstance().findByUsername(username);
        if (user == null) {
            return null;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        return user.getUsername();
    }

    /** 从请求中取出该 Cookie */
    public static Cookie findCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (COOKIE_NAME.equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                return c;
            }
        }
        return null;
    }

    // ---------- 编码 / 解码 ----------

    private static String encode(String username) {
        String b64 = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(username.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return b64 + ":" + sign(username);
    }

    private static String decodeAndVerify(String value) {
        if (value == null) {
            return null;
        }
        int idx = value.lastIndexOf(':');
        if (idx <= 0) {
            return null;
        }
        String b64 = value.substring(0, idx);
        String sig = value.substring(idx + 1);
        String username;
        try {
            username = new String(java.util.Base64.getUrlDecoder().decode(b64),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return sign(username).equals(sig) ? username : null;
    }

    /** 简单签名：md5(username + ":" + SECRET) */
    private static String sign(String username) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((username + ":" + SECRET)
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 不可用", e);
        }
    }
}
