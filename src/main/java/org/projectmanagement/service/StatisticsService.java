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

/**
 * Service de génération de statistiques pour les projets et les membres
 * Fournit des métriques détaillées sur la progression et la charge de travail
 */
public class StatisticsService {
    // DAOs pour accéder aux données
    private final MemberDAO memberDAO;
    private final ProjectDAO projectDAO;
    private final TaskDAO taskDAO;

    // Constructeur : initialise les DAOs
    public StatisticsService() {
        this.memberDAO = new MemberDAO();
        this.projectDAO = new ProjectDAO();
        this.taskDAO = new TaskDAO(); 
    }

    /**
     * Calcule les statistiques détaillées d'un projet
     * L'ID du projet à analyser
     * return Map contenant toutes les métriques du projet
     */
    public Map<String, Object> getProjectStatistics(int projectId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Récupérer le projet
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return stats;  // Retourner map vide si projet inexistant
        }
        
        // Récupérer toutes les tâches du projet
        List<Task> tasks = taskDAO.findByProject(projectId);
        
        // === COMPTER LES TÂCHES PAR STATUT ===
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        long todoTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.TODO).count();
        long blockedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.BLOCKED).count();
        
        // Ajouter les compteurs au résultat
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("todoTasks", todoTasks);
        stats.put("blockedTasks", blockedTasks);
        stats.put("completionPercentage", totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0);
        
        // === CALCUL DES HEURES ===
        // Somme de toutes les heures estimées
        double totalEstimatedHours = tasks.stream().mapToDouble(Task::getEstimatedHours).sum();
        // Somme des heures des tâches complétées
        double completedHours = tasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
            .mapToDouble(Task::getEstimatedHours)
            .sum();
        
        stats.put("totalEstimatedHours", totalEstimatedHours);
        stats.put("completedHours", completedHours);
        stats.put("remainingHours", totalEstimatedHours - completedHours);
        
        // === DISTRIBUTION PAR PRIORITÉ ===
        // Grouper les tâches par niveau de priorité
        Map<String, Long> priorityDistribution = tasks.stream()
            .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        stats.put("priorityDistribution", priorityDistribution);
        
        // === TÂCHES ASSIGNÉES VS NON ASSIGNÉES ===
        long assignedTasks = tasks.stream().filter(Task::isAssigned).count();
        long unassignedTasks = totalTasks - assignedTasks;
        
        stats.put("assignedTasks", assignedTasks);
        stats.put("unassignedTasks", unassignedTasks);
        
        return stats;
    }

    /**
     * Calcule les statistiques de charge de travail de l'équipe
     * @return Map contenant les métriques globales et détaillées par membre
     */
    public Map<String, Object> getMemberWorkloadStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Récupérer tous les membres
        List<Member> members = memberDAO.findAll();
        
        // === CALCULS GLOBAUX ===
        // Somme des disponibilités hebdomadaires de tous les membres
        double totalAvailability = members.stream()
            .mapToDouble(Member::getWeeklyAvailability)
            .sum();
        
        // Somme de la charge de travail actuelle de tous les membres
        double totalWorkload = members.stream()
            .mapToDouble(Member::getCurrentWorkload)
            .sum();
        
        // Moyenne des pourcentages de charge
        double averageWorkloadPercentage = members.stream()
            .mapToDouble(Member::getWorkloadPercentage)
            .average()
            .orElse(0);  // 0 si aucun membre
        
        // Compter combien de membres sont surchargés (>100%)
        long overloadedMembers = members.stream()
            .filter(Member::isOverloaded)
            .count();
        
        // Ajouter les métriques globales
        stats.put("totalMembers", members.size());
        stats.put("totalAvailability", totalAvailability);
        stats.put("totalWorkload", totalWorkload);
        stats.put("averageWorkloadPercentage", averageWorkloadPercentage);
        stats.put("overloadedMembers", overloadedMembers);
        stats.put("utilizationPercentage", totalAvailability > 0 ? (totalWorkload / totalAvailability * 100) : 0);
        
        // === DÉTAILS PAR MEMBRE ===
        // Créer une liste avec les informations détaillées de chaque membre
        List<Map<String, Object>> memberWorkloads = members.stream()
            .map(member -> {
                // Pour chaque membre, créer un objet avec ses métriques
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("id", member.getId());
                memberData.put("name", member.getName());
                memberData.put("weeklyAvailability", member.getWeeklyAvailability());
                memberData.put("currentWorkload", member.getCurrentWorkload());
                memberData.put("availableHours", member.getAvailableHours());
                memberData.put("workloadPercentage", member.getWorkloadPercentage());
                memberData.put("isOverloaded", member.isOverloaded());
                
                // Récupérer le nombre de tâches assignées à ce membre
                // Récupérer le nombre de tâches assignées à ce membre
                try {
                    List<Task> memberTasks = taskDAO.findByMember(member.getId());
                    memberData.put("taskCount", memberTasks.size());
                } catch (SQLException e) {
                    memberData.put("taskCount", 0);  // En cas d'erreur, mettre 0
                }
                
                return memberData;
            })
            .collect(Collectors.toList());  // Collecter dans une liste
        
        // Ajouter la liste des membres au résultat
        stats.put("memberWorkloads", memberWorkloads);
        
        return stats;
    }

    /**
     * Calcule les statistiques globales du système (vue d'ensemble)
     * @return Map contenant les compteurs globaux
     */
    public Map<String, Object> getOverallStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Récupérer tous les projets et membres
        List<Project> projects = projectDAO.findAll();
        List<Member> members = memberDAO.findAll();
        
        // Ajouter les compteurs de base
        stats.put("totalProjects", projects.size());
        stats.put("totalMembers", members.size());
        
        // Compter toutes les tâches de tous les projets
        int totalTasks = 0;
        for (Project project : projects) {
            List<Task> tasks = taskDAO.findByProject(project.getId());
            totalTasks += tasks.size();
        }
        stats.put("totalTasks", totalTasks);
        
        return stats;
    }
}
