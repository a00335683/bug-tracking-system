$(document).ready(function () {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const role = localStorage.getItem("role");

    if (role !== "ADMIN") {
        $("#actionHeader").hide();
    }

    if (role !== "TESTER") {
        $("#createIssueBtn").hide();
    }

    $.ajax({
        url: "/api/issues",
        type: "GET",
        headers: getAuthHeaders(),
        success: function (issues) {
            const tableBody = $("#issuesTable tbody");
            tableBody.empty();

            issues.forEach(function (issue) {
                let actionButton = "";

                if (role === "ADMIN") {
                    actionButton = `<button class="btn btn-sm btn-primary assign-btn" data-id="${issue.id}">Assign</button>`;
                }

                const row = `
                    <tr>
                        <td>${issue.id}</td>
                        <td>${issue.title}</td>
                        <td>${issue.status}</td>
                        <td>${issue.priority}</td>
                        <td>${issue.projectId}</td>
                        <td>${issue.assignedToId ?? "-"}</td>
                        <td>${actionButton}</td>
                    </tr>
                `;
                tableBody.append(row);
            });
        },
        error: function () {
            logout();
        }
    });
    $("#createIssueForm").on("submit", function (event) {
        event.preventDefault();

        const issueData = {
            projectId: parseInt($("#projectId").val()),
            reporterId: parseInt($("#reporterId").val()),
            title: $("#issueTitle").val(),
            description: $("#issueDescription").val(),
            priority: $("#issuePriority").val()
        };

        $.ajax({
            url: "/api/issues",
            type: "POST",
            headers: {
                ...getAuthHeaders(),
                "Content-Type": "application/json"
            },
            data: JSON.stringify(issueData),
            success: function () {
                $("#createIssueForm")[0].reset();

                const modalElement = document.getElementById("createIssueModal");
                const modal = bootstrap.Modal.getInstance(modalElement);
                modal.hide();

                location.reload();
            },
            error: function (xhr) {
                let message = "Unable to create issue.";

                if (xhr.status === 403) {
                    message = "You are not allowed to create issues.";
                } else if (xhr.responseJSON && xhr.responseJSON.description) {
                    message = xhr.responseJSON.description;
                }

                alert(message);
            }
        });
    });
});