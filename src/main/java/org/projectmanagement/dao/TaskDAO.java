package org.projectmanagement.dao;

import org.projectmanagement.model.Task;
import org.projectmanagement.model.TaskSkill;
import org.projectmanagement.model.Skill;
import org.projectmanagement.model.Member;
import org.projectmanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    public int create(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (project_id, title, description, estimated_hours, priority, " +
                    "status, start_date, deadline, assigned_member_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getProjectId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setDouble(4, task.getEstimatedHours());
            stmt.setString(5, task.getPriority().name());
            stmt.setString(6, task.getStatus().name());
            stmt.setDate(7, task.getStartDate());
            stmt.setDate(8, task.getDeadline());
            if (task.getAssignedMember() != null) {
                stmt.setInt(9, task.getAssignedMember().getId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    task.setId(id);
                    logger.info("Created task: {} with ID: {}", task.getTitle(), id);
                    return id;
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
        }
    }

    public Task findById(int id) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id WHERE t.id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(id));
                    task.setDependencies(findTaskDependencies(id));
                    return task;
                }
            }
        }
        return null;
    }

    public List<Task> findByProject(int projectId) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.project_id = ? ORDER BY t.priority DESC, t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> findUnassignedByProject(int projectId) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.project_id = ? AND t.assigned_member_id IS NULL " +
                    "ORDER BY t.priority DESC, t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> findByProjectAndStatus(int projectId, Task.TaskStatus status) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.project_id = ? AND t.status = ? " +
                    "ORDER BY t.priority DESC, t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            stmt.setString(2, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> findByMember(int memberId) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.assigned_member_id = ? ORDER BY t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public void update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, estimated_hours = ?, " +
                    "priority = ?, status = ?, start_date = ?, deadline = ?, assigned_member_id = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDouble(3, task.getEstimatedHours());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getStatus().name());
            stmt.setDate(6, task.getStartDate());
            stmt.setDate(7, task.getDeadline());
            if (task.getAssignedMember() != null) {
                stmt.setInt(8, task.getAssignedMember().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setInt(9, task.getId());
            
            stmt.executeUpdate();
            logger.info("Updated task: {}", task.getTitle());
        }
    }

    public void assignTask(int taskId, int memberId) throws SQLException {
        String sql = "UPDATE tasks SET assigned_member_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            logger.info("Assigned task {} to member {}", taskId, memberId);
        }
    }
    
    /**
     * Assign a task to a member manually (with competence check, workload update and alerts)
     */
    public void assignTaskToMember(int taskId, int memberId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Get task details and member name
            String getTaskSql = "SELECT t.estimated_hours, m.name as member_name " +
                               "FROM tasks t, members m WHERE t.id = ? AND m.id = ?";
            double taskHours = 0;
            String memberName = "";
            
            try (PreparedStatement stmt = conn.prepareStatement(getTaskSql)) {
                stmt.setInt(1, taskId);
                stmt.setInt(2, memberId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        taskHours = rs.getDouble("estimated_hours");
                        memberName = rs.getString("member_name");
                    } else {
                        throw new SQLException("Task or member not found");
                    }
                }
            }
            
            // 2. Check member competence for required skills
            String checkSkillsSql = 
                "SELECT ts.skill_id, ts.required_level, " +
                "COALESCE(ms.proficiency_level, 0) as member_level " +
                "FROM task_skills ts " +
                "LEFT JOIN member_skills ms ON ts.skill_id = ms.skill_id AND ms.member_id = ? " +
                "WHERE ts.task_id = ?";
            
            int totalSkills = 0;
            int matchedSkills = 0;
            StringBuilder missingSkills = new StringBuilder();
            
            try (PreparedStatement stmt = conn.prepareStatement(checkSkillsSql)) {
                stmt.setInt(1, memberId);
                stmt.setInt(2, taskId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        totalSkills++;
                        int requiredLevel = rs.getInt("required_level");
                        int memberLevel = rs.getInt("member_level");
                        
                        if (memberLevel >= requiredLevel) {
                            matchedSkills++;
                        } else {
                            if (missingSkills.length() > 0) missingSkills.append(", ");
                            missingSkills.append("Skill #").append(rs.getInt("skill_id"));
                        }
                    }
                }
            }
            
            // Calculate competence score
            double competenceScore = totalSkills > 0 ? (double) matchedSkills / totalSkills : 1.0;
            
            // Refuse assignment if competence < 0.6
            if (competenceScore < 0.6) {
                throw new SQLException(
                    String.format("INCOMPETENT: %s n'est pas assez compétent pour cette tâche (score: %.0f%%).",
                                memberName, competenceScore * 100)
                );
            }
            
            // 3. Update task assignment
            String updateTaskSql = "UPDATE tasks SET assigned_member_id = ?, updated_at = NOW() WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateTaskSql)) {
                stmt.setInt(1, memberId);
                stmt.setInt(2, taskId);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Assigning task failed, task not found.");
                }
            }
            
            // 4. Update member workload
            String updateWorkloadSql = "UPDATE members SET current_workload = current_workload + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateWorkloadSql)) {
                stmt.setDouble(1, taskHours);
                stmt.setInt(2, memberId);
                stmt.executeUpdate();
            }
            
            // 5. Check if member is overloaded and create alert
            String checkOverloadSql = "SELECT name, current_workload, weekly_availability FROM members WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkOverloadSql)) {
                stmt.setInt(1, memberId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        double currentWorkload = rs.getDouble("current_workload");
                        int weeklyAvailability = rs.getInt("weekly_availability");
                        
                        if (currentWorkload > weeklyAvailability) {
                            double overloadPct = (currentWorkload / weeklyAvailability) * 100;
                            
                            // Create overload alert
                            String alertSql = "INSERT INTO alerts (type, severity, title, message, member_id, is_read) " +
                                            "VALUES ('OVERLOAD', ?, ?, ?, ?, false)";
                            try (PreparedStatement alertStmt = conn.prepareStatement(alertSql)) {
                                String severity = overloadPct > 150 ? "CRITICAL" : "HIGH";
                                String title = "Member Overloaded: " + name;
                                String message = String.format("%s is overloaded at %.1f%% capacity (%.1f/%.1f hours)",
                                    name, overloadPct, currentWorkload, (double)weeklyAvailability);
                                
                                alertStmt.setString(1, severity);
                                alertStmt.setString(2, title);
                                alertStmt.setString(3, message);
                                alertStmt.setInt(4, memberId);
                                alertStmt.executeUpdate();
                                
                                logger.info("Created overload alert for member {}", name);
                            }
                        }
                    }
                }
            }
            
            conn.commit();
            logger.info("Manually assigned task {} to member {} with workload update", taskId, memberId);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    /**
     * Unassign a task from its member (only if status is TODO) with workload update
     */
    public void unassignTask(int taskId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Get task details and current assignment
            String getTaskSql = "SELECT estimated_hours, assigned_member_id FROM tasks WHERE id = ? AND status = 'TODO'";
            double taskHours = 0;
            Integer memberId = null;
            
            try (PreparedStatement stmt = conn.prepareStatement(getTaskSql)) {
                stmt.setInt(1, taskId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        taskHours = rs.getDouble("estimated_hours");
                        memberId = rs.getInt("assigned_member_id");
                        if (rs.wasNull()) {
                            memberId = null;
                        }
                    } else {
                        throw new SQLException("Cannot unassign task: either task not found or status is not TODO");
                    }
                }
            }
            
            if (memberId == null) {
                throw new SQLException("Task is not assigned to anyone");
            }
            
            // 2. Update task to remove assignment
            String updateTaskSql = "UPDATE tasks SET assigned_member_id = NULL, updated_at = NOW() WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateTaskSql)) {
                stmt.setInt(1, taskId);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Cannot unassign task");
                }
            }
            
            // 3. Update member workload (subtract hours)
            String updateWorkloadSql = "UPDATE members SET current_workload = GREATEST(0, current_workload - ?) WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateWorkloadSql)) {
                stmt.setDouble(1, taskHours);
                stmt.setInt(2, memberId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            logger.info("Unassigned task {} and updated workload for member {}", taskId, memberId);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    public void updateStatus(int taskId, Task.TaskStatus status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Deleted task with ID: {}", id);
        }
    }

    public void addSkillRequirement(int taskId, int skillId, int requiredLevel) throws SQLException {
        String sql = "INSERT INTO task_skills (task_id, skill_id, required_level) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE required_level = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            stmt.setInt(2, skillId);
            stmt.setInt(3, requiredLevel);
            stmt.setInt(4, requiredLevel);
            stmt.executeUpdate();
        }
    }

    public void addDependency(int taskId, int dependsOnTaskId) throws SQLException {
        String sql = "INSERT INTO task_dependencies (task_id, depends_on_task_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            stmt.setInt(2, dependsOnTaskId);
            stmt.executeUpdate();
        }
    }

    public List<TaskSkill> findTaskSkills(int taskId) throws SQLException {
        String sql = "SELECT ts.*, s.name as skill_name FROM task_skills ts " +
                    "JOIN skills s ON ts.skill_id = s.id WHERE ts.task_id = ?";
        List<TaskSkill> skills = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TaskSkill skill = new TaskSkill();
                    
                    // Initialiser l'objet Task
                    Task task = new Task();
                    task.setId(rs.getInt("task_id"));
                    skill.setTask(task);
                    
                    // Initialiser l'objet Skill
                    Skill skillObj = new Skill();
                    skillObj.setId(rs.getInt("skill_id"));
                    skillObj.setName(rs.getString("skill_name"));
                    skill.setSkill(skillObj);
                    
                    skill.setRequiredLevel(rs.getInt("required_level"));
                    skills.add(skill);
                }
            }
        }
        return skills;
    }

    public List<Integer> findTaskDependencies(int taskId) throws SQLException {
        String sql = "SELECT depends_on_task_id FROM task_dependencies WHERE task_id = ?";
        List<Integer> dependencies = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dependencies.add(rs.getInt("depends_on_task_id"));
                }
            }
        }
        return dependencies;
    }

    private Task extractTaskFromResultSet(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setProjectId(rs.getInt("project_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setEstimatedHours(rs.getDouble("estimated_hours"));
        task.setPriority(Task.Priority.valueOf(rs.getString("priority")));
        task.setStatus(Task.TaskStatus.valueOf(rs.getString("status")));
        task.setStartDate(rs.getDate("start_date"));
        task.setDeadline(rs.getDate("deadline"));
        
        int assignedMemberId = rs.getInt("assigned_member_id");
        if (!rs.wasNull()) {
            // Initialiser l'objet Member
            Member assignedMember = new Member();
            assignedMember.setId(assignedMemberId);
            assignedMember.setName(rs.getString("member_name"));
            task.setAssignedMember(assignedMember);
        }

        return task;
    }
}
