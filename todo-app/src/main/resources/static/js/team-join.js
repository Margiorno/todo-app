document.getElementById("joinTeamBtn").addEventListener("click", function() {
    const code = document.getElementById("code").value;

    fetch('/teams/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ code: code })
    })
        .then(response => {
            if (response.ok) {
                sessionStorage.setItem("teamJoinSuccessMessage", "Successfully joined team .");
                window.location.reload();
            } else {
                return response.text().then(errorMessage => {
                    sessionStorage.setItem("teamJoinErrorMessage", errorMessage);
                    window.location.reload();
                });
            }
        })
        .catch(error => {
            console.error('Network or server error:', error);
            sessionStorage.setItem("teamJoinErrorMessage", "Connection refused.");
            window.location.reload();
        });
});


document.addEventListener("DOMContentLoaded", function() {
    const notificationContainer = document.getElementById("notification-container");

    const successMessage = sessionStorage.getItem("teamJoinSuccessMessage");
    const errorMessage = sessionStorage.getItem("teamJoinErrorMessage");

    if (successMessage) {
        notificationContainer.innerHTML = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <strong>Sukces!</strong> ${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;

        sessionStorage.removeItem("teamJoinSuccessMessage");

    } else if (errorMessage) {
        notificationContainer.innerHTML = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>Error!</strong> ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        sessionStorage.removeItem("teamJoinErrorMessage");
    }
});
