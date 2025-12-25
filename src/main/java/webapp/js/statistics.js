async function loadStatistics() {
    try {
        const workloadStats = await StatisticsAPI.getWorkload();
        displayWorkloadStatistics(workloadStats);
        
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
        
        displayProjectStatistics(projectsWithTasks);
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

function displayWorkloadStatistics(stats) {
    const container = document.getElementById('workloadChart');
    
    let html = '<h3>Team Workload Distribution</h3>';
    html += `<p>Average Workload: ${(stats.averageWorkloadPercentage || 0).toFixed(1)}%</p>`;
    html += `<p>Team Utilization: ${(stats.utilizationPercentage || 0).toFixed(1)}%</p>`;
    html += '<div style="margin-top: 1rem;">';
    
    if (stats.memberWorkloads) {
        stats.memberWorkloads.forEach(member => {
            const percentage = member.workloadPercentage || 0;
            const progressClass = percentage > 100 ? 'danger' : percentage > 80 ? 'warning' : '';
            
            html += `
                <div class="workload-item">
                    <div class="workload-header">
                        <span>${member.name}</span>
                        <span class="workload-percentage">
                            ${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h 
                            (${percentage.toFixed(1)}%)
                        </span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${progressClass}" style="width: ${Math.min(percentage, 100)}%"></div>
                    </div>
                    <p style="font-size: 0.9rem; color: #666; margin-top: 0.25rem;">
                        ${member.taskCount} tasks • ${member.availableHours.toFixed(1)}h available
                    </p>
                </div>
            `;
        });
    }
    
    html += '</div>';
    container.innerHTML = html;
}

function displayProjectStatistics(projects) {
    const container = document.getElementById('progressChart');
    
    let html = '<h3>Project Progress Overview</h3>';
    
    if (projects.length === 0) {
        html += '<p>No projects to display</p>';
    } else {
        projects.forEach(project => {
            const taskCount = project.tasks ? project.tasks.length : 0;
            const completedTasks = project.tasks ? project.tasks.filter(t => t.status === 'COMPLETED').length : 0;
            const completion = taskCount > 0 ? (completedTasks / taskCount * 100) : 0;
            
            html += `
                <div class="workload-item">
                    <div class="workload-header">
                        <span>${project.name}</span>
                        <span class="workload-percentage">${completedTasks}/${taskCount} tasks (${completion.toFixed(1)}%)</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${completion}%"></div>
                    </div>
                </div>
            `;
        });
    }
    
    container.innerHTML = html;
}
