document.addEventListener('DOMContentLoaded', function () {

    const taskFormModalEl = document.getElementById('taskFormModal');

    const detailsModalEl = document.getElementById('taskDetailsModal');
    const detailsModal = bootstrap.Modal.getInstance(detailsModalEl);

    const startSlider      = document.getElementById('startRange');
    const endSlider        = document.getElementById('endRange');
    const startTimeInput   = document.getElementById('startTimeInput');
    const endTimeInput     = document.getElementById('endTimeInput');
    const startMinusBtn    = document.getElementById('startMinusBtn');
    const startPlusBtn     = document.getElementById('startPlusBtn');
    const endMinusBtn      = document.getElementById('endMinusBtn');
    const endPlusBtn       = document.getElementById('endPlusBtn');

    if (startSlider && endSlider && startTimeInput && endTimeInput) {

        function formatTime(minutes) {
            const h = Math.floor(minutes / 60).toString().padStart(2, '0');
            const m = (minutes % 60).toString().padStart(2, '0');
            return `${h}:${m}`;
        }

        function timeToMinutes(time) {
            if (!time) return 0;
            const [h, m] = time.split(':').map(Number);
            return h * 60 + m;
        }

        function updateFromSliders() {
            let s = parseInt(startSlider.value, 10);
            let e = parseInt(endSlider.value, 10);

            if (s > e) {
                [s, e] = [e, s];
            }

            startTimeInput.value = formatTime(s);
            endTimeInput.value = formatTime(e);
        }

        function updateFromInputs() {
            let s = timeToMinutes(startTimeInput.value);
            let e = timeToMinutes(endTimeInput.value);

            if (s > e) {
                e = s;
                endTimeInput.value = formatTime(e);
            }
            startSlider.value = s;
            endSlider.value = e;
        }

        function adjustTime(inputElem, delta) {
            let mins = timeToMinutes(inputElem.value) + delta;
            mins = Math.max(0, Math.min(1439, mins));
            inputElem.value = formatTime(mins);
            updateFromInputs();
        }

        function initializeTimeSliderState() {
            updateFromInputs();
        }

        startSlider.addEventListener('input', updateFromSliders);
        endSlider.addEventListener('input', updateFromSliders);
        startTimeInput.addEventListener('change', updateFromInputs);
        endTimeInput.addEventListener('change', updateFromInputs);

        startMinusBtn.addEventListener('click', () => adjustTime(startTimeInput, -1));
        startPlusBtn.addEventListener('click',  () => adjustTime(startTimeInput,  1));
        endMinusBtn.addEventListener('click',   () => adjustTime(endTimeInput,   -1));
        endPlusBtn.addEventListener('click',    () => adjustTime(endTimeInput,    1));

        taskFormModalEl.addEventListener('show.bs.modal', function (event) {
            const form = document.getElementById('taskForm');
            form.reset();
            document.getElementById('form-task-id').value = '';
            document.getElementById('form-team-id').value = '';
            document.getElementById('status-field-wrapper').style.display = 'none';
            form.querySelectorAll('.text-danger').forEach(el => el.textContent = '');

            const button = event.relatedTarget;
            const taskId = button.getAttribute('data-task-id');
            const teamId = button.getAttribute('data-team-id');
            const modalLabel = document.getElementById('taskFormModalLabel');
            const saveBtn = document.getElementById('saveTaskBtn');

            if (teamId) document.getElementById('form-team-id').value = teamId;

            if (taskId) {
                modalLabel.textContent = 'Edit Task';
                saveBtn.textContent = 'Save Changes';
                document.getElementById('status-field-wrapper').style.display = 'block';

                fetch(`/task/${taskId}`)
                    .then(response => response.json())
                    .then(task => {
                        document.getElementById('form-task-id').value = task.id;
                        document.getElementById('title').value = task.title;
                        document.getElementById('description').value = task.description;
                        document.getElementById('priority').value = task.priority;
                        document.getElementById('status').value = task.status;
                        document.getElementById('taskDate').value = task.taskDate;

                        startTimeInput.value = task.startTime;
                        endTimeInput.value = task.endTime;
                        if (task.team) document.getElementById('form-team-id').value = task.team.id;

                        initializeTimeSliderState();
                    });
                if (detailsModal) detailsModal.hide();
            } else {
                modalLabel.textContent = 'Add New Task';
                saveBtn.textContent = 'Add Task';

                document.getElementById('taskDate').value = new Date().toISOString().split('T')[0]; [2]

                startTimeInput.value = "09:00";
                endTimeInput.value = "10:00";

                initializeTimeSliderState();
            }
        });
    }

    document.getElementById('saveTaskBtn').addEventListener('click', function() {
        const form = document.getElementById('taskForm');
        const titleInput = document.getElementById('title');
        const titleErrorElement = document.getElementById('error-title');

        form.querySelectorAll('.text-danger').forEach(el => el.textContent = '');

        if (titleInput.value.trim() === '') {
            titleErrorElement.textContent = 'Title cannot be empty.';
            return;
        }

        const taskId = document.getElementById('form-task-id').value;
        const teamId = document.getElementById('form-team-id').value;
        const isEditMode = !!taskId;
        let url = isEditMode ? `/task/update/${taskId}` : '/task/new';
        if (teamId) { url += `?team=${teamId}`; }
        const method = isEditMode ? 'PUT' : 'POST';
        const formData = new FormData(form);
        const taskData = Object.fromEntries(formData.entries());

        fetch(url, { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(taskData)})
            .then(response => {
                if (response.ok) {
                    window.location.reload();
                } else if (response.status === 400) {
                    return response.json().then(errors => {
                        for (const field in errors) {
                            const errorElement = document.getElementById(`error-${field}`);
                            if (errorElement) errorElement.textContent = errors[field];
                        }
                    });
                } else {
                    throw new Error('Server error');
                }
            }).catch(error => console.error('Error saving task:', error));
    })});