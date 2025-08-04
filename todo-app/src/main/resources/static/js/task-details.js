document.addEventListener('DOMContentLoaded', function () {
    const taskDetailsModal = document.getElementById('taskDetailsModal');
    if (taskDetailsModal) {
        taskDetailsModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const taskId = button.getAttribute('data-task-id');

            const modalBody = taskDetailsModal.querySelector('.modal-body');
            const editTaskBtn = document.getElementById('editTaskBtn');

            modalBody.innerHTML = '<p>Loading...</p>';
            editTaskBtn.href = '#';

            fetch(`/task/${taskId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network error');
                    }
                    return response.json();
                })
                .then(task => {
                    let assigneesHtml = '-';
                    if (task.assignees && task.assignees.length > 0) {
                        assigneesHtml = task.assignees.map(a => a.id).join(', ');
                    }

                    const teamHtml = task.team ? `
                        <dt class="col-sm-3">Team</dt>
                        <dd class="col-sm-9">${task.team.name}</dd>` : '';

                    modalBody.innerHTML = `
                        <dl class="row mb-0">
                            <dt class="col-sm-3">Title</dt>
                            <dd class="col-sm-9">${task.title}</dd>

                            <dt class="col-sm-3">Description</dt>
                            <dd class="col-sm-9">${task.description || '-'}</dd>

                            <dt class="col-sm-3">Priority</dt>
                            <dd class="col-sm-9">${task.priority}</dd>

                            <dt class="col-sm-3">Status</dt>
                            <dd class="col-sm-9">${task.status}</dd>

                            <dt class="col-sm-3">Date</dt>
                            <dd class="col-sm-9">${task.date}</dd>

                            <dt class="col-sm-3">Hours</dt>
                            <dd class="col-sm-9">${task.startTime} – ${task.endTime}</dd>
                            
                            <dt class="col-sm-3">Assignees</dt>
                            <dd class="col-sm-9">${assigneesHtml}</dd>
                        </dl>
                    `;

                    const teamParam = task.team ? `&team=${task.team.id}` : '';
                    editTaskBtn.href = `/task/edit/${task.id}?${teamParam}`;
                })
                .catch(error => {
                    modalBody.innerHTML = '<p class="text-danger">Error while downloading tasks.</p>';
                });
        });
    }
});