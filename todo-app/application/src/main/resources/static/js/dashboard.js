document.addEventListener('DOMContentLoaded', () => {
    const state = {
        currentView: 'all', selectedTeamId: null, selectedTeamName: 'My Tasks', selectedScope: 'USER_TASKS',
        centerDate: formatDateISO(new Date()),
        filters: { priority: '', status: '', startDate: '', endDate: '' },
        teams: [], scopes: [], priorities: [], statuses: []
    };

    function formatDateISO(date) { return date.toISOString().split('T')[0]; }
    function addDays(dateStr, days) {
        const date = new Date(dateStr);
        date.setDate(date.getDate() + days);
        return formatDateISO(date);
    }

    const api = {
        get: async (url) => {
            const res = await fetch(url);
            if (!res.ok) { const errText = await res.text(); throw new Error(errText || `HTTP ${res.status} for URL: ${url}`); }
            const text = await res.text();
            return text ? JSON.parse(text) : {};
        },
        getContext: () => Promise.all([
            api.get('/teams/all'), api.get('/task/scopes'),
            api.get('/task/priorities'), api.get('/task/statuses')
        ]),
        getTasks: (params) => api.get(`/task?${params.toString()}`),
        getTeamMembers: (teamId) => api.get(`/teams/${teamId}/members`)
    };

    const render = {
        _findAndRender: (selector, fn) => { const el = document.querySelector(selector); if (el) fn(el); },
        tasks: (tasks) => {
            render._findAndRender('#task-table-body', (el) => {
                if (!tasks || tasks.length === 0) { el.innerHTML = '<tr><td colspan="9" class="text-center p-4">No tasks found.</td></tr>'; return; }
                el.innerHTML = tasks.map(task => {
                    const assignees = task.assignees?.map(u => `${u.firstName} ${u.lastName}`).join(', ');
                    return `<tr>
                        <td class="ps-3">${task.id.substring(0, 8)}...</td>
                        <td>${task.title}</td><td>${assignees || 'N/A'}</td>
                        <td><span class="badge bg-light text-dark">${task.priority}</span></td>
                        <td><span class="badge bg-primary">${task.status}</span></td>
                        <td>${task.taskDate || 'N/A'}</td><td>${task.startTime || ''} - ${task.endTime || ''}</td>
                        <td class="text-end pe-3"><button class="btn btn-sm btn-outline-primary task-details-btn" data-bs-toggle="modal" data-bs-target="#taskDetailsModal" data-task-id="${task.id}">details</button></td>
                    </tr>`;
                }).join('');
            });
        },
        teams: () => {
            render._findAndRender('#teams-list', (el) => {
                el.innerHTML = state.teams.map(team => `<a href="#" class="nav-link team-link" data-team-id="${team.id}" data-team-name="${team.name}">${team.name}</a>`).join('') + '<a href="#" class="nav-link team-link active" data-team-id="">-- My Tasks --</a>';
            });
        },
        teamMembers: (members) => {
            render._findAndRender('#team-members-list', (el) => {
                el.innerHTML = '';
                if (!members || members.length === 0) { el.innerHTML = '<tr><td colspan="3">No members found.</td></tr>'; return; }
                el.innerHTML = members.map(member => `<tr>
                        <td>${member.firstName} ${member.lastName}</td>
                        <td>${member.email}</td>
                        <td><button class="btn btn-danger btn-sm remove-member-btn" data-member-id="${member.id}" data-team-id="${state.selectedTeamId}">Remove</button></td>
                    </tr>`).join('');
            });
        },
        scopeButtons: () => {
            render._findAndRender('#scope-selection-container', (el) => {
                if (state.selectedTeamId) {
                    const buttonsHtml = state.scopes.map(scope =>
                        `<button class="btn btn-sm scope-btn ${scope === state.selectedScope ? 'btn-primary' : 'btn-outline-primary'}" data-scope="${scope}">${scope}</button>`
                    ).join('');

                    el.innerHTML = `
                        <div class="d-flex align-items-center">
                            <span class="me-3">Task Scope:</span>
                            <div class="btn-group" role="group">
                                ${buttonsHtml}
                            </div>
                        </div>
                    `;
                }
                else {
                    el.innerHTML = '';
                }
            });
        },
        calendarNav: () => {
            render._findAndRender('#calendar-nav-container', (el) => {
                if (state.currentView !== 'calendar') { el.innerHTML = ''; return; }
                let html = `<div class="card mb-4"><div class="card-body"><div class="d-flex justify-content-center align-items-center flex-wrap"><button class="btn btn-outline-secondary calendar-nav-btn" data-days="-1">&lt;</button><div class="btn-group mx-2">`;
                for (let i = -2; i <= 2; i++) { const date = addDays(state.centerDate, i); html += `<button class="btn calendar-nav-btn ${i === 0 ? 'btn-primary' : 'btn-outline-primary'}" data-date="${date}">${new Date(date).toLocaleDateString(undefined, { day: '2-digit', month: '2-digit' })}</button>`; }
                html += `</div><button class="btn btn-outline-secondary calendar-nav-btn" data-days="1">&gt;</button></div></div></div>`;
                el.innerHTML = html;
            });
        },
        filterForm: () => {
            render._findAndRender('#filter-nav-container', (el) => {
                if (state.currentView !== 'filter') { el.innerHTML = ''; return; }
                el.innerHTML = `<div class="card mb-4"><div class="card-body"><form id="filter-form" class="row g-3 align-items-end">
                        <div class="col-md-2"><label class="form-label">Priority</label><select name="priority" class="form-select filter-input"><option value="">All</option>${state.priorities.map(p => `<option value="${p}">${p}</option>`).join('')}</select></div>
                        <div class="col-md-2"><label class="form-label">Status</label><select name="status" class="form-select filter-input"><option value="">All</option>${state.statuses.map(s => `<option value="${s}">${s}</option>`).join('')}</select></div>
                        <div class="col-md-3"><label class="form-label">Start Date</label><input type="date" name="startDate" class="form-control filter-input"></div>
                        <div class="col-md-3"><label class="form-label">End Date</label><input type="date" name="endDate" class="form-control filter-input"></div>
                        <div class="col-md-2"><button type="submit" class="btn btn-primary w-100">Apply</button></div></form></div></div>`;
            });
        },
        taskFormOptions: () => {
            render._findAndRender('#taskFormModal #priority', (el) => {
                el.innerHTML = state.priorities
                    .map(p => `<option value="${p}">${p}</option>`)
                    .join('');
            });

            render._findAndRender('#taskFormModal #status', (el) => {
                el.innerHTML = state.statuses
                    .map(s => `<option value="${s}">${s}</option>`)
                    .join('');
            });
        },
        updateActiveStates: () => {
            document.querySelectorAll('#view-selector .nav-link').forEach(el => el.classList.toggle('active', el.dataset.view === state.currentView));
            document.querySelectorAll('#teams-list .nav-link').forEach(el => el.classList.toggle('active', el.dataset.teamId === (state.selectedTeamId || '')));
        },
        teamManagement: () => {
            render._findAndRender('#team-management-container', (el) => {
                if (state.selectedTeamId) {
                    el.innerHTML = `<div class="card mb-4 border-primary"><div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <h3 class="h5 mb-0">Team: ${state.selectedTeamName}</h3><button class="btn-close btn-close-white team-close-btn" aria-label="Close team view"></button>
                        </div><div class="card-body bg-light"><button class="btn btn-sm btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#manageMembersModal">Manage members</button></div></div>`;
                } else el.innerHTML = '';
            });
        },
        all: () => {
            render.updateActiveStates(); render.teamManagement(); render.scopeButtons(); render.calendarNav(); render.filterForm();
            render._findAndRender('#task-list-header', el => { el.textContent = state.selectedTeamId ? `Tasks for: ${state.selectedTeamName}` : 'My personal tasks'; });
            render._findAndRender('#add-new-task-btn', el => { if (state.selectedTeamId) el.setAttribute('data-team-id', state.selectedTeamId); else el.removeAttribute('data-team-id'); });
        }
    };

    async function fetchAndRenderTasks() {
        render._findAndRender('#task-table-body', el => { el.innerHTML = '<tr><td colspan="9" class="text-center p-4">Loading tasks...</td></tr>'; });
        const params = new URLSearchParams();
        if (state.selectedTeamId) { params.append('teamId', state.selectedTeamId); params.append('scope', state.selectedScope); }
        if (state.currentView === 'calendar') params.append('date', state.centerDate);
        if (state.currentView === 'filter') { Object.entries(state.filters).forEach(([k, v]) => v && params.append(k, v)); }
        try { const tasks = await api.getTasks(params); render.tasks(tasks); }
        catch (err) { console.error("Failed to fetch tasks:", err); render._findAndRender('#task-table-body', el => { el.innerHTML = '<tr><td colspan="9" class="text-center p-4 text-danger">Error loading tasks.</td></tr>'; }); }
    }

    function handleEvent(e) {
        document.querySelector('#notification-container')?.remove();

        const target = e.target;
        const viewLink = target.closest('.view-link'), teamLink = target.closest('.team-link'), scopeBtn = target.closest('.scope-btn'),
            calendarBtn = target.closest('.calendar-nav-btn'), teamCloseBtn = target.closest('.team-close-btn'),
            filterForm = target.closest('#filter-form');

        let needsUiUpdate = true, needsTaskFetch = true;

        if (viewLink) { e.preventDefault(); state.currentView = viewLink.dataset.view; }
        else if (teamLink) { e.preventDefault(); state.selectedTeamId = teamLink.dataset.teamId || null; state.selectedTeamName = teamLink.dataset.teamName || 'My Tasks'; }
        else if (scopeBtn) { state.selectedScope = scopeBtn.dataset.scope; }
        else if (calendarBtn) { if (calendarBtn.dataset.date) state.centerDate = calendarBtn.dataset.date; if (calendarBtn.dataset.days) state.centerDate = addDays(state.centerDate, parseInt(calendarBtn.dataset.days)); }
        else if (teamCloseBtn) { state.selectedTeamId = null; state.selectedTeamName = 'My Tasks'; }
        else if (filterForm && e.type === 'submit') { e.preventDefault(); state.filters = Object.fromEntries(new FormData(filterForm).entries()); }
        else { needsUiUpdate = false; }

        if (needsUiUpdate) { render.all(); if (needsTaskFetch) fetchAndRenderTasks(); }
    }

    document.body.addEventListener('click', handleEvent);
    document.body.addEventListener('submit', handleEvent);

    const manageMembersModal = document.getElementById('manageMembersModal');
    if(manageMembersModal) {
        manageMembersModal.addEventListener('show.bs.modal', async () => {
            if (state.selectedTeamId) {
                const generateBtn = manageMembersModal.querySelector('#generateInviteCodeBtn');
                if (generateBtn) generateBtn.setAttribute('data-team-id', state.selectedTeamId);
                try { const members = await api.getTeamMembers(state.selectedTeamId); render.teamMembers(members); }
                catch(error) { console.error("Could not fetch team members:", error); render.teamMembers(null); }
            }
        });
    }

    async function initializeDashboard() {
        try {
            ['#filter-nav-container', '#calendar-nav-container', '#team-management-container'].forEach(selector => {
                if (!document.querySelector(selector)) {
                    const el = document.createElement('div'); el.id = selector.substring(1);
                    document.querySelector('.main-content')?.prepend(el);
                }
            });
            const [teams, scopes, priorities, statuses] = await api.getContext();
            state.teams = teams; state.scopes = scopes; state.priorities = priorities; state.statuses = statuses;
            render.teams();
            render.taskFormOptions();
            render.all();
            await fetchAndRenderTasks();
        } catch (err) {
            console.error("Failed to initialize dashboard:", err);
            const main = document.querySelector('.main-content');
            if (main) main.innerHTML = `<div class="alert alert-danger">Failed to load dashboard data. Please refresh the page.</div>`;
        }
    }

    initializeDashboard();
});