package org.projectmanagement.dao;


import org.projectmanagement.model.Skill;
import org.projectmanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillDAO {
    private static final Logger logger = LoggerFactory.getLogger(SkillDAO.class);

    public int create(Skill skill) throws SQLException {
        String sql = "INSERT INTO skills (name, description) VALUES (?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, skill.getName());
            stmt.setString(2, skill.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating skill failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    skill.setId(id);
                    logger.info("Created skill: {} with ID: {}", skill.getName(), id);
                    return id;
                } else {
                    throw new SQLException("Creating skill failed, no ID obtained.");
                }
            }
        }
    }

    public Skill findById(int id) throws SQLException {
        String sql = "SELECT * FROM skills WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSkillFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Skill findByName(String name) throws SQLException {
        String sql = "SELECT * FROM skills WHERE name = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSkillFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Skill> findAll() throws SQLException {
        String sql = "SELECT * FROM skills ORDER BY name";
        List<Skill> skills = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                skills.add(extractSkillFromResultSet(rs));
            }
        }
        return skills;
    }

    public void update(Skill skill) throws SQLException {
        String sql = "UPDATE skills SET name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, skill.getName());
            stmt.setString(2, skill.getDescription());
            stmt.setInt(3, skill.getId());
            
            stmt.executeUpdate();
            logger.info("Updated skill: {}", skill.getName());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM skills WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Deleted skill with ID: {}", id);
        }
    }

    private Skill extractSkillFromResultSet(ResultSet rs) throws SQLException {
        Skill skill = new Skill();
        skill.setId(rs.getInt("id"));
        skill.setName(rs.getString("name"));
        skill.setDescription(rs.getString("description"));
        skill.setCreatedAt(rs.getTimestamp("created_at"));
        return skill;
    }
}
