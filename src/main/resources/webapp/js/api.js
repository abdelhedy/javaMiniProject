// API Base URL
const API_BASE = '/api';

// API Helper Function
async function apiRequest(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            let errorMessage = 'Request failed';
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorMessage;
            } catch (e) {
                errorMessage = `${response.status} ${response.statusText}`;
            }
            throw new Error(errorMessage);
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Members API
const MembersAPI = {
    getAll: () => apiRequest('/members/'),

    getById: (id) => apiRequest(`/members/${id}`),

    create: (member) => apiRequest('/members/', {
        method: 'POST',
        body: JSON.stringify(member)
    }),

    update: (member) => apiRequest('/members/', {
        method: 'PUT',
        body: JSON.stringify(member)
    }),

    delete: (id) => apiRequest(`/members/${id}`, {
        method: 'DELETE'
    }),

    addSkill: (memberId, skillId, proficiencyLevel) => apiRequest(`/members/${memberId}/skills`, {
        method: 'POST',
        body: JSON.stringify({ skillId, proficiencyLevel })
    }),

    removeSkill: (memberId, skillId) => apiRequest(`/members/${memberId}/skills/${skillId}`, {
        method: 'DELETE'
    })
};

// Projects API
const ProjectsAPI = {
    getAll: () => apiRequest('/projects/'),

    getById: (id) => apiRequest(`/projects/${id}`),

    create: (project) => apiRequest('/projects/', {
        method: 'POST',
        body: JSON.stringify(project)
    }),

    update: (project) => apiRequest('/projects/', {
        method: 'PUT',
        body: JSON.stringify(project)
    }),

    delete: (id) => apiRequest(`/projects/${id}`, {
        method: 'DELETE'
    }),

    getTasks: (projectId) => apiRequest(`/projects/${projectId}/tasks`)
};

// Tasks API
const TasksAPI = {
    getById: (id) => apiRequest(`/tasks/${id}`),

    create: (task) => apiRequest('/tasks/', {
        method: 'POST',
        body: JSON.stringify(task)
    }),

    update: (task) => apiRequest('/tasks/', {
        method: 'PUT',
        body: JSON.stringify(task)
    }),

    delete: (id) => apiRequest(`/tasks/${id}`, {
        method: 'DELETE'
    }),

    // Manual assignment
    assign: (taskId, memberId) => apiRequest(`/tasks/${taskId}/assign`, {
        method: 'POST',
        body: JSON.stringify({ memberId })
    }),

    assignToMember: (taskId, memberId) => apiRequest(`/tasks/${taskId}/assign`, {
        method: 'POST',
        body: JSON.stringify({ memberId })
    }),

    unassign: (taskId) => apiRequest(`/tasks/${taskId}/assign`, {
        method: 'DELETE'
    }),

    updateStatus: (taskId, status) => apiRequest(`/tasks/${taskId}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
    }),

    addSkillRequirement: (taskId, skillId, requiredLevel) => apiRequest(`/tasks/${taskId}/skills`, {
        method: 'POST',
        body: JSON.stringify({ skillId, requiredLevel })
    }),

    addDependency: (taskId, dependsOnTaskId) => apiRequest(`/tasks/${taskId}/dependencies`, {
        method: 'POST',
        body: JSON.stringify({ dependsOnTaskId })
    })
};

// Skills API
const SkillsAPI = {
    getAll: () => apiRequest('/skills/')
};

// Alerts API
const AlertsAPI = {
    getAll: (unreadOnly = false) => apiRequest(`/alerts/${unreadOnly ? '?unread=true' : ''}`),

    getById: (id) => apiRequest(`/alerts/${id}`),

    getUnreadCount: () => apiRequest('/alerts/count'),

    markAsRead: (id) => apiRequest(`/alerts/${id}/read`, {
        method: 'PUT'
    }),

    markAllAsRead: () => apiRequest('/alerts/read-all', {
        method: 'PUT'
    }),

    delete: (id) => apiRequest(`/alerts/${id}`, {
        method: 'DELETE'
    })
};

// Allocation API
const AllocationAPI = {
    allocateTasks: (projectId) => apiRequest(`/allocate/${projectId}`, {
        method: 'POST'
    })
};

// Statistics API
const StatisticsAPI = {
    getOverall: () => apiRequest('/statistics/'),

    getWorkload: () => apiRequest('/statistics/workload'),

    getProject: (projectId) => apiRequest(`/statistics/project/${projectId}`)
};