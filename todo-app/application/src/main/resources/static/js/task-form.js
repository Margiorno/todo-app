document.addEventListener('DOMContentLoaded', () => {
    const elements = {
        modalForm: document.getElementById('taskFormModal'),
        modalDetails: document.getElementById('taskDetailsModal'),
        form: document.getElementById('taskForm'),
        saveBtn: document.getElementById('saveTaskBtn'),
        titleInput: document.getElementById('title'),
        titleError: document.getElementById('error-title'),
        statusWrapper: document.getElementById('status-field-wrapper'),
        modalLabel: document.getElementById('taskFormModalLabel'),
        startSlider: document.getElementById('startRange'),
        endSlider: document.getElementById('endRange'),
        startTimeInput: document.getElementById('startTimeInput'),
        endTimeInput: document.getElementById('endTimeInput'),
        startMinusBtn: document.getElementById('startMinusBtn'),
        startPlusBtn: document.getElementById('startPlusBtn'),
        endMinusBtn: document.getElementById('endMinusBtn'),
        endPlusBtn: document.getElementById('endPlusBtn'),
    };

    const formatTime = (minutes) => `${String(Math.floor(minutes / 60)).padStart(2, '0')}:${String(minutes % 60).padStart(2, '0')}`;
    const timeToMinutes = (time) => {
        const [h, m] = (time || '00:00').split(':').map(Number);
        return h * 60 + m;
    };

    const updateTimeFromSliders = () => {
        let s = parseInt(elements.startSlider.value, 10);
        let e = parseInt(elements.endSlider.value, 10);
        if (s > e) [s, e] = [e, s];

        elements.startTimeInput.value = formatTime(s);
        elements.endTimeInput.value = formatTime(e);
    };

    const updateSlidersFromInputs = () => {
        let s = timeToMinutes(elements.startTimeInput.value);
        let e = timeToMinutes(elements.endTimeInput.value);
        if (s > e) e = s;

        elements.startSlider.value = s;
        elements.endSlider.value = e;
        elements.endTimeInput.value = formatTime(e);
    };

    const adjustTime = (input, delta) => {
        let mins = Math.max(0, Math.min(1439, timeToMinutes(input.value) + delta));
        input.value = formatTime(mins);
        updateSlidersFromInputs();
    };

    const initTimeSliders = () => updateSlidersFromInputs();

    elements.modalForm.addEventListener('show.bs.modal', async (event) => {
        resetForm();
        const triggerBtn = event.relatedTarget;
        const taskId = triggerBtn?.getAttribute('data-task-id');
        const teamId = triggerBtn?.getAttribute('data-team-id');

        document.getElementById('form-team-id').value = teamId || '';
        const modal = bootstrap.Modal.getInstance(elements.modalDetails);
        if (modal) modal.hide();

        if (taskId) {
            // Edit mode
            elements.modalLabel.textContent = 'Edit Task';
            elements.saveBtn.textContent = 'Save Changes';
            elements.statusWrapper.style.display = 'block';
            await loadTaskData(taskId);
        } else {
            // Add mode
            elements.modalLabel.textContent = 'Add New Task';
            elements.saveBtn.textContent = 'Add Task';
            elements.statusWrapper.style.display = 'none';

            document.getElementById('taskDate').value = new Date().toISOString().split('T')[0];
            elements.startTimeInput.value = "09:00";
            elements.endTimeInput.value = "10:00";
            initTimeSliders();
        }
    });

    const resetForm = () => {
        elements.form.reset();
        document.getElementById('form-task-id').value = '';
        document.getElementById('form-team-id').value = '';
        elements.form.querySelectorAll('.text-danger').forEach(el => el.textContent = '');
    };

    const loadTaskData = async (taskId) => {
        try {
            const res = await fetch(`/task/${taskId}`);
            const task = await res.json();

            document.getElementById('form-task-id').value = task.id;
            elements.titleInput.value = task.title;
            document.getElementById('description').value = task.description;
            document.getElementById('priority').value = task.priority;
            document.getElementById('status').value = task.status;
            document.getElementById('taskDate').value = task.taskDate;

            elements.startTimeInput.value = task.startTime;
            elements.endTimeInput.value = task.endTime;
            if (task.team) document.getElementById('form-team-id').value = task.team.id;

            initTimeSliders();
        } catch (err) {
            console.error('Error loading task:', err);
        }
    };

    elements.saveBtn.addEventListener('click', async () => {
        elements.form.querySelectorAll('.text-danger').forEach(el => el.textContent = '');

        if (!elements.titleInput.value.trim()) {
            elements.titleError.textContent = 'Title cannot be empty.';
            return;
        }

        const taskId = document.getElementById('form-task-id').value;
        const teamId = document.getElementById('form-team-id').value;
        const isEdit = !!taskId;

        let url = isEdit ? `/task/update/${taskId}` : '/task/new';
        if (teamId) url += `?team=${teamId}`;

        const method = isEdit ? 'PUT' : 'POST';
        const formData = new FormData(elements.form);
        const taskData = Object.fromEntries(formData.entries());

        try {
            const response = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(taskData)
            });

            if (response.ok) {
                window.location.reload();
            } else if (response.status === 400) {
                const errors = await response.json();
                for (const field in errors) {
                    const errorEl = document.getElementById(`error-${field}`);
                    if (errorEl) errorEl.textContent = errors[field];
                }
            } else {
                throw new Error('Server error');
            }
        } catch (err) {
            console.error('Error saving task:', err);
        }
    });

    elements.startSlider.addEventListener('input', updateTimeFromSliders);
    elements.endSlider.addEventListener('input', updateTimeFromSliders);
    elements.startTimeInput.addEventListener('change', updateSlidersFromInputs);
    elements.endTimeInput.addEventListener('change', updateSlidersFromInputs);

    elements.startMinusBtn.addEventListener('click', () => adjustTime(elements.startTimeInput, -1));
    elements.startPlusBtn.addEventListener('click', () => adjustTime(elements.startTimeInput, 1));
    elements.endMinusBtn.addEventListener('click', () => adjustTime(elements.endTimeInput, -1));
    elements.endPlusBtn.addEventListener('click', () => adjustTime(elements.endTimeInput, 1));
});
