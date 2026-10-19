/* ============================================================
   注册页交互与表单校验
   - 用户名 / 邮箱 / 密码 / 确认密码 / 手机号（选填）校验
   - 确认密码一致性检查
   - 密码显示/隐藏切换
   - 提交成功后给出反馈并重置表单（演示环境）
   ============================================================ */
(function () {
  "use strict";

  var form = document.getElementById("registerForm");
  var username = document.getElementById("username");
  var email = document.getElementById("email");
  var password = document.getElementById("password");
  var confirm = document.getElementById("confirm");
  var phone = document.getElementById("phone");

  function setError(input, msg) {
    var box = form.querySelector('.error-msg[data-for="' + input.id + '"]');
    box.textContent = msg || "";
    input.classList.add("invalid");
    input.classList.remove("valid");
  }

  function setValid(input) {
    var box = form.querySelector('.error-msg[data-for="' + input.id + '"]');
    box.textContent = "";
    input.classList.remove("invalid");
    input.classList.add("valid");
  }

  /* 用户名：非空 + 3-20 + 合法字符 */
  function validateUsername() {
    var v = username.value.trim();
    if (!v) return fail(username, "用户名不能为空");
    if (v.length < 3 || v.length > 20) return fail(username, "用户名长度需为 3-20 个字符");
    if (!/^[\u4e00-\u9fa5\w]+$/.test(v)) return fail(username, "只能包含中文、字母、数字或下划线");
    return pass(username);
  }

  /* 邮箱：标准格式 */
  function validateEmail() {
    var v = email.value.trim();
    if (!v) return fail(email, "邮箱不能为空");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) return fail(email, "邮箱格式不正确");
    return pass(email);
  }

  /* 密码：6-20 且同时含字母和数字 */
  function validatePassword() {
    var v = password.value;
    if (!v) return fail(password, "密码不能为空");
    if (v.length < 6 || v.length > 20) return fail(password, "密码长度需为 6-20 个字符");
    if (!(/[A-Za-z]/.test(v) && /\d/.test(v))) return fail(password, "密码须同时包含字母和数字");
    return pass(password);
  }

  /* 确认密码：非空 + 与密码一致 */
  function validateConfirm() {
    var v = confirm.value;
    if (!v) return fail(confirm, "请再次输入密码");
    if (v !== password.value) return fail(confirm, "两次输入的密码不一致");
    return pass(confirm);
  }

  /* 手机号：选填，填写则须为 11 位且以 1 开头 */
  function validatePhone() {
    var v = phone.value.trim();
    if (!v) return pass(phone); // 选填
    if (!/^1\d{10}$/.test(v)) return fail(phone, "请输入有效的 11 位手机号");
    return pass(phone);
  }

  function fail(input, msg) {
    setError(input, msg);
    return false;
  }
  function pass(input) {
    setValid(input);
    return true;
  }

  /* 失焦校验 */
  username.addEventListener("blur", validateUsername);
  email.addEventListener("blur", validateEmail);
  password.addEventListener("blur", validatePassword);
  confirm.addEventListener("blur", validateConfirm);
  phone.addEventListener("blur", validatePhone);

  /* 输入时实时纠正已出错字段；密码变化时同步检查确认密码 */
  username.addEventListener("input", function () {
    if (username.classList.contains("invalid")) validateUsername();
  });
  email.addEventListener("input", function () {
    if (email.classList.contains("invalid")) validateEmail();
  });
  password.addEventListener("input", function () {
    if (password.classList.contains("invalid")) validatePassword();
    if (confirm.classList.contains("invalid")) validateConfirm();
  });
  confirm.addEventListener("input", function () {
    if (confirm.classList.contains("invalid")) validateConfirm();
  });
  phone.addEventListener("input", function () {
    if (phone.classList.contains("invalid")) validatePhone();
  });

  /* 密码显示/隐藏切换（两个密码框） */
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

  /* 提交处理：真实调用 RegisterServlet */
  form.addEventListener("submit", function (e) {
    e.preventDefault();
    var ok =
      validateUsername() &
      validateEmail() &
      validatePassword() &
      validateConfirm() &
      validatePhone();

    if (!ok) return;

    var btn = form.querySelector(".btn-primary");
    var oldText = btn.textContent;
    btn.disabled = true;
    btn.textContent = "注册中…";

    /* POST 到 RegisterServlet，后端做二次校验后写库 */
    fetch("register", {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: new URLSearchParams(new FormData(form)).toString()
    })
      .then(function (res) { return res.json(); })
      .then(function (data) {
        if (data.success) {
          btn.textContent = "注册成功，跳转中…";
          alert(data.message || "注册成功！");
          window.location.href = data.redirect || "login.html";
        } else {
          btn.disabled = false;
          btn.textContent = oldText;
          // 后端错误就地展示（如用户名已存在）
          var msg = data.message || "注册失败";
          if (msg.indexOf("用户名") >= 0) {
            setError(username, msg);
          } else if (msg.indexOf("邮箱") >= 0) {
            setError(email, msg);
          } else if (msg.indexOf("密码") >= 0 || msg.indexOf("一致") >= 0) {
            setError(confirm, msg);
          } else if (msg.indexOf("手机") >= 0) {
            setError(phone, msg);
          } else {
            setError(username, msg);
          }
        }
      })
      .catch(function (err) {
        btn.disabled = false;
        btn.textContent = oldText;
        setError(username, "网络异常，请确认服务已启动：" + err.message);
      });
  });
})();
