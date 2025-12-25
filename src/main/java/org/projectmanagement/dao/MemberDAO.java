package org.projectmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.projectmanagement.model.Member;
import org.projectmanagement.model.MemberSkill;
import org.projectmanagement.model.Skill;
import org.projectmanagement.util.DatabaseUtil;

public class MemberDAO{

    public int create(Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, weekly_availability, current_workload) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setInt(3, member.getWeeklyAvailability());
            stmt.setDouble(4, member.getCurrentWorkload());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating member failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    member.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating member failed, no ID obtained.");
                }
            }
        }
    }

    public Member findById(int id) throws SQLException {
        String sql = "SELECT * FROM members WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Member member = extractMemberFromResultSet(rs);
                    member.setSkills(findMemberSkills(id));
                    return member;
                }
            }
        }
        return null;
    }

    public List<Member> findAll() throws SQLException {
        String sql = "SELECT * FROM members ORDER BY name";
        List<Member> members = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Member member = extractMemberFromResultSet(rs);
                member.setSkills(findMemberSkills(member.getId()));
                members.add(member);
            }
        }
        return members;
    }

    public void update(Member member) throws SQLException {
        String sql = "UPDATE members SET name = ?, email = ?, weekly_availability = ?, " +
                    "current_workload = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setInt(3, member.getWeeklyAvailability());
            stmt.setDouble(4, member.getCurrentWorkload());
            stmt.setInt(5, member.getId());
            
            stmt.executeUpdate();
        }
    }

    public void updateWorkload(int memberId, double workload) throws SQLException {
        String sql = "UPDATE members SET current_workload = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, workload);
            stmt.setInt(2, memberId);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM members WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void addSkill(int memberId, int skillId, int proficiencyLevel) throws SQLException {
        String sql = "INSERT INTO member_skills (member_id, skill_id, proficiency_level) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE proficiency_level = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            stmt.setInt(2, skillId);
            stmt.setInt(3, proficiencyLevel);
            stmt.setInt(4, proficiencyLevel);
            stmt.executeUpdate();
        }
    }

    public void removeSkill(int memberId, int skillId) throws SQLException {
        String sql = "DELETE FROM member_skills WHERE member_id = ? AND skill_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            stmt.setInt(2, skillId);
            stmt.executeUpdate();
        }
    }

    public List<MemberSkill> findMemberSkills(int memberId) throws SQLException {
        String sql = "SELECT ms.*, s.name as skill_name FROM member_skills ms " +
                    "JOIN skills s ON ms.skill_id = s.id WHERE ms.member_id = ?";
        List<MemberSkill> skills = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberSkill memberSkill = new MemberSkill();
                    
                    Member member = new Member();
                    member.setId(rs.getInt("member_id"));
                    memberSkill.setMember(member);
                    
                    Skill skill = new Skill();
                    skill.setId(rs.getInt("skill_id"));
                    skill.setName(rs.getString("skill_name"));
                    memberSkill.setSkill(skill);
                    
                    memberSkill.setProficiencyLevel(rs.getInt("proficiency_level"));
                    skills.add(memberSkill);
                }
            }
        }
        return skills;
    }

    public List<Member> findAvailableMembers(double minAvailableHours) throws SQLException {
        String sql = "SELECT * FROM members WHERE (weekly_availability - current_workload) >= ? " +
                    "ORDER BY current_workload ASC";
        List<Member> members = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, minAvailableHours);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Member member = extractMemberFromResultSet(rs);
                    member.setSkills(findMemberSkills(member.getId()));
                    members.add(member);
                }
            }
        }
        return members;
    }

    public List<Member> findBySkill(int skillId, int minProficiency) throws SQLException {
        String sql = "SELECT m.* FROM members m " +
                    "JOIN member_skills ms ON m.id = ms.member_id " +
                    "WHERE ms.skill_id = ? AND ms.proficiency_level >= ? " +
                    "ORDER BY ms.proficiency_level DESC, m.current_workload ASC";
        List<Member> members = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, skillId);
            stmt.setInt(2, minProficiency);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Member member = extractMemberFromResultSet(rs);
                    member.setSkills(findMemberSkills(member.getId()));
                    members.add(member);
                }
            }
        }
        return members;
    }

    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setWeeklyAvailability(rs.getInt("weekly_availability"));
        member.setCurrentWorkload(rs.getDouble("current_workload"));
        return member;
    }
}