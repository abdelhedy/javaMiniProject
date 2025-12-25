class TimelineVisualizer {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.tasks = [];
        this.members = new Map();
        this.startDate = null;
        this.endDate = null;
    }

    setData(tasks, project) {
        this.tasks = tasks;
        this.startDate = new Date(project.startDate);
        this.endDate = new Date(project.deadline);
        
        this.members.clear();
        tasks.forEach(task => {
            if (task.assignedMember && task.assignedMember.id) {
                const memberId = task.assignedMember.id;
                if (!this.members.has(memberId)) {
                    this.members.set(memberId, {
                        id: memberId,
                        name: task.assignedMember.name,
                        tasks: []
                    });
                }
                this.members.get(memberId).tasks.push(task);
            }
        });
        
        // Add unassigned tasks
        const unassignedTasks = tasks.filter(t => !t.assignedMember || !t.assignedMember.id);
        if (unassignedTasks.length > 0) {
            this.members.set(0, {
                id: 0,
                name: 'Unassigned',
                tasks: unassignedTasks
            });
        }
    }

    render() {
        if (this.tasks.length === 0) {
            this.container.innerHTML = '<div class="empty-state"><i class="fas fa-calendar-alt"></i><p>No tasks to display</p></div>';
            return;
        }

        const totalDays = Math.ceil((this.endDate - this.startDate) / (1000 * 60 * 60 * 24));
        
        let html = '<div class="timeline-content">';
        
        html += '<div class="timeline-header">';
        html += `<div class="timeline-label">Team Member</div>`;
        html += '<div class="timeline-dates">';
        html += `<span>${this.formatDate(this.startDate)}</span>`;
        html += `<span>${this.formatDate(this.endDate)}</span>`;
        html += '</div>';
        html += '</div>';
        
        this.members.forEach((member, memberId) => {
            html += this.renderMemberTimeline(member, totalDays);
        });
        
        html += '</div>';
        this.container.innerHTML = html;
    }

    renderMemberTimeline(member, totalDays) {
        let html = '<div class="timeline-row">';
        html += `<div class="timeline-label">${member.name}</div>`;
        html += '<div class="timeline-bars">';
        
        member.tasks.forEach(task => {
            if (task.deadline) {
                const taskStart = task.startDate ? new Date(task.startDate) : this.startDate;
                const taskEnd = new Date(task.deadline);
                
                const startOffset = Math.max(0, (taskStart - this.startDate) / (1000 * 60 * 60 * 24));
                const duration = (taskEnd - taskStart) / (1000 * 60 * 60 * 24);
                
                const leftPercent = (startOffset / totalDays) * 100;
                const widthPercent = Math.max(1, (duration / totalDays) * 100);
                
                html += `<div class="timeline-bar ${task.status.toLowerCase()}" 
                    style="left: ${leftPercent}%; width: ${widthPercent}%;"
                    title="${task.title}\nDuration: ${task.estimatedHours}h\nStatus: ${task.status}\nDeadline: ${taskEnd.toLocaleDateString()}">
                    <span>${task.title}</span>
                </div>`;
            }
        });
        
        html += '</div>';
        html += '</div>';
        
        return html;
    }

    formatDate(date) {
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }
}

let timelineVisualizer = null;

function initTimeline() {
    timelineVisualizer = new TimelineVisualizer('timelineContainer');
}

async function loadTimeline() {
    const select = document.getElementById('timelineProjectSelect');
    const projectId = parseInt(select.value);
    
    if (!projectId) {
        document.getElementById('timelineContainer').innerHTML = 
            '<div class="empty-state"><i class="fas fa-calendar-alt"></i><p>Please select a project</p></div>';
        return;
    }
    
    try {
        const project = await ProjectsAPI.getById(projectId);
        const tasks = await ProjectsAPI.getTasks(projectId);
        
        if (!timelineVisualizer) {
            initTimeline();
        }
        
        timelineVisualizer.setData(tasks, project);
        timelineVisualizer.render();
    } catch (error) {
        console.error('Error loading timeline:', error);
        showNotification('Error loading timeline', 'error');
    }
}

async function populateTimelineProjectSelect() {
    try {
        const projects = await ProjectsAPI.getAll();
        const select = document.getElementById('timelineProjectSelect');
        
        select.innerHTML = '<option value="">Select a project</option>';
        projects.forEach(project => {
            const option = document.createElement('option');
            option.value = project.id;
            option.textContent = project.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading projects:', error);
    }
}
