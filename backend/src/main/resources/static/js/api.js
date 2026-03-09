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
    window.location.href = "login.html";
}