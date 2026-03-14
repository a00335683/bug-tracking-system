let statusChartInstance = null;
let priorityChartInstance = null;

function initVisuals() {
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
            renderStatusChart(issues);
            renderPriorityChart(issues);
        },
        error: function () {
            logout();
        }
    });
}

function renderStatusChart(issues) {
    const statusCounts = {
        OPEN: 0,
        IN_PROGRESS: 0,
        RESOLVED: 0,
        VERIFIED: 0,
        CLOSED: 0
    };

    issues.forEach(function (issue) {
        if (statusCounts.hasOwnProperty(issue.status)) {
            statusCounts[issue.status]++;
        }
    });

    const ctx = document.getElementById("statusChart");

    if (statusChartInstance) {
        statusChartInstance.destroy();
    }

    statusChartInstance = new Chart(ctx, {
        type: "pie",
        data: {
            labels: ["Open", "In Progress", "Resolved", "Verified", "Closed"],
            datasets: [{
                data: [
                    statusCounts.OPEN,
                    statusCounts.IN_PROGRESS,
                    statusCounts.RESOLVED,
                    statusCounts.VERIFIED,
                    statusCounts.CLOSED
                ],
                backgroundColor: [
                    "#d8b4fe",
                    "#f9a8d4",
                    "#8b1e3f",
                    "#c084fc",
                    "#f472b6"
                ],
                borderColor: "#ffffff",
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: "bottom"
                }
            }
        }
    });
}

function renderPriorityChart(issues) {
    const priorityCounts = {
        LOW: 0,
        MEDIUM: 0,
        HIGH: 0
    };

    issues.forEach(function (issue) {
        if (priorityCounts.hasOwnProperty(issue.priority)) {
            priorityCounts[issue.priority]++;
        }
    });

    const ctx = document.getElementById("priorityChart");

    if (priorityChartInstance) {
        priorityChartInstance.destroy();
    }

    priorityChartInstance = new Chart(ctx, {
        type: "bar",
        data: {
            labels: ["Low", "Medium", "High"],
            datasets: [{
                label: "Issues",
                data: [
                    priorityCounts.LOW,
                    priorityCounts.MEDIUM,
                    priorityCounts.HIGH
                ],
                backgroundColor: [
                    "#d8b4fe",
                    "#f9a8d4",
                    "#8b1e3f"
                ],
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });
}