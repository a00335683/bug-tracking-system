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

function showNavbar() {
    $(".navbar").show();
}

function hideNavbar() {
    $(".navbar").hide();
}

$(document).ready(function () {
    const token = getToken();

    if (!token) {
        hideNavbar();
        loadPage("login.html", initLogin);
    } else {
        showNavbar();
        loadPage("dashboard.html", initDashboard);
    }

    $("#navDashboard").on("click", function () {
        showNavbar();
        loadPage("dashboard.html", initDashboard);
    });

    $("#navIssues").on("click", function () {
        showNavbar();
        loadPage("issues.html", initIssues);
    });

    $("#navProjects").on("click", function () {
        showNavbar();
        loadPage("projects.html", initProjects);
    });

    $(document).on("click", "#goIssuesBtn", function () {
        showNavbar();
        loadPage("issues.html", initIssues);
    });

    $(document).on("click", "#showGraphBtn", function () {
        showNavbar();
        loadPage("visuals.html", initVisuals);
    });

    $(document).on("click", "#backToDashboardBtn", function () {
        showNavbar();
        loadPage("dashboard.html", initDashboard);
    });
});