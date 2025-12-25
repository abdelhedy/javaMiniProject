// Global state
let currentPage = 'dashboard';
let allSkills = [];
let currentProject = null;

// Initialize app
document.addEventListener('DOMContentLoaded', async () => {
    await loadSkills();
    await loadDashboard();
    await loadAlertCount();
    setInterval(loadAlertCount, 30000);
});

// Page Navigation
function showPage(pageName) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    
    document.getElementById(pageName).classList.add('active');
    
    document.querySelectorAll('.nav-menu a').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
    
    currentPage = pageName;
    
    switch(pageName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'members':
            loadMembers();
            break;
        case 'projects':
            loadProjects();
            break;
        case 'timeline':
            populateTimelineProjectSelect();
            break;
        case 'statistics':
            loadStatistics();
            break;
        case 'alerts':
            loadAlerts();
            break;
    }
}

// Dashboard Functions
async function loadDashboard() {
    try {
        const stats = await StatisticsAPI.getOverall();
        const workloadStats = await StatisticsAPI.getWorkload();
        const projects = await ProjectsAPI.getAll();
        
        document.getElementById('totalProjects').textContent = stats.totalProjects || 0;
        document.getElementById('totalMembers').textContent = stats.totalMembers || 0;
        document.getElementById('totalTasks').textContent = stats.totalTasks || 0;
        document.getElementById('overloadedMembers').textContent = workloadStats.overloadedMembers || 0;
        
        const projectsWithTasks = await Promise.all(
            projects.slice(0, 5).map(async (project) => {
                try {
                    const tasks = await ProjectsAPI.getTasks(project.id);
                    return { ...project, tasks };
                } catch (error) {
                    console.error(`Error loading tasks for project ${project.id}:`, error);
                    return { ...project, tasks: [] };
                }
            })
        );
        
        displayRecentProjects(projectsWithTasks);
        displayWorkloadOverview(workloadStats.memberWorkloads || []);
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

function displayRecentProjects(projects) {
    const container = document.getElementById('recentProjects');
    
    if (projects.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>No projects yet</p></div>';
        return;
    }
    
    let html = '<div class="task-list">';
    projects.forEach(project => {
        const tasks = project.tasks || [];
        const taskCount = tasks.length;
        const completedCount = tasks.filter(t => t.status === 'COMPLETED').length;
        const completion = taskCount > 0 ? (completedCount / taskCount * 100) : 0;
        
        html += `
            <div class="task-item project-clickable" onclick="viewProjectDetails(${project.id})" title="Click to view project details">
                <div class="task-info">
                    <h4>${project.name} <i class="fas fa-arrow-right" style="font-size: 0.8rem; color: #999; margin-left: 0.5rem;"></i></h4>
                    <p>${taskCount} task${taskCount !== 1 ? 's' : ''} • ${completion.toFixed(0)}% complete</p>
                </div>
                <span class="status-badge ${project.status.toLowerCase().replace('_', '-')}">${project.status}</span>
            </div>
        `;
    });
    html += '</div>';
    
    container.innerHTML = html;
}

function displayWorkloadOverview(memberWorkloads) {
    const container = document.getElementById('workloadOverview');
    
    if (memberWorkloads.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>No team members yet</p></div>';
        return;
    }
    
    let html = '';
    memberWorkloads.forEach(member => {
        let percentage = member.workloadPercentage || 0;
        if (percentage === 0 && member.weeklyAvailability > 0) {
            percentage = (member.currentWorkload / member.weeklyAvailability) * 100;
        }
        
        let progressClass = '';
        if (percentage > 100) {
            progressClass = 'danger';
        } else if (percentage >= 90) {
            progressClass = 'warning';
        } else if (percentage >= 75) {
            progressClass = 'high';
        } else if (percentage >= 50) {
            progressClass = 'normal';
        } else {
            progressClass = 'low';
        }
        
        html += `
            <div class="workload-item">
                <div class="workload-header">
                    <span>${member.name}</span>
                    <span class="workload-percentage">${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h (${percentage.toFixed(0)}%)</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill ${progressClass}" style="width: ${Math.min(percentage, 100)}%"></div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

// Utility Functions
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

function showNotification(message, type = 'info') {
    const icon = type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ';
    alert(`${icon} ${message}`);
}

window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('active');
    }
}
