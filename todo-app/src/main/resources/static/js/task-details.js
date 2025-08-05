document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('taskDetailsModal');
    if (!modal) return;

    const modalBody = modal.querySelector('.modal-body');
    const editButton = document.getElementById('editTaskBtn');

    modal.addEventListener('show.bs.modal', async (event) => {
        const triggerButton = event.relatedTarget;
        const taskId = triggerButton?.getAttribute('data-task-id');
        if (!taskId) return;

        modalBody.innerHTML = '<p>Loading...</p>';
        if (editButton) {
            editButton.setAttribute('data-task-id', taskId);
        }

        try {
            const response = await fetch(`/task/${taskId}`);
            if (!response.ok) throw new Error(`HTTP error ${response.status}`);
            const task = await response.json();
            modalBody.innerHTML = buildTaskDetailsHtml(task);
        } catch (error) {
            console.error('Failed to load task details:', error);
            modalBody.innerHTML = '<p class="text-danger">Could not load task details.</p>';
        }
    });

    function buildTaskDetailsHtml(task) {
        const {
            title, description, priority, status, taskDate,
            startTime, endTime, team, assignees
        } = task;

        const assigneesHtml = (assignees?.length)
            ? assignees.map(a => a.id).join(', ')
            : '-';

        const teamHtml = team
            ? `<dt class="col-sm-3">Team</dt><dd class="col-sm-9">${team.name}</dd>`
            : '';

        return `
            <dl class="row mb-0">
                <dt class="col-sm-3">Title</dt><dd class="col-sm-9">${title}</dd>
                <dt class="col-sm-3">Description</dt><dd class="col-sm-9">${description || '-'}</dd>
                <dt class="col-sm-3">Priority</dt><dd class="col-sm-9">${priority}</dd>
                <dt class="col-sm-3">Status</dt><dd class="col-sm-9">${status}</dd>
                <dt class="col-sm-3">Date</dt><dd class="col-sm-9">${taskDate}</dd>
                <dt class="col-sm-3">Hours</dt><dd class="col-sm-9">${startTime} – ${endTime}</dd>
                ${teamHtml}
                <dt class="col-sm-3">Assignees</dt><dd class="col-sm-9">${assigneesHtml}</dd>
            </dl>
        `;
    }
});
