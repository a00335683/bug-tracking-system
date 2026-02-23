$(document).ready(function () {

    $("#loginBtn").click(function () {

        const username = $("#username").val();
        const password = $("#password").val();

        $.ajax({
            url: "/api/auth/login",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                username: username,
                password: password
            }),
            success: function (response) {

                // Save token
                // localStorage.setItem("token", response.token);
                localStorage.setItem("token", response);

                // Redirect to dashboard
                window.location.href = "dashboard.html";
            },
            error: function (xhr) {
                $("#errorMessage").text("Invalid username or password");
            }
        });

    });

});