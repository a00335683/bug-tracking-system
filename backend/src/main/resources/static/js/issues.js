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
        $("#assignHeader").hide();
    }

    if (role !== "TESTER") {
        $("#createIssueBtn").hide();
    }

    function loadIssues(url) {
        $.ajax({
            url: url,
            type: "GET",
            headers: getAuthHeaders(),
            success: function (issues) {
                const tableBody = $("#issuesTable tbody");
                tableBody.empty();

                issues.forEach(function (issue) {
                    let assignButton = "";
                    let statusButton = "";

                    if (role === "ADMIN") {
                        assignButton = `<button class="btn btn-sm btn-primary assign-btn" data-id="${issue.id}">Assign</button>`;
                    }

                    statusButton = `<button class="btn btn-sm btn-warning status-btn" data-id="${issue.id}" data-status="${issue.status}">Update Status</button>`;

                    const row = `
                    <tr>
                        <td>${issue.id}</td>
                        <td>${issue.title}</td>
                        <td>${issue.status}</td>
                        <td>${issue.priority}</td>
                        <td>${issue.projectId}</td>
                        <td>${issue.assignedToUsername ?? "-"}</td>
                        ${role === "ADMIN" ? `<td>${assignButton}</td>` : ""}
                        <td>${statusButton}</td>
                    </tr>
                `;
                    tableBody.append(row);
                });
            },
            error: function () {
                logout();
            }
        });
    }

    loadIssues("/api/issues");

    $(document).on("click", ".assign-btn", function () {
        const issueId = $(this).data("id");
        $("#assignIssueId").val(issueId);

        const modal = new bootstrap.Modal(document.getElementById("assignIssueModal"));
        modal.show();
    });

    $(document).on("click", ".status-btn", function () {
        const issueId = $(this).data("id");
        const currentStatus = $(this).data("status");
        $("#statusIssueId").val(issueId);

        const role = localStorage.getItem("role");
        const statusSelect = $("#newStatus");

        statusSelect.empty();
        $("#resolutionNoteGroup").addClass("d-none");

        if (role === "DEVELOPER") {

            if (currentStatus === "OPEN") {
                statusSelect.append('<option value="IN_PROGRESS">IN_PROGRESS</option>');
            }

            if (currentStatus === "IN_PROGRESS") {
                statusSelect.append('<option value="RESOLVED">RESOLVED</option>');
            }

        }

        if (role === "TESTER" && currentStatus === "RESOLVED") {
            statusSelect.append('<option value="VERIFIED">VERIFIED</option>');
        }

        if (role === "ADMIN" && currentStatus === "VERIFIED") {
            statusSelect.append('<option value="CLOSED">CLOSED</option>');
        }
        $("#resolutionNote").val("");

        if ($("#newStatus").val() === "RESOLVED") {
            $("#resolutionNoteGroup").removeClass("d-none");
        }

        const modal = new bootstrap.Modal(document.getElementById("updateStatusModal"));
        modal.show();
    });
    $(document).on("change", "#newStatus", function () {

        const status = $(this).val();

        if (status === "RESOLVED") {
            $("#resolutionNoteGroup").removeClass("d-none");
        } else {
            $("#resolutionNoteGroup").addClass("d-none");
        }

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
    $("#updateStatusForm").on("submit", function (event) {
        event.preventDefault();

        const issueId = $("#statusIssueId").val();
        const newStatus = $("#newStatus").val();
        const resolutionNote = $("#resolutionNote").val();

        $.ajax({
            url: "/api/issues/" + issueId + "/status",
            type: "PUT",
            headers: {
                ...getAuthHeaders(),
                "Content-Type": "application/json"
            },
            data: JSON.stringify({
                status: newStatus,
                resolutionNote: resolutionNote
            }),
            success: function () {
                $("#updateStatusForm")[0].reset();

                const modalElement = document.getElementById("updateStatusModal");
                const modal = bootstrap.Modal.getInstance(modalElement);
                modal.hide();

                location.reload();
            },
            error: function (xhr) {
                let message = "Failed to update status.";

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

    $("#filterBtn").on("click", function () {

        const projectId = $("#filterProjectId").val();
        const status = $("#filterStatus").val();
        const priority = $("#filterPriority").val();

        let url = "/api/issues/filter?";

        if (projectId) {
            url += "projectId=" + projectId + "&";
        }

        if (status) {
            url += "status=" + status + "&";
        }

        if (priority) {
            url += "priority=" + priority + "&";
        }

        loadIssues(url);

    });
});