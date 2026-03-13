function initProjects() {
    const token = getToken();

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const role = localStorage.getItem("role");

    if (role !== "ADMIN") {
        $("#createProjectBtn").hide();
    }

    function showMessage(message, type) {
        const box = $("#projectMessage");
        box.removeClass("d-none alert-success alert-danger")
            .addClass("alert-" + type)
            .text(message);

        setTimeout(function () {
            box.addClass("d-none").text("");
        }, 3000);
    }

    function buildActionButton(project) {
        if (role !== "ADMIN") {
            return `<span class="text-muted">Not allowed</span>`;
        }

        if (project.status === "ACTIVE") {
            return `
                <button class="btn btn-sm btn-warning archive-btn" data-id="${project.id}">
                    <i class="bi bi-archive"></i> Archive
                </button>
            `;
        }

        if (project.status === "ARCHIVED") {
            return `
                <button class="btn btn-sm btn-success reactivate-btn" data-id="${project.id}">
                    <i class="bi bi-arrow-clockwise"></i> Reactivate
                </button>
            `;
        }

        return `<span class="text-muted">-</span>`;
    }

    function buildRow(project) {
        return `
            <tr>
                <td>${project.id}</td>
                <td>${project.name}</td>
                <td>${project.description}</td>
                <td>${project.status}</td>
                <td>${project.createdAt ? new Date(project.createdAt).toLocaleString() : "-"}</td>
                <td>${buildActionButton(project)}</td>
            </tr>
        `;
    }

    function initializeProjectsTable() {
        $("#projectsTable").DataTable({
            destroy: true,
            columnDefs: [
                { targets: 0, searchable: false },
                { targets: 4, searchable: false },
                { targets: 5, searchable: false, orderable: false }
            ]
        });
    }

    function loadProjects() {
        if ($.fn.DataTable.isDataTable("#projectsTable")) {
            $("#projectsTable").DataTable().destroy();
        }

        const tableBody = $("#projectsTable tbody");
        tableBody.empty();

        $.ajax({
            url: "/api/projects",
            type: "GET",
            headers: getAuthHeaders(),

            success: function (projects) {
                tableBody.empty();

                if (!projects || projects.length === 0) {
                    tableBody.append(`
                        <tr>
                            <td colspan="6" class="text-center">No projects found</td>
                        </tr>
                    `);
                    return;
                }

                projects.forEach(function (project) {
                    tableBody.append(buildRow(project));
                });

                initializeProjectsTable();
            },

            error: function () {
                logout();
            }
        });
    }

    loadProjects();

    $(document).off("submit", "#createProjectForm").on("submit", "#createProjectForm", function (event) {
        event.preventDefault();

        const name = $("#projectName").val().trim();
        const description = $("#projectDescription").val().trim();

        if (!name || !description) {
            showMessage("Please fill in all fields.", "danger");
            return;
        }

        $.ajax({
            url: "/api/projects",
            type: "POST",
            headers: {
                ...getAuthHeaders(),
                "Content-Type": "application/json"
            },
            data: JSON.stringify({
                name: name,
                description: description
            }),

            success: function () {
                $("#createProjectForm")[0].reset();

                const modalElement = document.getElementById("createProjectModal");
                const modal = bootstrap.Modal.getInstance(modalElement);
                if (modal) {
                    modal.hide();
                }

                showMessage("Project created successfully.", "success");
                loadProjects();
            },

            error: function (xhr) {
                let message = "Failed to create project.";

                if (xhr.responseJSON && xhr.responseJSON.error) {
                    message = xhr.responseJSON.error;
                }

                showMessage(message, "danger");
            }
        });
    });

    $(document).off("click", ".archive-btn").on("click", ".archive-btn", function () {
        const projectId = $(this).data("id");

        $.ajax({
            url: "/api/projects/" + projectId + "/archive",
            type: "PUT",
            headers: getAuthHeaders(),

            success: function () {
                showMessage("Project archived successfully.", "success");
                loadProjects();
            },

            error: function (xhr) {
                let message = "Failed to archive project.";

                if (xhr.responseJSON && xhr.responseJSON.error) {
                    message = xhr.responseJSON.error;
                }

                showMessage(message, "danger");
            }
        });
    });

    $(document).off("click", ".reactivate-btn").on("click", ".reactivate-btn", function () {
        const projectId = $(this).data("id");

        $.ajax({
            url: "/api/projects/" + projectId + "/reactivate",
            type: "PUT",
            headers: getAuthHeaders(),

            success: function () {
                showMessage("Project reactivated successfully.", "success");
                loadProjects();
            },

            error: function (xhr) {
                let message = "Failed to reactivate project.";

                if (xhr.responseJSON && xhr.responseJSON.error) {
                    message = xhr.responseJSON.error;
                }

                showMessage(message, "danger");
            }
        });
    });
}