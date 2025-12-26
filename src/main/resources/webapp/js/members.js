async function loadMembers() {
    try {
        const members = await MembersAPI.getAll();
        displayMembers(members);
    } catch (error) {
        console.error('Error loading members:', error);
        showNotification('Error loading members', 'error');
    }
}

function displayMembers(members) {
    const container = document.getElementById('membersList');
    
    if (members.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-users"></i><p>No team members yet. Add your first member!</p></div>';
        return;
    }
    
    let html = '';
    members.forEach(member => {
        let workloadPercentage = member.workloadPercentage || 0;
        if (workloadPercentage === 0 && member.weeklyAvailability > 0) {
            workloadPercentage = (member.currentWorkload / member.weeklyAvailability) * 100;
        }
        
        let progressClass = '';
        if (workloadPercentage > 100) {
            progressClass = 'danger';
        } else if (workloadPercentage >= 90) {
            progressClass = 'warning';
        } else if (workloadPercentage >= 75) {
            progressClass = 'high';
        } else if (workloadPercentage >= 50) {
            progressClass = 'normal';
        } else {
            progressClass = 'low';
        }
        const initials = member.name.split(' ').map(n => n[0]).join('').toUpperCase();
        
        html += `
            <div class="member-card">
                <div class="member-header">
                    <div class="member-avatar">${initials}</div>
                    <div class="member-info">
                        <h3>${member.name}</h3>
                        <p>${member.email}</p>
                    </div>
                </div>
                <div class="member-stats">
                    <div class="stat-row">
                        <span>Workload:</span>
                        <span>${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h (${workloadPercentage.toFixed(0)}%)</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${progressClass}" style="width: ${Math.min(workloadPercentage, 100)}%"></div>
                    </div>
                </div>
                <div class="skills-tags">
                    ${member.skills.map(skill => `<span class="skill-tag">${skill.skill ? skill.skill.name : 'Unknown'} (${skill.proficiencyLevel})</span>`).join('')}
                </div>
                <div class="member-actions">
                    <button class="btn btn-secondary" onclick="showEditMemberModal(${member.id})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn btn-danger" onclick="deleteMember(${member.id}, '${member.name.replace(/'/g, "\\'")}')">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function showAddMemberModal() {
    populateSkillsCheckboxes('skillsCheckboxes');
    document.getElementById('addMemberModal').classList.add('active');
}

function populateSkillsCheckboxes(containerId) {
    const container = document.getElementById(containerId);
    let html = '';
    
    allSkills.forEach(skill => {
        html += `
            <label>
                <input type="checkbox" name="skill_${skill.id}" value="${skill.id}">
                ${skill.name}
                <select name="level_${skill.id}" style="width: 60px; margin-left: 5px;">
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3" selected>3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                </select>
            </label>
        `;
    });
    
    container.innerHTML = html;
}

async function addMember(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const member = {
        name: formData.get('name'),
        email: formData.get('email'),
        weeklyAvailability: parseInt(formData.get('weeklyAvailability')),
        currentWorkload: 0
    };
    
    try {
        const result = await MembersAPI.create(member);
        const memberId = result.id;
        
        for (const skill of allSkills) {
            const checkbox = form.querySelector(`input[name="skill_${skill.id}"]`);
            if (checkbox && checkbox.checked) {
                const level = parseInt(form.querySelector(`select[name="level_${skill.id}"]`).value);
                await MembersAPI.addSkill(memberId, skill.id, level);
            }
        }
        
        closeModal('addMemberModal');
        form.reset();
        showNotification('Member added successfully!', 'success');
        loadMembers();
        
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error adding member:', error);
        showNotification('Error adding member: ' + error.message, 'error');
    }
}

async function showEditMemberModal(memberId) {
    try {
        const members = await MembersAPI.getAll();
        const member = members.find(m => m.id === memberId);
        
        if (!member) {
            throw new Error('Member not found');
        }
        
        document.getElementById('editMemberId').value = member.id;
        document.getElementById('editMemberName').value = member.name;
        document.getElementById('editMemberEmail').value = member.email;
        document.getElementById('editMemberAvailability').value = member.weeklyAvailability;
        
        const container = document.getElementById('editSkillsCheckboxes');
        let html = '';
        
        allSkills.forEach(skill => {
            const memberSkill = member.skills.find(ms => ms.skill && ms.skill.id === skill.id);
            const checked = memberSkill ? 'checked' : '';
            const level = memberSkill ? memberSkill.proficiencyLevel : 3;
            
            html += `
                <label>
                    <input type="checkbox" name="skill_${skill.id}" value="${skill.id}" ${checked}>
                    ${skill.name}
                    <select name="level_${skill.id}" style="width: 60px; margin-left: 5px;">
                        <option value="1" ${level === 1 ? 'selected' : ''}>1</option>
                        <option value="2" ${level === 2 ? 'selected' : ''}>2</option>
                        <option value="3" ${level === 3 ? 'selected' : ''}>3</option>
                        <option value="4" ${level === 4 ? 'selected' : ''}>4</option>
                        <option value="5" ${level === 5 ? 'selected' : ''}>5</option>
                    </select>
                </label>
            `;
        });
        
        container.innerHTML = html;
        document.getElementById('editMemberModal').classList.add('active');
    } catch (error) {
        console.error('Error loading member:', error);
        showNotification('Error loading member details', 'error');
    }
}

async function updateMember(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    const memberId = parseInt(formData.get('memberId'));
    
    // Get current member data first to preserve workload
    const members = await MembersAPI.getAll();
    const currentMember = members.find(m => m.id === memberId);
    
    const member = {
        id: memberId,
        name: formData.get('name'),
        email: formData.get('email'),
        weeklyAvailability: parseInt(formData.get('weeklyAvailability')),
        currentWorkload: currentMember ? currentMember.currentWorkload : 0
    };
    
    try {
        await MembersAPI.update(member);
        
        const updatedMembers = await MembersAPI.getAll();
        const updatedMember = updatedMembers.find(m => m.id === memberId);
        
        if (updatedMember) {
            for (const ms of updatedMember.skills) {
                await MembersAPI.removeSkill(memberId, ms.skill.id);
            }
        }
        
        for (const skill of allSkills) {
            const checkbox = form.querySelector(`input[name="skill_${skill.id}"]`);
            if (checkbox && checkbox.checked) {
                const level = parseInt(form.querySelector(`select[name="level_${skill.id}"]`).value);
                await MembersAPI.addSkill(memberId, skill.id, level);
            }
        }
        
        closeModal('editMemberModal');
        showNotification('Member updated successfully!', 'success');
        loadMembers();
        
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error updating member:', error);
        showNotification('Error updating member: ' + error.message, 'error');
    }
}

async function deleteMember(memberId, memberName) {
    if (!confirm(`Are you sure you want to delete ${memberName}? This action cannot be undone.`)) {
        return;
    }
    
    try {
        await MembersAPI.delete(memberId);
        showNotification('Member deleted successfully!', 'success');
        loadMembers();
        
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error deleting member:', error);
        showNotification('Error deleting member: ' + error.message, 'error');
    }
}
