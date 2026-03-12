$(document).ready(function () {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const role = localStorage.getItem("role");

    function loadDevelopers() {
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
    }

    if (role !== "ADMIN") {
        $("#assignHeader").remove();
    }

    if (role !== "TESTER") {
        $("#createIssueBtn").hide();
    }

    loadDevelopers();

    function buildRow(issue) {
        let statusButton = "";
        let canUpdateStatus = false;

        if (role === "DEVELOPER" && (issue.status === "OPEN" || issue.status === "IN_PROGRESS")) {
            canUpdateStatus = true;
        }

        if (role === "TESTER" && issue.status === "RESOLVED") {
            canUpdateStatus = true;
        }

        if (role === "ADMIN" && issue.status === "VERIFIED") {
            canUpdateStatus = true;
        }

        if (canUpdateStatus) {
            statusButton = `<button class="btn btn-sm btn-warning status-btn" data-id="${issue.id}" data-status="${issue.status}">Update Status</button>`;
        } else {
            statusButton = `<span class="text-muted">Not allowed</span>`;
        }

        return `
            <tr>
                <td>${issue.id}</td>
                <td>${issue.title}</td>
                <td>${issue.status}</td>
                <td>${issue.priority}</td>
                <td>${issue.projectId}</td>
                <td>${issue.assignedToUsername ?? "-"}</td>
                ${role === "ADMIN"
            ? `<td><button class="btn btn-sm btn-primary assign-btn" data-id="${issue.id}">Assign</button></td>`
            : ""}
                <td>${statusButton}</td>
            </tr>
        `;
    }

    function initializeTable() {
        const options = {
            destroy: true,
            columnDefs: []
        };

        if (role === "ADMIN") {
            options.columnDefs = [
                { targets: 0, searchable: false }, // ID
                { targets: 4, searchable: false }, // Project ID
                { targets: 5, searchable: false }, // Assigned To
                { targets: 6, searchable: false, orderable: false }, // Assign
                { targets: 7, searchable: false, orderable: false }  // Update Status
            ];
        } else {
            options.columnDefs = [
                { targets: 0, searchable: false }, // ID
                { targets: 4, searchable: false }, // Project ID
                { targets: 5, searchable: false }, // Assigned To
                { targets: 6, searchable: false, orderable: false }  // Update Status
            ];
        }

        $("#issuesTable").DataTable(options);
    }

    function loadIssues(url) {
        if ($.fn.DataTable.isDataTable("#issuesTable")) {
            $("#issuesTable").DataTable().destroy();
        }

        const tableBody = $("#issuesTable tbody");
        tableBody.empty();

        $.ajax({
            url: url,
            type: "GET",
            headers: getAuthHeaders(),
            success: function (issues) {
                tableBody.empty();

                if (!issues || issues.length === 0) {
                    const colspan = role === "ADMIN" ? 8 : 7;
                    tableBody.append(`
                        <tr>
                            <td colspan="${colspan}" class="text-center">No issues found</td>
                        </tr>
                    `);
                    return;
                }

                issues.forEach(function (issue) {
                    tableBody.append(buildRow(issue));
                });

                initializeTable();
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

        const statusSelect = $("#newStatus");
        statusSelect.empty();
        $("#resolutionNoteGroup").addClass("d-none");
        $("#resolutionNote").val("");

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

        if (!developerId) {
            alert("Please select a developer.");
            return;
        }

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

                alert("Issue assigned successfully.");
                loadIssues("/api/issues");
            },
            error: function (xhr) {
                let message = "Failed to assign issue.";

                if (xhr.responseJSON && xhr.responseJSON.error) {
                    message = xhr.responseJSON.error;
                }

                alert(message);
            }
        });
    });

    $("#updateStatusForm").on("submit", function (event) {
        event.preventDefault();

        const issueId = $("#statusIssueId").val();
        const newStatus = $("#newStatus").val();
        const resolutionNote = $("#resolutionNote").val().trim();

        if (!newStatus) {
            alert("Please select a status.");
            return;
        }

        if (newStatus === "RESOLVED" && !resolutionNote) {
            alert("Please enter a resolution note.");
            return;
        }

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

                alert("Issue status updated successfully.");
                loadIssues("/api/issues");
            },
            error: function (xhr) {
                let message = "Failed to update status.";

                if (xhr.responseJSON && xhr.responseJSON.error) {
                    message = xhr.responseJSON.error;
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
            title: $("#issueTitle").val().trim(),
            description: $("#issueDescription").val().trim(),
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

                alert("Issue created successfully.");
                loadIssues("/api/issues");
            },
            error: function (xhr) {
                let message = "Unable to create issue.";

                if (xhr.status === 403) {
                    message = "You are not allowed to create issues.";
                } else if (xhr.responseJSON && xhr.responseJSON.error) {
                    message = xhr.responseJSON.error;
                }

                alert(message);
            }
        });
    });

    $("#filterBtn").on("click", function () {
        const projectId = $("#filterProjectId").val();
        const status = $("#filterStatus").val();
        const priority = $("#filterPriority").val();

        if (!projectId && !status && !priority) {
            loadIssues("/api/issues");
            return;
        }

        const params = new URLSearchParams();

        if (projectId) {
            params.append("projectId", projectId);
        }

        if (status) {
            params.append("status", status);
        }

        if (priority) {
            params.append("priority", priority);
        }

        loadIssues("/api/issues/filter?" + params.toString());
    });
});