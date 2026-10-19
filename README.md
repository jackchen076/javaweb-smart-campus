# JavaWeb 作业 9.8 —— 智慧校园管理系统（登录 / 注册）

## 一、作业要求对照

| 要求 | 完成情况 |
|------|---------|
| 使用 HTML、CSS、JavaScript 编写登录页、注册页 | ✅ |
| 登录页含用户名、密码输入框及登录按钮 | ✅ `login.html` |
| 登录页实现表单验证 | ✅ 失焦即时校验 + 提交全量校验 |
| 注册页含用户名、密码、确认密码等字段 | ✅ 另含邮箱、手机号（选填） |
| 注册页实现输入校验与提交交互 | ✅ 含确认密码一致性校验 |
| 代码结构清晰、界面美观、支持响应式布局 | ✅ ≤480px / ≤360px 断点适配 |
| 能在本地 Tomcat 服务器正常运行 | ✅ Tomcat 9.0.121 实测通过 |
| **（扩展）接入 Servlet 后端** | ✅ 含 JDBC + MySQL 持久化、记住我 |

## 二、技术栈

- **前端**：原生 HTML5 + CSS3 + JavaScript（ES5，无框架依赖）
- **后端**：Java Servlet 4.0（`javax.servlet`，对应 Tomcat 9）
- **数据库**：MySQL 8.0 + JDBC（PreparedStatement 防注入）
- **构建**：Maven（war 打包）
- **运行环境**：JDK 21 + Apache Tomcat 9.0.121

## 三、项目结构

```
smart-campus/
├── pom.xml                                  Maven 配置（war + servlet-api + mysql 驱动）
├── sql/
│   └── schema.sql                           建库建表脚本
└── src/main/
    ├── java/com/smartcampus/
    │   ├── model/User.java                  用户实体
    │   ├── dao/UserDao.java                 数据访问层（JDBC + MySQL）
    │   ├── util/DBUtil.java                 数据库连接工具
    │   ├── util/RememberMeUtil.java         「记住我」Cookie 工具
    │   ├── filter/AutoLoginFilter.java      自动登录过滤器
    │   └── servlet/
    │       ├── RegisterServlet.java         /register   注册
    │       ├── LoginServlet.java            /login      登录
    │       ├── LogoutServlet.java           /logout     注销
    │       └── CurrentUserServlet.java      /current-user 查询当前用户
    └── webapp/
        ├── login.html                       登录页
        ├── register.html                    注册页
        ├── main.html                        登录成功首页
        ├── css/style.css                    共享样式
        ├── js/login.js                      登录页脚本
        ├── js/register.js                   注册页脚本
        └── WEB-INF/web.xml                  部署描述符
```

## 四、功能说明

### 登录页（login.html）
- 用户名、密码输入框 + 登录按钮 + 记住我 + 忘记密码链接
- 校验规则：用户名 3–20 位（中文/字母/数字/下划线）；密码 6–20 位
- 密码框 👁 显隐切换、提交按钮 loading 态
- 后端校验失败时错误信息就地显示在输入框下方

### 注册页（register.html）
- 用户名、邮箱、密码、确认密码、手机号（选填）
- 校验规则：
  - 用户名：3–20 位，中文/字母/数字/下划线
  - 邮箱：标准格式校验
  - 密码：6–20 位且须同时含字母和数字
  - 确认密码：须与密码一致
  - 手机号：选填，填则须为 11 位且以 1 开头
- 注册成功自动跳转登录页

### Servlet 后端
| 路径 | 方法 | 功能 |
|------|------|------|
| `/register` | POST | 服务端二次校验 + 用户名唯一性检查 |
| `/login` | POST | 校验账号密码，写入 Session，支持记住我 |
| `/logout` | GET | 销毁 Session + 清除 Cookie |
| `/current-user` | GET | 查询当前登录用户 |

### 「记住我」机制
- 登录勾选后下发 `SC_REMEMBER` Cookie（7 天有效期，`HttpOnly`）
- Cookie 值 = `base64(用户名):md5(用户名:密钥)`，**带签名防止伪造**
- `AutoLoginFilter` 拦截受保护页面，无 Session 时自动恢复登录态

## 五、数据库设计

数据库 `smart_campus`，表 `user`：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) UNIQUE | 用户名（唯一索引） |
| password | VARCHAR(100) | 密码 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| create_time | DATETIME | 注册时间 |

初始化：`mysql -uroot -p < sql/schema.sql`

## 六、部署到 Tomcat 运行

### 方式一：IDEA（推荐）
1. IDEA 打开 `smart-campus` 文件夹（自动识别 Maven 工程）
2. `File → Project Structure` → Project SDK 选 JDK 21
3. `Run → Edit Configurations → + → Tomcat Server → Local`
   - Application server：`C:\Users\chp\WorkBuddy\apache-tomcat`
4. `Deployment → + → Artifact → smart-campus:war exploded`，Application context 填 `/smart-campus`
5. 执行 `sql/schema.sql` 建库建表
6. 修改 `src/main/java/com/smartcampus/util/DBUtil.java` 里的数据库密码
7. 点 ▶ Run

### 方式二：手动部署
```bash
# 1. 建库建表
mysql -uroot -p < sql/schema.sql

# 2. 编译（用 Tomcat 自带的 servlet-api）
javac -encoding UTF-8 -cp "tomcat/lib/servlet-api.jar;mysql-connector-j-8.4.0.jar" \
      -d build/classes src/main/java/com/smartcampus/**/*.java

# 3. 组装 webapp
#    classes 复制到 WEB-INF/classes/，驱动 jar 放到 WEB-INF/lib/
#    静态资源放到 webapp 根目录，web.xml 放到 WEB-INF/

# 4. 启动 Tomcat
tomcat/bin/startup.bat
```

**访问地址**：http://localhost:8080/smart-campus/login.html
**测试账号**：`admin / admin123`

## 七、测试结果

| 测试项 | 结果 |
|--------|------|
| 静态页面访问 | ✅ HTTP 200 |
| 注册成功（数据写入 MySQL） | ✅ 查库确认 |
| 重复用户名注册 | ✅ 被拒 |
| 两次密码不一致 | ✅ 被拒 |
| 邮箱格式错误 | ✅ 被拒 |
| 登录成功 + Session 保持 | ✅ |
| 登录失败（错误密码） | ✅ 被拒 |
| 记住我自动登录 | ✅ 仅凭 Cookie 恢复登录 |
| 伪造 Cookie | ✅ 签名校验拒绝 |
| 注销（清 Session + Cookie） | ✅ 302 跳登录页 |

## 八、安全性说明（演示环境）

本项目为课程作业演示，以下为生产环境需改进项：
1. 密码目前明文存储 → 生产应使用 **BCrypt** 加盐哈希
2. 数据库密码硬编码在 `DBUtil.java` → 生产应放配置文件或配置中心
3. 未加 CSRF Token → 生产建议补充
4. 使用 `DriverManager` 直连 → 生产建议用连接池（Druid / HikariCP）
