package com.smartcampus.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接工具类。
 *
 * 修改下面的 URL / USER / PASSWORD 即可对接你的 MySQL。
 * 演示环境直接 DriverManager 获取连接；生产建议换成连接池（Druid / HikariCP）。
 */
public final class DBUtil {

    /** 数据库连接地址：库名 smart_campus，关闭 SSL、设置时区与 UTF-8 */
    private static final String URL =
            "jdbc:mysql://localhost:3306/smart_campus"
                    + "?useSSL=false&serverTimezone=Asia/Shanghai"
                    + "&characterEncoding=utf8&allowPublicKeyRetrieval=true";

    /** 数据库用户名 */
    private static final String USER = "root";

    /** 数据库密码 */
    private static final String PASSWORD = "123456";

    static {
        try {
            // MySQL 8 驱动类名
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("未找到 MySQL 驱动，请检查 pom.xml 依赖：" + e.getMessage());
        }
    }

    private DBUtil() {
    }

    /** 获取数据库连接 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 静默关闭，用于 finally 块 */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    /** 供启动自检：数据库是否可连通 */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBUtil] 数据库连接失败：" + e.getMessage());
            return false;
        }
    }
}
