document.addEventListener('DOMContentLoaded', function() {
    const cropImageModalEl = document.getElementById('cropImageModal');
    const cropImageModal = new bootstrap.Modal(cropImageModalEl);
    const imageToCrop = document.getElementById('imageToCrop');
    const fileInput = document.getElementById('file');
    const cropAndUploadBtn = document.getElementById('cropAndUploadBtn');
    const status = document.getElementById('upload-status');
    const pictureContainer = document.getElementById('picture-container');
    let cropper = null;

    fileInput.addEventListener('change', (e) => {
        const files = e.target.files;
        if (files && files.length > 0) {
            const reader = new FileReader();
            reader.onload = () => {
                imageToCrop.src = reader.result;
                cropImageModal.show();
            };
            reader.readAsDataURL(files[0]);
        }
        e.target.value = '';
    });

    cropImageModalEl.addEventListener('shown.bs.modal', () => {
        cropper = new Cropper(imageToCrop, {
            aspectRatio: 1,
            viewMode: 1,
            dragMode: 'move',
            background: false,
            autoCropArea: 0.8,
        });
    });

    cropImageModalEl.addEventListener('hidden.bs.modal', () => {
        if (cropper) {
            cropper.destroy();
            cropper = null;
        }
    });

    cropAndUploadBtn.addEventListener('click', async () => {
        if (!cropper) return;

        status.textContent = 'Processing...';
        status.className = 'text-muted';

        const canvas = cropper.getCroppedCanvas({
            width: 400,
            height: 400,
            imageSmoothingQuality: 'high',
        });

        canvas.toBlob(async (blob) => {
            const formData = new FormData();
            formData.append('file', blob, 'profile-picture.jpg');

            status.textContent = 'Uploading...';
            cropImageModal.hide();

            try {
                const response = await fetch('/users/profile/avatar', { method: 'POST', body: formData });
                if (!response.ok) {
                    throw new Error(await response.text() || 'Upload failed');
                }
                const result = await response.json();
                const newImageUrl = `/files/profile-pictures/${result.filename}`;

                let existingImg = document.getElementById('profile-picture');
                const cacheBusterUrl = newImageUrl + '?t=' + new Date().getTime();

                if (existingImg) {
                    existingImg.src = cacheBusterUrl;
                } else {
                    pictureContainer.innerHTML = '';
                    const img = document.createElement('img');
                    img.id = 'profile-picture';
                    img.src = cacheBusterUrl;
                    img.alt = 'Profile Picture';
                    pictureContainer.appendChild(img);
                }
                status.textContent = 'Profile picture updated!';
                status.className = 'text-success';
            } catch (error) {
                status.textContent = 'Error uploading picture: ' + error.message;
                status.className = 'text-danger';
            }
        }, 'image/jpeg');
    });
});
