function getToken() {
    return localStorage.getItem("token");
}

function getAuthHeaders() {
    return {
        "Authorization": "Bearer " + getToken()
    };
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");

    hideNavbar();
    loadPage("login.html", initLogin);
}