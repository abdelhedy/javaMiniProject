package org.projectmanagement.dao;

import org.projectmanagement.model.Project;
import org.projectmanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);

    public int create(Project project) throws SQLException {
        String sql = "INSERT INTO projects (name, description, start_date, deadline, status) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setDate(3, project.getStartDate());
            stmt.setDate(4, project.getDeadline());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating project failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    project.setId(id);
                    logger.info("Created project: {} with ID: {}", project.getName(), id);
                    return id;
                } else {
                    throw new SQLException("Creating project failed, no ID obtained.");
                }
            }
        }
    }

    public Project findById(int id) throws SQLException {
        String sql = "SELECT * FROM projects WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractProjectFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Project> findAll() throws SQLException {
        String sql = "SELECT * FROM projects ORDER BY created_at DESC";
        List<Project> projects = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }
        }
        return projects;
    }

    public void update(Project project) throws SQLException {
        String sql = "UPDATE projects SET name = ?, description = ?, start_date = ?, " +
                    "deadline = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setDate(3, project.getStartDate());
            stmt.setDate(4, project.getDeadline());
            stmt.setString(5, project.getStatus().name());
            stmt.setInt(6, project.getId());
            
            stmt.executeUpdate();
            logger.info("Updated project: {}", project.getName());
        }
    }


    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM projects WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Deleted project with ID: {}", id);
        }
    }

    private Project extractProjectFromResultSet(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setStartDate(rs.getDate("start_date"));
        project.setDeadline(rs.getDate("deadline"));
        project.setStatus(Project.ProjectStatus.valueOf(rs.getString("status")));
        return project;
    }
}
