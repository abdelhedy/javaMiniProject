// ====================================
// MODULE PROJETS & TÂCHES - Personne 3
// ====================================

// Projects Functions
async function loadProjects() {
    try {
        const projects = await ProjectsAPI.getAll();
        
        const projectsWithTasks = await Promise.all(
            projects.map(async (project) => {
                try {
                    const tasks = await ProjectsAPI.getTasks(project.id);
                    return { ...project, tasks };
                } catch (error) {
                    console.error(`Error loading tasks for project ${project.id}:`, error);
                    return { ...project, tasks: [] };
                }
            })
        );
        
        displayProjects(projectsWithTasks);
    } catch (error) {
        console.error('Error loading projects:', error);
        showNotification('Error loading projects', 'error');
    }
}

function displayProjects(projects) {
    const container = document.getElementById('projectsList');
    
    if (projects.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-folder"></i><p>No projects yet. Create your first project!</p></div>';
        return;
    }
    
    let html = '';
    projects.forEach(project => {
        const tasks = project.tasks || [];
        const taskCount = tasks.length;
        const completedTasks = tasks.filter(t => t.status === 'COMPLETED').length;
        const completion = taskCount > 0 ? (completedTasks / taskCount * 100) : 0;
        
        html += `
            <div class="project-card" onclick="viewProjectDetails(${project.id})">
                <div class="project-header">
                    <h3>${project.name}</h3>
                    <span class="status-badge ${project.status.toLowerCase().replace('_', '-')}">${project.status}</span>
                </div>
                <p>${project.description || 'No description'}</p>
                <div class="project-meta">
                    <span><i class="fas fa-calendar"></i> ${project.deadline}</span>
                    <span><i class="fas fa-tasks"></i> ${taskCount} task${taskCount !== 1 ? 's' : ''}</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${completion}%"></div>
                </div>
                <div class="project-actions" onclick="event.stopPropagation()">
                    <button class="btn btn-sm btn-primary" onclick="showAddTaskModalForProject(${project.id})">
                        <i class="fas fa-plus"></i> Add Task
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="allocateProjectTasks(${project.id})">
                        <i class="fas fa-magic"></i> Auto-Allocate
                    </button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function showAddProjectModal() {
    document.getElementById('addProjectModal').classList.add('active');
}

async function addProject(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const project = {
        name: formData.get('name'),
        description: formData.get('description'),
        startDate: formData.get('startDate'),
        deadline: formData.get('deadline'),
        status: 'PLANNING'
    };
    
    try {
        await ProjectsAPI.create(project);
        closeModal('addProjectModal');
        form.reset();
        showNotification('Project created successfully!', 'success');
        loadProjects();
    } catch (error) {
        console.error('Error creating project:', error);
        showNotification('Error creating project: ' + error.message, 'error');
    }
}

async function viewProjectDetails(projectId) {
    try {
        const project = await ProjectsAPI.getById(projectId);
        const tasks = await ProjectsAPI.getTasks(projectId);
        const stats = await StatisticsAPI.getProject(projectId);
        
        displayProjectDetails(project, tasks, stats);
        document.getElementById('projectDetailsModal').classList.add('active');
    } catch (error) {
        console.error('Error loading project details:', error);
        showNotification('Error loading project details', 'error');
    }
}

function displayProjectDetails(project, tasks, stats) {
    const container = document.getElementById('projectDetailsContent');
    
    let html = `
        <h2>${project.name}</h2>
        <p>${project.description || 'No description'}</p>
        
        <div class="project-meta">
            <span><i class="fas fa-calendar-start"></i> Start: ${project.startDate}</span>
            <span><i class="fas fa-calendar-day"></i> Deadline: ${project.deadline}</span>
            <span class="status-badge ${project.status.toLowerCase().replace('_', '-')}">${project.status}</span>
        </div>
        
        <div class="stats-grid" style="margin-top: 1rem;">
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${stats.totalTasks || 0}</h3>
                    <p>Total Tasks</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${stats.completedTasks || 0}</h3>
                    <p>Completed</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${(stats.completionPercentage || 0).toFixed(1)}%</h3>
                    <p>Progress</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${(stats.totalEstimatedHours || 0).toFixed(1)}h</h3>
                    <p>Total Hours</p>
                </div>
            </div>
        </div>
        
        <h3 style="margin-top: 2rem;">Tasks</h3>
        <div class="task-list">
    `;
    
    if (tasks.length === 0) {
        html += '<p>No tasks in this project yet.</p>';
    } else {
        tasks.forEach(task => {
            let statusButtons = '';
            if (task.status === 'TODO' && task.assignedMember && task.assignedMember.id) {
                statusButtons = `<button class="btn btn-small btn-primary" onclick="startTask(${task.id}, ${project.id})" title="Commencer la tâche">
                    <i class="fas fa-play"></i> Commencer
                </button>`;
            } else if (task.status === 'IN_PROGRESS') {
                statusButtons = `<button class="btn btn-small btn-success" onclick="completeTask(${task.id}, ${project.id})" title="Marquer comme terminée">
                    <i class="fas fa-check"></i> Terminer
                </button>`;
            } else if (task.status === 'COMPLETED') {
                statusButtons = `<span class="task-completed-badge">
                    <i class="fas fa-check-circle"></i> Terminée
                </span>`;
            }
            
            let assignButton = '';
            if (task.status === 'TODO' && (!task.assignedMember || !task.assignedMember.id)) {
                assignButton = `<button class="btn btn-small btn-secondary" onclick="showAssignTaskModal(${task.id}, ${project.id})" title="Assigner manuellement">
                    <i class="fas fa-user-plus"></i> Assigner
                </button>`;
            }
            
            let unassignButton = '';
            if (task.status === 'TODO' && task.assignedMember && task.assignedMember.id) {
                unassignButton = `<button class="btn btn-small btn-warning" onclick="unassignTask(${task.id}, ${project.id})" title="Retirer l'assignation">
                    <i class="fas fa-user-minus"></i> Retirer
                </button>`;
            }
            
            html += `
                <div class="task-item">
                    <div class="task-info">
                        <h4>${task.title}</h4>
                        <p>
                            ${task.estimatedHours}h • 
                            ${(task.assignedMember && task.assignedMember.name) || 'Unassigned'} • 
                            <span class="priority-badge ${task.priority}">${task.priority}</span> • 
                            <span class="status-badge ${task.status.toLowerCase().replace('_', '-')}">${task.status}</span>
                        </p>
                    </div>
                    <div class="task-actions">
                        ${assignButton}
                        ${unassignButton}
                        ${statusButtons}
                    </div>
                </div>
            `;
        });
    }
    
    html += '</div>';
    container.innerHTML = html;
}

function showAddTaskModalForProject(projectId) {
    currentProject = projectId;
    document.getElementById('taskProjectId').value = projectId;
    populateSkillsCheckboxes('taskSkillsCheckboxes');
    document.getElementById('addTaskModal').classList.add('active');
}

async function addTask(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const task = {
        projectId: parseInt(formData.get('projectId')),
        title: formData.get('title'),
        description: formData.get('description'),
        estimatedHours: parseFloat(formData.get('estimatedHours')),
        priority: formData.get('priority'),
        deadline: formData.get('deadline') || null,
        requiredSkills: []
    };
    
    for (const skill of allSkills) {
        const checkbox = form.querySelector(`input[name="skill_${skill.id}"]`);
        if (checkbox && checkbox.checked) {
            const level = parseInt(form.querySelector(`select[name="level_${skill.id}"]`).value);
            task.requiredSkills.push({
                skillId: skill.id,
                requiredLevel: level
            });
        }
    }
    
    try {
        await TasksAPI.create(task);
        closeModal('addTaskModal');
        form.reset();
        showNotification('Task added successfully!', 'success');
        loadProjects();
    } catch (error) {
        console.error('Error adding task:', error);
        showNotification('Error adding task: ' + error.message, 'error');
    }
}

async function allocateProjectTasks(projectId) {
    if (!confirm('This will automatically assign all unassigned tasks in this project. Continue?')) {
        return;
    }
    
    try {
        showNotification('Allocating tasks...', 'info');
        const result = await AllocationAPI.allocateTasks(projectId);
        
        if (result.success) {
            showNotification(`Successfully assigned ${result.assignedCount} tasks!`, 'success');
        } else {
            showNotification(`Assigned ${result.assignedCount} tasks, ${result.failedCount} failed.`, 'warning');
        }
        
        loadProjects();
        loadAlerts();
        
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
        
        if (currentPage === 'members') {
            loadMembers();
        }
    } catch (error) {
        console.error('Error allocating tasks:', error);
        showNotification('Error allocating tasks: ' + error.message, 'error');
    }
}

async function showAssignTaskModal(taskId, projectId) {
    try {
        const members = await MembersAPI.getAll();
        const modal = document.getElementById('assignTaskModal');
        const select = document.getElementById('assignMemberId');
        
        select.innerHTML = '<option value="">Select a member</option>';
        members.forEach(member => {
            select.innerHTML += `<option value="${member.id}">${member.name}</option>`;
        });
        
        window.currentTaskToAssign = { taskId, projectId };
        modal.classList.add('active');
    } catch (error) {
        console.error('Error loading members:', error);
        showNotification('Error loading members', 'error');
    }
}

async function assignTask(event) {
    event.preventDefault();
    
    const form = event.target;
    const memberId = parseInt(form.querySelector('#assignMemberId').value);
    
    if (!memberId) {
        showNotification('Please select a member', 'error');
        return;
    }
    
    try {
        await TasksAPI.assign(window.currentTaskToAssign.taskId, memberId);
        closeModal('assignTaskModal');
        showNotification('Task assigned successfully!', 'success');
        
        viewProjectDetails(window.currentTaskToAssign.projectId);
        loadAlerts();
        
        if (currentPage === 'members') {
            loadMembers();
        }
    } catch (error) {
        console.error('Error assigning task:', error);
        showNotification('Error assigning task: ' + error.message, 'error');
    }
}

async function unassignTask(taskId, projectId) {
    if (!confirm('Remove this task assignment?')) {
        return;
    }
    
    try {
        await TasksAPI.unassign(taskId);
        showNotification('Task unassigned successfully!', 'success');
        viewProjectDetails(projectId);
        
        if (currentPage === 'members') {
            loadMembers();
        }
    } catch (error) {
        console.error('Error unassigning task:', error);
        showNotification('Error unassigning task: ' + error.message, 'error');
    }
}

async function startTask(taskId, projectId) {
    try {
        await TasksAPI.updateStatus(taskId, 'IN_PROGRESS');
        showNotification('Task started!', 'success');
        viewProjectDetails(projectId);
    } catch (error) {
        console.error('Error starting task:', error);
        showNotification('Error starting task: ' + error.message, 'error');
    }
}

async function completeTask(taskId, projectId) {
    try {
        await TasksAPI.updateStatus(taskId, 'COMPLETED');
        showNotification('Task completed!', 'success');
        viewProjectDetails(projectId);
        
        if (currentPage === 'members') {
            loadMembers();
        }
    } catch (error) {
        console.error('Error completing task:', error);
        showNotification('Error completing task: ' + error.message, 'error');
    }
}
