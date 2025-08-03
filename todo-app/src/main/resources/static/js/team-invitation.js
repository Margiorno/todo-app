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