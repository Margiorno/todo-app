document.addEventListener('DOMContentLoaded', function () {
    const invitationCodeModalElement = document.getElementById('invitationCodeModal');
    const invitationCodeModal = new bootstrap.Modal(invitationCodeModalElement);

    const generateBtn = document.getElementById('generateInviteCodeBtn');
    const codeDisplay = document.getElementById('invitationCodeDisplay');
    const copyBtn = document.getElementById('copyInviteCodeBtn');
    const errorMsg = document.getElementById('inviteError');

    generateBtn.addEventListener('click', function () {
        const teamId = this.getAttribute('data-team-id');

        codeDisplay.value = 'Generating...';
        errorMsg.classList.add('d-none');
        invitationCodeModal.show();

        fetch(`/teams/${teamId}/generate-invite-code`, {
            method: 'POST',
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
            .then(data => {
                if (data && data.code) {
                    codeDisplay.value = data.code;
                } else {
                    throw new Error('Invalid data format from server');
                }
            })
            .catch(error => {
                console.error('Error generating invitation code:', error);
                codeDisplay.value = 'Error!';
                errorMsg.classList.remove('d-none');
            });
    });

    copyBtn.addEventListener('click', function () {
        navigator.clipboard.writeText(codeDisplay.value).then(() => {
            const originalText = copyBtn.textContent;
            copyBtn.textContent = 'Copied!';
            setTimeout(() => {
                copyBtn.textContent = originalText;
            }, 2000);
        }).catch(err => {
            console.error('Failed to copy text: ', err);
        });
    });
});

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


function removeMember(teamId, memberId) {
    fetch(`/teams/${teamId}/delete-member?userId=${memberId}`, {
        method: 'POST'
    })
        .then(response => {
            if (response.ok) {
                sessionStorage.setItem("teamManagementSuccessMessage", "Member removed successfully.");
                window.location.reload();
            } else {
                return response.text().then(errorMessage => {
                    const message = errorMessage || "Could not remove the member. Please try again.";
                    sessionStorage.setItem("teamManagementErrorMessage", message);
                    window.location.reload();
                });
            }
        })
        .catch(error => {
            console.error('Network or server error:', error);
            sessionStorage.setItem("teamManagementErrorMessage", "Connection refused. Could not remove member.");
            window.location.reload();
        });
}





document.addEventListener("DOMContentLoaded", function() {

    const notificationContainer = document.getElementById("notification-container");

    const successMessage = sessionStorage.getItem("teamJoinSuccessMessage") || sessionStorage.getItem("teamManagementSuccessMessage");
    const errorMessage = sessionStorage.getItem("teamJoinErrorMessage") || sessionStorage.getItem("teamManagementErrorMessage");

    if (successMessage) {
        notificationContainer.innerHTML = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <strong>Sukces!</strong> ${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;

        sessionStorage.removeItem("teamJoinSuccessMessage");
        sessionStorage.removeItem("teamManagementSuccessMessage");

    } else if (errorMessage) {
        notificationContainer.innerHTML = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>Error!</strong> ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        sessionStorage.removeItem("teamJoinErrorMessage");
        sessionStorage.removeItem("teamManagementErrorMessage");
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const manageMembersModal = document.getElementById('manageMembersModal');

    if (manageMembersModal) {
        manageMembersModal.addEventListener('click', function (event) {

            if (event.target.classList.contains('remove-member-btn')) {
                event.preventDefault();

                const button = event.target;
                const memberId = button.dataset.memberId;
                const teamId = button.dataset.teamId;

                const memberEmail = button.closest('tr').querySelector('td:nth-child(2)').textContent;

                if (confirm(`Are you sure you want to remove ${memberEmail} from the team?`)) {
                    removeMember(teamId, memberId);
                }
            }
        });
    }
});


