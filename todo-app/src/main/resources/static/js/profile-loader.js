document.addEventListener('DOMContentLoaded', function () {
    const container = document.querySelector('.main-page-container');
    const profileId = container.dataset.profileId;

    if (profileId) {
        fetch(`/users/${profileId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(user => {
                document.getElementById('display-value-email').textContent = user.email || '';
                document.getElementById('display-value-firstName').textContent = user.firstName || '';
                document.getElementById('display-value-lastName').textContent = user.lastName || '';
                document.getElementById('display-value-dateOfBirth').textContent = user.dateOfBirth || '';
                document.getElementById('display-value-gender').textContent = user.gender || '';

                const profilePicture = document.getElementById('profile-picture');
                profilePicture.src = user.profilePicturePath
                    ? `/files/profile-pictures/${user.profilePicturePath}`
                    : '/files/profile-pictures/default.png';

                const addFriendBtn = document.getElementById('add-friend-btn');
                if (addFriendBtn) {
                    addFriendBtn.dataset.profileId = user.id;
                }

                const removeFriendBtn = document.getElementById('remove-friend-btn');
                if (removeFriendBtn) {
                    removeFriendBtn.dataset.friendId = user.id;
                }
            })
            .catch(error => {
                console.error('Error fetching user data:', error);
                const mainContent = document.querySelector('.main-content');
                if (mainContent) {
                    mainContent.innerHTML = '<p class="text-danger text-center">Could not load user profile.</p>';
                }
            });
    }
});