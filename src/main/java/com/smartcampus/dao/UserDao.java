package com.smartcampus.dao;

import com.smartcampus.model.User;
import com.smartcampus.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象 —— JDBC + MySQL 实现。
 *
 * 表结构见 sql/schema.sql（库 smart_campus，表 user）。
 * 全部使用 PreparedStatement 预编译，杜绝 SQL 注入。
 * Servlet 层调用方式与内存版完全一致（方法签名未变）。
 */
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();

    public static UserDao getInstance() {
        return INSTANCE;
    }

    private UserDao() {
    }

    /** 按用户名查用户，不存在返回 null */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id, username, password, email, phone FROM `user` WHERE username = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /** 用户名是否已存在 */
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    /** 新增用户，返回带自增主键的 User；用户名重复抛 RuntimeException */
    public User add(String username, String password, String email, String phone) {
        String sql = "INSERT INTO `user` (username, password, email, phone) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, (phone == null || phone.isEmpty()) ? null : phone);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("注册失败：写入 0 行");
            }
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setPhone(phone);
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            // 1062 = Duplicate entry，唯一索引冲突
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("该用户名已被注册，请更换");
            }
            throw new RuntimeException("注册失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /** 返回全部用户（调试/展示用） */
    public List<User> findAll() {
        String sql = "SELECT id, username, password, email, phone FROM `user` ORDER BY id";
        List<User> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户列表失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /** 用户总数 */
    public int count() {
        String sql = "SELECT COUNT(*) FROM `user`";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("统计用户数失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /** ResultSet -> User */
    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}
