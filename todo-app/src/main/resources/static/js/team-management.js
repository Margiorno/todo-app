document.addEventListener('DOMContentLoaded', function () {
    setupInviteCodeModal();
    setupJoinTeam();
    setupNotifications();
    setupManageMembers();
});

function setupInviteCodeModal() {
    const modalEl = document.getElementById('invitationCodeModal');
    const modal = new bootstrap.Modal(modalEl);
    const generateBtn = document.getElementById('generateInviteCodeBtn');
    const codeDisplay = document.getElementById('invitationCodeDisplay');
    const copyBtn = document.getElementById('copyInviteCodeBtn');
    const errorMsg = document.getElementById('inviteError');

    generateBtn.addEventListener('click', () => {
        const teamId = generateBtn.getAttribute('data-team-id');
        codeDisplay.value = 'Generating...';
        errorMsg.classList.add('d-none');
        modal.show();

        fetch(`/teams/${teamId}/generate-invite-code`, { method: 'POST' })
            .then(res => res.ok ? res.json() : Promise.reject('Network error'))
            .then(data => {
                if (data?.code) {
                    codeDisplay.value = data.code;
                } else {
                    throw new Error('Invalid server response');
                }
            })
            .catch(err => {
                console.error('Invite code error:', err);
                codeDisplay.value = 'Error!';
                errorMsg.classList.remove('d-none');
            });
    });

    copyBtn.addEventListener('click', () => {
        navigator.clipboard.writeText(codeDisplay.value)
            .then(() => {
                const originalText = copyBtn.textContent;
                copyBtn.textContent = 'Copied!';
                setTimeout(() => copyBtn.textContent = originalText, 2000);
            })
            .catch(err => console.error('Copy failed:', err));
    });
}

function setupJoinTeam() {
    const joinBtn = document.getElementById("joinTeamBtn");
    joinBtn.addEventListener("click", () => {
        const code = document.getElementById("code").value;

        fetch('/teams/join', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code })
        })
            .then(res => {
                if (res.ok) {
                    sessionStorage.setItem("teamJoinSuccessMessage", "Successfully joined team.");
                    location.reload();
                } else {
                    return res.text().then(msg => {
                        sessionStorage.setItem("teamJoinErrorMessage", msg);
                        location.reload();
                    });
                }
            })
            .catch(err => {
                console.error('Join error:', err);
                sessionStorage.setItem("teamJoinErrorMessage", "Connection refused.");
                location.reload();
            });
    });
}

function setupNotifications() {
    const container = document.getElementById("notification-container");
    const success = sessionStorage.getItem("teamJoinSuccessMessage") || sessionStorage.getItem("teamManagementSuccessMessage");
    const error = sessionStorage.getItem("teamJoinErrorMessage") || sessionStorage.getItem("teamManagementErrorMessage");

    if (success) {
        container.innerHTML = createAlert('success', 'Sukces!', success);
        sessionStorage.removeItem("teamJoinSuccessMessage");
        sessionStorage.removeItem("teamManagementSuccessMessage");
    } else if (error) {
        container.innerHTML = createAlert('danger', 'Error!', error);
        sessionStorage.removeItem("teamJoinErrorMessage");
        sessionStorage.removeItem("teamManagementErrorMessage");
    }
}

function createAlert(type, strongText, message) {
    return `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            <strong>${strongText}</strong> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}

function setupManageMembers() {
    const modal = document.getElementById('manageMembersModal');

    if (!modal) return;

    modal.addEventListener('click', (e) => {
        if (!e.target.classList.contains('remove-member-btn')) return;

        e.preventDefault();
        const btn = e.target;
        const memberId = btn.dataset.memberId;
        const teamId = btn.dataset.teamId;
        const email = btn.closest('tr').querySelector('td:nth-child(2)').textContent;

        if (confirm(`Are you sure you want to remove ${email} from the team?`)) {
            removeMember(teamId, memberId);
        }
    });
}

function removeMember(teamId, memberId) {
    fetch(`/teams/${teamId}/delete-member?userId=${memberId}`, {
        method: 'POST'
    })
        .then(res => {
            if (res.ok) {
                sessionStorage.setItem("teamManagementSuccessMessage", "Member removed successfully.");
            } else {
                return res.text().then(msg => {
                    sessionStorage.setItem("teamManagementErrorMessage", msg || "Could not remove the member. Please try again.");
                });
            }
        })
        .catch(err => {
            console.error('Remove error:', err);
            sessionStorage.setItem("teamManagementErrorMessage", "Connection refused. Could not remove member.");
        })
        .finally(() => location.reload());
}
