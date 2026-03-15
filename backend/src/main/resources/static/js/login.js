function initLogin() {

    $("#loginForm").on("submit", function (event) {
        event.preventDefault();

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

                localStorage.setItem("token", response.token);
                localStorage.setItem("role", response.role);

                // load dashboard inside SPA
                showNavbar();
                loadPage("dashboard.html", initDashboard);
            },

            error: function () {
                $("#errorMessage")
                    .removeClass("d-none")
                    .text("Invalid username or password");
            }
        });
    });

}