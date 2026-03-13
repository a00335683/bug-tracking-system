function loadPage(page, callback) {
    $("#app").load("views/" + page, function (response, status) {
        if (status === "error") {
            $("#app").html("<div class='alert alert-danger'>Failed to load page.</div>");
            return;
        }

        if (typeof callback === "function") {
            callback();
        }
    });
}

$(document).ready(function () {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    loadPage("dashboard.html", initDashboard);

    $("#navDashboard").on("click", function () {
        loadPage("dashboard.html", initDashboard);
    });

    $("#navIssues").on("click", function () {
        loadPage("issues.html", initIssues);
    });

    $("#navProjects").on("click", function () {
        loadPage("projects.html", initProjects);
    });

    $(document).on("click", "#goIssuesBtn", function () {
        loadPage("issues.html", initIssues);
    });
});