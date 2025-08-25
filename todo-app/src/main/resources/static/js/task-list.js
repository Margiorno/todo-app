async function fetchTasks(teamId, scope) {
    let url = `/task/get`;
    if (teamId) {
        url += `?team=${teamId}&scope=${scope || 'USER_TASKS'}`;
    }

    const response = await fetch(url);
    if (!response.ok) throw new Error('Failed to fetch tasks');
    return await response.json();
}

async function renderTasks(teamId, scope) {
    const tasks = await fetchTasks(teamId, scope);
    const tbody = document.querySelector('#tasks-table-body');
    tbody.innerHTML = ''; // clear old rows

    if (!tasks || tasks.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center p-4">Tasks not found</td></tr>`;
        return;
    }

    tasks.forEach(task => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${task.id}</td>
            <td>${task.title}</td>
            <td>${task.assignees ? task.assignees.join(', ') : ''}</td>
            <td>${task.priority}</td>
            <td>${task.status}</td>
            <td>${task.taskDate}</td>
            <td>${task.startTime} - ${task.endTime}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-primary" data-task-id="${task.id}" data-bs-toggle="modal" data-bs-target="#taskDetailsModal">details</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

renderTasks(null, 'USER_TASKS');
