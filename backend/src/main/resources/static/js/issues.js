$(document).ready(function () {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const role = localStorage.getItem("role");

    $.ajax({
        url: "/api/users",
        type: "GET",
        headers: getAuthHeaders(),
        success: function (users) {
            const developerSelect = $("#developerId");
            developerSelect.empty();

            developerSelect.append('<option value="">Select developer</option>');

            users.forEach(function (user) {
                if (user.role === "DEVELOPER") {
                    developerSelect.append(
                        `<option value="${user.id}">${user.username}</option>`
                    );
                }
            });
        },
        error: function () {
            console.log("Failed to load users");
        }
    });

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
                        <td>${issue.assignedToUsername ?? "-"}</td>
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
    $(document).on("click", ".assign-btn", function () {
        const issueId = $(this).data("id");
        $("#assignIssueId").val(issueId);

        const modal = new bootstrap.Modal(document.getElementById("assignIssueModal"));
        modal.show();
    });
    $("#assignIssueForm").on("submit", function (event) {
        event.preventDefault();

        const issueId = $("#assignIssueId").val();
        const developerId = $("#developerId").val();

        $.ajax({
            url: "/api/issues/" + issueId + "/assign",
            type: "PUT",
            headers: {
                ...getAuthHeaders(),
                "Content-Type": "application/json"
            },
            data: JSON.stringify({
                developerId: parseInt(developerId)
            }),
            success: function () {
                $("#assignIssueForm")[0].reset();

                const modalElement = document.getElementById("assignIssueModal");
                const modal = bootstrap.Modal.getInstance(modalElement);
                modal.hide();

                location.reload();
            },
            error: function (xhr) {
                let message = "Failed to assign issue.";

                if (xhr.responseJSON && xhr.responseJSON.description) {
                    message = xhr.responseJSON.description;
                }

                alert(message);
            }
        });
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