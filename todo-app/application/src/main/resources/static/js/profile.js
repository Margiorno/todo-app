document.addEventListener('DOMContentLoaded', function () {
    const container = document.querySelector('.main-page-container');
    const profileId = container.dataset.profileId;

    if (!profileId) return;

    const api = {
        fetchUserDetails: () => fetch(`/users/${profileId}`),
        fetchProfileStatus: () => fetch(`/social/${profileId}/status`, { method: 'POST' }),
        fetchGenders: () => fetch('/social/genders')
    };

    const populateUserDetails = (user) => {
        document.getElementById('display-value-email').textContent = user.email || '';
        document.getElementById('display-value-firstName').textContent = user.firstName || '';
        document.getElementById('display-value-lastName').textContent = user.lastName || '';
        document.getElementById('display-value-dateOfBirth').textContent = user.dateOfBirth || '';
        document.getElementById('display-value-gender').textContent = user.gender || '';

        const profilePicture = document.getElementById('profile-picture');
        profilePicture.src = user.profilePicturePath
            ? `/files/profile-pictures/${user.profilePicturePath}`
            : '/files/profile-pictures/default.png';
    };

    const populateGendersDropdown = (genders) => {
        const genderSelect = document.getElementById('input-gender');
        if (!genderSelect) return;
        genderSelect.innerHTML = '';
        genders.forEach(gender => {
            const option = document.createElement('option');
            option.value = gender;
            option.textContent = gender;
            genderSelect.appendChild(option);
        });
    };

    const renderActionButtons = (statusInfo) => {
        const actionsContainer = document.getElementById('profile-actions-container');
        if (!actionsContainer) return;
        actionsContainer.innerHTML = '';
        let buttonsHtml = '';

        switch (statusInfo.status) {
            case 'OWNER':
                buttonsHtml = `
                    <hr>
                    <label for="file" class="btn btn-primary">Change profile picture</label>
                    <input type="file" id="file" name="file" class="d-none" accept="image/*">
                    <p id="upload-status" class="mt-2"></p>
                `;
                document.querySelectorAll('.edit-button').forEach(btn => btn.classList.remove('d-none'));
                break;
            case 'FRIEND':
                buttonsHtml = `<button type="button" id="remove-friend-btn" class="btn btn-danger w-100" data-friend-id="${profileId}">Remove Friend</button>`;
                break;
            case 'INVITATION_SENT':
                buttonsHtml = `
                    <div class="text-muted mb-2"><small>Friend request sent</small></div>
                    <button type="button" class="btn btn-secondary" id="cancel-invitation-btn" data-request-id="${statusInfo.friendRequestId}">Cancel</button>
                `;
                break;
            case 'INVITATION_RECEIVED':
                buttonsHtml = `
                    <div class="text-muted mb-2"><small>Friend request received</small></div>
                    <div class="d-flex justify-content-center gap-2" id="invitation-action-container">
                        <button type="button" class="btn btn-success" id="accept-invitation-btn" data-request-id="${statusInfo.friendRequestId}">Accept</button>
                        <button type="button" class="btn btn-outline-danger" id="decline-invitation-btn" data-request-id="${statusInfo.friendRequestId}">Decline</button>
                    </div>
                `;
                break;
            default: // NOT_FRIEND
                buttonsHtml = `<button type="button" id="add-friend-btn" data-profile-id="${profileId}" class="btn btn-success w-100">Add Friend</button>`;
                break;
        }
        actionsContainer.innerHTML = buttonsHtml;
    };

    let cropper;
    let modal;
    const modalElement = document.getElementById('cropModal');
    const imageToCrop = document.getElementById('image-to-crop');

    if (modalElement) {
        document.addEventListener('change', function (event) {
            if (event.target && event.target.id === 'file') {
                const file = event.target.files[0];
                if (!file) return;

                const reader = new FileReader();
                reader.onload = function (e) {
                    if (!modal) modal = new bootstrap.Modal(modalElement);
                    imageToCrop.src = e.target.result;
                    modal.show();
                    if (cropper) cropper.destroy();
                    cropper = new Cropper(imageToCrop, { aspectRatio: 1, viewMode: 1, background: false });
                };
                reader.readAsDataURL(file);
            }
        });
    }

    document.body.addEventListener('click', async function(e) {
        const target = e.target;

        const handleFriendAction = (url, confirmMsg = null) => {
            if (confirmMsg && !confirm(confirmMsg)) return;
            fetch(url, { method: 'POST' })
                .then(res => res.ok ? window.location.reload() : Promise.reject('Request failed'))
                .catch(err => console.error(err));
        };

        if (target.id === 'add-friend-btn') handleFriendAction(`/social/${target.dataset.profileId}/invite`);
        if (target.id === 'remove-friend-btn') handleFriendAction(`/social/${target.dataset.friendId}/remove`, 'Are you sure?');
        if (target.id === 'cancel-invitation-btn') handleFriendAction(`/social/${target.dataset.requestId}/cancel`);
        if (target.id === 'accept-invitation-btn') handleFriendAction(`/social/${target.dataset.requestId}/accept`);
        if (target.id === 'decline-invitation-btn') handleFriendAction(`/social/${target.dataset.requestId}/decline`);

        if (target.matches('.edit-button')) {
            const field = target.dataset.field;
            document.getElementById(`display-view-${field}`).classList.add('hidden');
            document.getElementById(`edit-view-${field}`).classList.remove('hidden');
            document.getElementById(`input-${field}`).value = document.getElementById(`display-value-${field}`).textContent;
        }
        if (target.matches('.cancel-button')) {
            const field = target.dataset.field;
            document.getElementById(`display-view-${field}`).classList.remove('hidden');
            document.getElementById(`edit-view-${field}`).classList.add('hidden');
        }
        if (target.matches('.save-button')) {
            const field = target.dataset.field;
            const newValue = document.getElementById(`input-${field}`).value;
            const statusSpan = document.getElementById(`status-${field}`);
            statusSpan.textContent = 'Saving...';
            try {
                const response = await fetch('/users/profile/update', {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ [field]: newValue })
                });
                if (!response.ok) throw new Error(await response.text());
                const updatedData = await response.json();
                document.getElementById(`display-value-${field}`).textContent = updatedData[field];
                statusSpan.textContent = 'Saved!';
                setTimeout(() => {
                    document.getElementById(`display-view-${field}`).classList.remove('hidden');
                    document.getElementById(`edit-view-${field}`).classList.add('hidden');
                    statusSpan.textContent = '';
                }, 1500);
            } catch (error) {
                statusSpan.textContent = `Error: ${error.message}`;
            }
        }

        if (target.id === 'crop-button') {
            if (!cropper) return;
            const uploadStatus = document.getElementById('upload-status');
            if (uploadStatus) uploadStatus.textContent = 'Uploading...';

            cropper.getCroppedCanvas({ width: 250, height: 250 }).toBlob(blob => {
                const formData = new FormData();
                formData.append('file', blob, 'profile.jpg');
                fetch('/users/profile/avatar', { method: 'POST', body: formData })
                    .then(res => res.ok ? res.json() : Promise.reject('Upload failed'))
                    .then(data => {
                        document.getElementById('profile-picture').src = `/files/profile-pictures/${data.filename}?t=${new Date().getTime()}`;
                        if (uploadStatus) uploadStatus.textContent = 'Success!';
                        if (modal) modal.hide();
                    })
                    .catch(err => {
                        console.error(err);
                        if (uploadStatus) uploadStatus.textContent = 'Error!';
                    });
            });
        }
    });

    Promise.all([
        api.fetchUserDetails(),
        api.fetchProfileStatus(),
        api.fetchGenders()
    ])
        .then(responses => Promise.all(responses.map(res => res.ok ? res.json() : Promise.reject(res))))
        .then(([user, statusInfo, genders]) => {
            populateUserDetails(user);
            populateGendersDropdown(genders);
            renderActionButtons(statusInfo);
        })
        .catch(error => {
            console.error("Could not load profile data:", error);
            if (container) container.innerHTML = '<p class="text-danger text-center">Could not load user profile data.</p>';
        });
});