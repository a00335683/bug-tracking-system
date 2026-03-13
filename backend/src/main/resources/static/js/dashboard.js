function initDashboard() {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    $.ajax({
        url: "/api/projects",
        type: "GET",
        headers: getAuthHeaders(),
        success: function (projects) {
            $("#projectCount").text(projects.length);
        },
        error: function () {
            logout();
        }
    });

    $.ajax({
        url: "/api/issues",
        type: "GET",
        headers: getAuthHeaders(),
        success: function (issues) {
            $("#issueCount").text(issues.length);
        },
        error: function () {
            logout();
        }
    });
}