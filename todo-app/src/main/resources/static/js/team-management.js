document.addEventListener('DOMContentLoaded', () => {
    setupCreateTeam();
    setupInviteCodeModal();
    setupJoinTeam();
    setupNotifications();
    setupManageMembers();
});

/* --- HELPERS --- */
async function apiPost(url, body, isFormData = false) {
    const options = { method: 'POST', body: isFormData ? body : JSON.stringify(body) };
    if (!isFormData) options.headers = { 'Content-Type': 'application/json' };
    const response = await fetch(url, options);
    if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || `HTTP error ${response.status}`);
    }
    const text = await response.text();
    return text ? JSON.parse(text) : {};
}

function createAlert(type, strongText, message) {
    return `<div class="alert alert-${type} alert-dismissible fade show" role="alert">
                <strong>${strongText}</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>`;
}

/* --- CREATE TEAM --- */
function setupCreateTeam() {
    const form = document.getElementById('createTeamForm');
    if (!form) return;

    const errorMsg = document.getElementById('createTeamError');
    const modalEl = document.getElementById('createTeamModal');
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);

    form.addEventListener('submit', async e => {
        e.preventDefault();
        errorMsg.textContent = '';
        errorMsg.classList.add('d-none');
        try {
            const data = await apiPost(form.action, new FormData(form), true);
            modal.hide();
            sessionStorage.setItem("successMessage", `Team "${data.name}" created successfully.`);
            location.reload();
        } catch (err) {
            console.error('Create team error:', err);
            errorMsg.textContent = err.message || 'Could not create team.';
            errorMsg.classList.remove('d-none');
        }
    });
}

/* --- INVITE CODE MODAL --- */
function setupInviteCodeModal() {
    const modalEl = document.getElementById('invitationCodeModal');
    if (!modalEl) return;
    const modal = new bootstrap.Modal(modalEl);

    document.body.addEventListener('click', async (e) => {
        if (e.target.id !== 'generateInviteCodeBtn') return;

        const generateBtn = e.target;
        const teamId = generateBtn.dataset.teamId;
        const codeDisplay = document.getElementById('invitationCodeDisplay');
        const errorMsg = document.getElementById('inviteError');

        if (!teamId || !codeDisplay || !errorMsg) return;

        codeDisplay.value = 'Generating...';
        errorMsg.classList.add('d-none');
        modal.show();

        try {
            const data = await apiPost(`/teams/${teamId}/generate-invite-code`);
            codeDisplay.value = data.code;
        } catch (err) {
            console.error('Invite code error:', err);
            codeDisplay.value = 'Error!';
            errorMsg.classList.remove('d-none');
        }
    });

    const copyBtn = document.getElementById('copyInviteCodeBtn');
    if (copyBtn) {
        copyBtn.addEventListener('click', () => {
            const codeDisplay = document.getElementById('invitationCodeDisplay');
            navigator.clipboard.writeText(codeDisplay.value).then(() => {
                const original = copyBtn.textContent;
                copyBtn.textContent = 'Copied!';
                setTimeout(() => (copyBtn.textContent = original), 2000);
            });
        });
    }
}

/* --- JOIN TEAM --- */
function setupJoinTeam() {
    const joinBtn = document.getElementById("joinTeamBtn");
    if (!joinBtn) return;

    joinBtn.addEventListener("click", async () => {
        const code = document.getElementById("code").value;
        try {
            await apiPost('/teams/join', { code });
            sessionStorage.setItem("successMessage", "Successfully joined team.");
        } catch (err) {
            sessionStorage.setItem("errorMessage", err.message || "Could not join team.");
        }
        location.reload();
    });
}

/* --- NOTIFICATIONS --- */
function setupNotifications() {
    const container = document.getElementById("notification-container");
    if (!container) return;

    const success = sessionStorage.getItem("successMessage");
    const error = sessionStorage.getItem("errorMessage");

    if (success) {
        container.innerHTML = createAlert('success', 'Success!', success);
        sessionStorage.removeItem("successMessage");
    } else if (error) {
        container.innerHTML = createAlert('danger', 'Error!', error);
        sessionStorage.removeItem("errorMessage");
    }
}

/* --- MANAGE MEMBERS --- */
function setupManageMembers() {
    const modal = document.getElementById('manageMembersModal');
    if (!modal) return;

    modal.addEventListener('click', async e => {
        if (!e.target.classList.contains('remove-member-btn')) return;
        e.preventDefault();

        const btn = e.target;
        const memberId = btn.dataset.memberId;
        const teamId = btn.dataset.teamId;
        const email = btn.closest('tr').querySelector('td:nth-child(2)').textContent;

        if (confirm(`Are you sure you want to remove ${email} from the team?`)) {
            try {
                await apiPost(`/teams/${teamId}/delete-member?userId=${memberId}`);
                sessionStorage.setItem("successMessage", "Member removed successfully.");
            } catch (err) {
                sessionStorage.setItem("errorMessage", err.message || "Could not remove member.");
            }
            location.reload();
        }
    });
}