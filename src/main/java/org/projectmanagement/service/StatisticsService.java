package org.projectmanagement.service;

import org.projectmanagement.dao.MemberDAO;
import org.projectmanagement.dao.ProjectDAO;
import org.projectmanagement.dao.TaskDAO;
import org.projectmanagement.model.Member;
import org.projectmanagement.model.Project;
import org.projectmanagement.model.Task;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {
    private final MemberDAO memberDAO;
    private final ProjectDAO projectDAO;
    private final TaskDAO taskDAO;

    public StatisticsService() {
        this.memberDAO = new MemberDAO();
        this.projectDAO = new ProjectDAO();
        this.taskDAO = new TaskDAO(); 
    }

    public Map<String, Object> getProjectStatistics(int projectId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return stats;
        }
        
        List<Task> tasks = taskDAO.findByProject(projectId);
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        long todoTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.TODO).count();
        long blockedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.BLOCKED).count();
        
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("todoTasks", todoTasks);
        stats.put("blockedTasks", blockedTasks);
        stats.put("completionPercentage", totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0);
        
        double totalEstimatedHours = tasks.stream().mapToDouble(Task::getEstimatedHours).sum();
        double completedHours = tasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
            .mapToDouble(Task::getEstimatedHours)
            .sum();
        
        stats.put("totalEstimatedHours", totalEstimatedHours);
        stats.put("completedHours", completedHours);
        stats.put("remainingHours", totalEstimatedHours - completedHours);
        
        Map<String, Long> priorityDistribution = tasks.stream()
            .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        stats.put("priorityDistribution", priorityDistribution);
        
        long assignedTasks = tasks.stream().filter(Task::isAssigned).count();
        long unassignedTasks = totalTasks - assignedTasks;
        
        stats.put("assignedTasks", assignedTasks);
        stats.put("unassignedTasks", unassignedTasks);
        
        return stats;
    }

    public Map<String, Object> getMemberWorkloadStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        List<Member> members = memberDAO.findAll();
        
        double totalAvailability = members.stream()
            .mapToDouble(Member::getWeeklyAvailability)
            .sum();
        
        double totalWorkload = members.stream()
            .mapToDouble(Member::getCurrentWorkload)
            .sum();
        
        double averageWorkloadPercentage = members.stream()
            .mapToDouble(Member::getWorkloadPercentage)
            .average()
            .orElse(0);
        
        long overloadedMembers = members.stream()
            .filter(Member::isOverloaded)
            .count();
        
        stats.put("totalMembers", members.size());
        stats.put("totalAvailability", totalAvailability);
        stats.put("totalWorkload", totalWorkload);
        stats.put("averageWorkloadPercentage", averageWorkloadPercentage);
        stats.put("overloadedMembers", overloadedMembers);
        stats.put("utilizationPercentage", totalAvailability > 0 ? (totalWorkload / totalAvailability * 100) : 0);
        
        List<Map<String, Object>> memberWorkloads = members.stream()
            .map(member -> {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("id", member.getId());
                memberData.put("name", member.getName());
                memberData.put("weeklyAvailability", member.getWeeklyAvailability());
                memberData.put("currentWorkload", member.getCurrentWorkload());
                memberData.put("availableHours", member.getAvailableHours());
                memberData.put("workloadPercentage", member.getWorkloadPercentage());
                memberData.put("isOverloaded", member.isOverloaded());
                
                try {
                    List<Task> memberTasks = taskDAO.findByMember(member.getId());
                    memberData.put("taskCount", memberTasks.size());
                } catch (SQLException e) {
                    memberData.put("taskCount", 0);
                }
                
                return memberData;
            })
            .collect(Collectors.toList());
        
        stats.put("memberWorkloads", memberWorkloads);
        
        return stats;
    }

    public Map<String, Object> getOverallStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        List<Project> projects = projectDAO.findAll();
        List<Member> members = memberDAO.findAll();
        
        stats.put("totalProjects", projects.size());
        stats.put("totalMembers", members.size());
        
        int totalTasks = 0;
        for (Project project : projects) {
            List<Task> tasks = taskDAO.findByProject(project.getId());
            totalTasks += tasks.size();
        }
        stats.put("totalTasks", totalTasks);
        
        return stats;
    }
}
