/* ============================================================
   登录页交互与表单校验
   - 失焦即时校验 + 提交时全量校验
   - 密码显示/隐藏切换
   - 演示环境模拟提交（实际项目改为 fetch 提交到后端 Servlet）
   ============================================================ */
(function () {
  "use strict";

  var form = document.getElementById("loginForm");
  var username = document.getElementById("username");
  var password = document.getElementById("password");

  /* 显示错误信息 */
  function setError(input, msg) {
    var box = form.querySelector('.error-msg[data-for="' + input.id + '"]');
    box.textContent = msg || "";
    input.classList.add("invalid");
    input.classList.remove("valid");
  }

  /* 标记校验通过 */
  function setValid(input) {
    var box = form.querySelector('.error-msg[data-for="' + input.id + '"]');
    box.textContent = "";
    input.classList.remove("invalid");
    input.classList.add("valid");
  }

  /* 用户名校验：非空 + 长度 3-20 + 合法字符 */
  function validateUsername() {
    var v = username.value.trim();
    if (!v) {
      setError(username, "用户名不能为空");
      return false;
    }
    if (v.length < 3 || v.length > 20) {
      setError(username, "用户名长度需为 3-20 个字符");
      return false;
    }
    if (!/^[\u4e00-\u9fa5\w]+$/.test(v)) {
      setError(username, "只能包含中文、字母、数字或下划线");
      return false;
    }
    setValid(username);
    return true;
  }

  /* 密码校验：非空 + 长度 6-20 */
  function validatePassword() {
    var v = password.value;
    if (!v) {
      setError(password, "密码不能为空");
      return false;
    }
    if (v.length < 6 || v.length > 20) {
      setError(password, "密码长度需为 6-20 个字符");
      return false;
    }
    setValid(password);
    return true;
  }

  /* 失焦校验 */
  username.addEventListener("blur", validateUsername);
  password.addEventListener("blur", validatePassword);

  /* 输入时若已处于错误态，则实时纠正提示 */
  username.addEventListener("input", function () {
    if (username.classList.contains("invalid")) validateUsername();
  });
  password.addEventListener("input", function () {
    if (password.classList.contains("invalid")) validatePassword();
  });

  /* 密码显示/隐藏切换 */
  form.querySelectorAll(".toggle-pwd").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var target = document.getElementById(btn.getAttribute("data-target"));
      if (target.type === "password") {
        target.type = "text";
        btn.textContent = "🙈";
      } else {
        target.type = "password";
        btn.textContent = "👁";
      }
    });
  });

  /* 提交处理：真实调用 LoginServlet */
  form.addEventListener("submit", function (e) {
    e.preventDefault();
    // 使用按位与确保两项都执行校验
    var ok = validateUsername() & validatePassword();
    if (!ok) return;

    var btn = form.querySelector(".btn-primary");
    var oldText = btn.textContent;
    btn.disabled = true;
    btn.textContent = "登录中…";

    /* 以 application/x-www-form-urlencoded 形式 POST 到 LoginServlet */
    fetch("login", {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: new URLSearchParams(new FormData(form)).toString()
    })
      .then(function (res) { return res.json(); })
      .then(function (data) {
        if (data.success) {
          btn.textContent = "登录成功，跳转中…";
          window.location.href = "main.html";
        } else {
          btn.disabled = false;
          btn.textContent = oldText;
          // 后端返回的错误信息显示在密码框下方（不暴露具体哪项错）
          setError(password, data.message || "登录失败");
        }
      })
      .catch(function (err) {
        btn.disabled = false;
        btn.textContent = oldText;
        setError(password, "网络异常，请确认服务已启动：" + err.message);
      });
  });
})();
