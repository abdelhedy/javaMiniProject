
//alerts functions
async function loadAlerts() {
    try {
        const alerts = await AlertsAPI.getAll();
        displayAlerts(alerts);
    } catch (error) {
        console.error('Error loading alerts:', error);
    }
}

async function loadAlertCount() {
    try {
        const result = await AlertsAPI.getUnreadCount();
        const badge = document.getElementById('alertBadge');
        badge.textContent = result.count;
        badge.style.display = result.count > 0 ? 'inline-block' : 'none';
    } catch (error) {
        console.error('Error loading alert count:', error);
    }
}

function displayAlerts(alerts) {
    const container = document.getElementById('alertsList');

    if (alerts.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-bell"></i><p>No alerts</p></div>';
        return;
    }

    let html = '';
    alerts.forEach(alert => {
        const unreadClass = !alert.isRead ? 'unread' : '';

        html += `
            <div class="alert-item ${alert.type.toLowerCase()} ${unreadClass}">
                <div class="alert-content">
                    <h4>${alert.title}</h4>
                    <p>${alert.message}</p>
                    ${alert.member && alert.member.name ? `<p><strong>Member:</strong> ${alert.member.name}</p>` : ''}
                    ${alert.project && alert.project.name ? `<p><strong>Project:</strong> ${alert.project.name}</p>` : ''}
                    <div class="alert-time">${new Date(alert.createdAt).toLocaleString()}</div>
                </div>
                <div>
                    ${!alert.isRead ? `<button class="btn btn-sm btn-secondary" onclick="markAlertRead(${alert.id})">Mark Read</button>` : ''}
                    <button class="btn btn-sm btn-danger" onclick="deleteAlert(${alert.id})">Delete</button>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

async function markAlertRead(alertId) {
    try {
        await AlertsAPI.markAsRead(alertId);
        loadAlerts();
        loadAlertCount();
    } catch (error) {
        console.error('Error marking alert as read:', error);
    }
}

async function markAllAlertsRead() {
    try {
        await AlertsAPI.markAllAsRead();
        showNotification('All alerts marked as read', 'success');
        loadAlerts();
        loadAlertCount();
    } catch (error) {
        console.error('Error marking alerts as read:', error);
    }
}

async function deleteAlert(alertId) {
    try {
        await AlertsAPI.delete(alertId);
        loadAlerts();
        loadAlertCount();
    } catch (error) {
        console.error('Error deleting alert:', error);
    }
}