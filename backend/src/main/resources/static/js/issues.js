$(document).ready(function () {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    $.ajax({
        url: "/api/issues",
        type: "GET",
        headers: getAuthHeaders(),
        success: function (issues) {
            const tableBody = $("#issuesTable tbody");
            tableBody.empty();

            issues.forEach(function (issue) {
                const row = `
                    <tr>
                        <td>${issue.id}</td>
                        <td>${issue.title}</td>
                        <td>${issue.status}</td>
                        <td>${issue.priority}</td>
                        <td>${issue.projectId}</td>
                        <td>${issue.assignedToId ?? "-"}</td>
                    </tr>
                `;
                tableBody.append(row);
            });
        },
        error: function () {
            logout();
        }
    });
});