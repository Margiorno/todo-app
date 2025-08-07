document.addEventListener('DOMContentLoaded', function() {
    function toggleEditView(field, showEdit) {
        const displayView = document.getElementById(`display-view-${field}`);
        const editView = document.getElementById(`edit-view-${field}`);

        if (showEdit) {
            const displayValue = document.getElementById(`display-value-${field}`).textContent;
            const input = document.getElementById(`input-${field}`);
            if (input.tagName === 'SELECT') {
                for (let i = 0; i < input.options.length; i++) {
                    if (input.options[i].text === displayValue) {
                        input.selectedIndex = i;
                        break;
                    }
                }
            } else {
                input.value = displayValue;
            }
            displayView.classList.add('hidden');
            editView.classList.remove('hidden');
        } else {
            displayView.classList.remove('hidden');
            editView.classList.add('hidden');
        }
    }

    document.querySelector('.profile-container').addEventListener('click', async function(e) {
        if (e.target.matches('.edit-button')) {
            const field = e.target.dataset.field;
            toggleEditView(field, true);
        }

        if (e.target.matches('.cancel-button')) {
            const field = e.target.dataset.field;
            document.getElementById(`status-${field}`).textContent = '';
            toggleEditView(field, false);
        }

        if (e.target.matches('.save-button')) {
            const field = e.target.dataset.field;
            const input = document.getElementById(`input-${field}`);
            const statusSpan = document.getElementById(`status-${field}`);
            const newValue = input.value;

            if (!newValue) {
                statusSpan.textContent = "Value cannot be empty.";
                statusSpan.className = 'update-status text-danger';
                return;
            }

            statusSpan.textContent = 'Saving...';
            statusSpan.className = 'update-status text-muted';

            try {
                const response = await fetch('/users/profile/update', {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ [field]: newValue })
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(errorText || `Could not update the field.`);
                }

                const updatedData = await response.json();

                if (field === 'gender') {
                    document.getElementById(`display-value-${field}`).textContent = input.options[input.selectedIndex].text;
                } else {
                    document.getElementById(`display-value-${field}`).textContent = updatedData[field];
                }

                statusSpan.textContent = 'Saved!';
                statusSpan.className = 'update-status text-success';

                setTimeout(() => {
                    statusSpan.textContent = '';
                    toggleEditView(field, false);
                }, 1500);

            } catch (error) {
                statusSpan.textContent = `Error: ${error.message}`;
                statusSpan.className = 'update-status text-danger';
            }
        }
    });
});
