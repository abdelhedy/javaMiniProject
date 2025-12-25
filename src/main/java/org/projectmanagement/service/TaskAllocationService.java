package org.projectmanagement.service;

import org.projectmanagement.dao.AlertDAO;
import org.projectmanagement.dao.MemberDAO;
import org.projectmanagement.dao.TaskDAO;
import org.projectmanagement.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TaskAllocationService {
    private static final Logger logger = LoggerFactory.getLogger(TaskAllocationService.class);
    
    /**
     * Seuil minimum de compétence requis pour affecter une tâche
     * - 0.6 = Compétences moyennes à bonnes requises (60%)
     * Ce seuil garantit qu'on n'affecte pas de tâches à des personnes non compétentes
     */
    private static final double MINIMUM_COMPETENCE_THRESHOLD = 0.6;
    
    /**
     * Seuil pour le rééquilibrage (identique au seuil d'affectation)
     * Lors du rééquilibrage, on garde le même niveau d'exigence
     */
    private static final double REBALANCING_COMPETENCE_THRESHOLD = 0.6;
    
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final AlertDAO alertDAO;

    public TaskAllocationService() {
        this.taskDAO = new TaskDAO();
        this.memberDAO = new MemberDAO();
        this.alertDAO = new AlertDAO();
    }

    public AllocationResult allocateTasks(int projectId) throws SQLException {
        logger.info("Starting task allocation for project: {}", projectId);
        
        List<Task> unassignedTasks = taskDAO.findUnassignedByProject(projectId);
        
        // Récupérer les tâches TODO assignées à des membres surchargés
        List<Task> todoTasksFromOverloadedMembers = taskDAO.findByProjectAndStatus(projectId, Task.TaskStatus.TODO);
        List<Member> availableMembers = memberDAO.findAll();
        
        if (availableMembers.isEmpty()) {
            logger.warn("No available members found");
            return new AllocationResult(0, unassignedTasks.size(), "No available members");
        }
        
        logger.info("Checking for existing overloaded members before allocation");
        checkExistingOverloads(projectId);
        
        int assignedCount = 0;
        int failedCount = 0;
        int rebalancedCount = 0;
        
        logger.info("Phase 1: Rebalancing TODO tasks from overloaded members");
        logger.info("Found {} TODO tasks to evaluate for rebalancing", todoTasksFromOverloadedMembers.size());
        
        for (Task task : todoTasksFromOverloadedMembers) {
            if (task.getAssignedMember() == null) continue;
            
            try {
                Member currentMember = memberDAO.findById(task.getAssignedMember().getId());
                logger.info("Evaluating task '{}' assigned to '{}' ({}%)", 
                    task.getTitle(), currentMember.getName(), 
                    String.format("%.1f", currentMember.getWorkloadPercentage()));
                
                if (currentMember.getWorkloadPercentage() > 100) {
                    logger.info("Member is overloaded, searching for better member...");
                    Member betterMember = findBetterMember(task, availableMembers, currentMember);
                    
                    if (betterMember != null && betterMember.getId() != currentMember.getId()) {
                        logger.info("Rebalancing task '{}' from '{}' ({}%) to '{}' ({}%)", 
                            task.getTitle(), 
                            currentMember.getName(), 
                            String.format("%.1f", currentMember.getWorkloadPercentage()),
                            betterMember.getName(),
                            String.format("%.1f", betterMember.getWorkloadPercentage()));
                        
                        currentMember.setCurrentWorkload(currentMember.getCurrentWorkload() - task.getEstimatedHours());
                        memberDAO.updateWorkload(currentMember.getId(), currentMember.getCurrentWorkload());
                        
                        assignTaskToMember(task, betterMember);
                        betterMember.setCurrentWorkload(betterMember.getCurrentWorkload() + task.getEstimatedHours());
                        memberDAO.updateWorkload(betterMember.getId(), betterMember.getCurrentWorkload());
                        
                        rebalancedCount++;
                    }
                }
            } catch (Exception e) {
                logger.error("Error rebalancing task: {}", task.getTitle(), e);
            }
        }
        
        logger.info("Phase 2: Assigning unassigned tasks");
        if (!unassignedTasks.isEmpty()) {
            List<Task> sortedTasks = prioritizeTasks(unassignedTasks);
            
            for (Task task : sortedTasks) {
                try {
                    Member bestMember = findBestMember(task, availableMembers);
                    
                    if (bestMember != null) {
                        assignTaskToMember(task, bestMember);
                        
                        bestMember.setCurrentWorkload(
                            bestMember.getCurrentWorkload() + task.getEstimatedHours()
                        );
                        memberDAO.updateWorkload(bestMember.getId(), bestMember.getCurrentWorkload());
                        
                        assignedCount++;
                        logger.info("Assigned task '{}' to member '{}'", task.getTitle(), bestMember.getName());
                        
                        if (bestMember.isOverloaded()) {
                            createOverloadAlert(bestMember, task);
                        }
                    } else {
                        failedCount++;
                        logger.warn("Could not find suitable member for task: {}", task.getTitle());
                        createNoSuitableMemberAlert(task, projectId);
                    }
                } catch (Exception e) {
                    failedCount++;
                    logger.error("Error assigning task: {}", task.getTitle(), e);
                }
            }
        }
        
        String message = String.format("Assigned %d new tasks, rebalanced %d tasks, failed %d", 
            assignedCount, rebalancedCount, failedCount);
        logger.info("Allocation complete: {}", message);
        
        return new AllocationResult(assignedCount + rebalancedCount, failedCount, message);
    }
    
    private Member findBetterMember(Task task, List<Member> members, Member currentMember) {
        double currentFinalWorkload = (currentMember.getCurrentWorkload() / currentMember.getWeeklyAvailability()) * 100;
        
        logger.info("Searching for better member than '{}' (current: {}%)", 
            currentMember.getName(), 
            String.format("%.1f", currentMember.getWorkloadPercentage()));
        
        List<CandidateEvaluation> candidates = new ArrayList<>();
        
        for (Member member : members) {
            if (member.getId() == currentMember.getId()) {
                continue;
            }
            
            double score = calculateMemberScore(task, member);
            
            if (score < REBALANCING_COMPETENCE_THRESHOLD) {
                logger.debug("Skipping '{}' - insufficient score ({:.3f} < {:.2f})", 
                    member.getName(), score, REBALANCING_COMPETENCE_THRESHOLD);
                continue;
            }
            
            double newWorkload = member.getCurrentWorkload() + task.getEstimatedHours();
            double finalWorkloadPct = (newWorkload / member.getWeeklyAvailability()) * 100;
            
            candidates.add(new CandidateEvaluation(member, score, finalWorkloadPct));
            
            logger.info("  Candidate '{}': score={:.3f}, current={}%, would be={}%", 
                member.getName(), score,
                String.format("%.1f", member.getWorkloadPercentage()),
                String.format("%.1f", finalWorkloadPct));
        }
        
        if (candidates.isEmpty()) {
            logger.info("No qualified candidates found");
            return null;
        }
        
        Member bestWithoutOverload = candidates.stream()
            .filter(c -> c.finalWorkloadPct < 100.0)
            .sorted((c1, c2) -> Double.compare(c2.score, c1.score))
            .map(c -> c.member)
            .findFirst()
            .orElse(null);
        
        if (bestWithoutOverload != null) {
            CandidateEvaluation selected = candidates.stream()
                .filter(c -> c.member.getId() == bestWithoutOverload.getId())
                .findFirst()
                .get();
            
            logger.info("PHASE 1 SUCCESS: Selected '{}' (score={:.3f}, final={}%) - NO overload!", 
                selected.member.getName(), selected.score, String.format("%.1f", selected.finalWorkloadPct));
            
            if (selected.finalWorkloadPct < currentFinalWorkload) {
                return bestWithoutOverload;
            }
        }
        
        // PHASE 2 : Tous seront surchargés, choisir celui avec la SURCHARGE MINIMALE
        logger.info("PHASE 2: All candidates will be overloaded, selecting minimum overload");
        
        CandidateEvaluation bestMinimalOverload = candidates.stream()
            .min((c1, c2) -> Double.compare(c1.finalWorkloadPct, c2.finalWorkloadPct))
            .orElse(null);
        
        if (bestMinimalOverload != null && bestMinimalOverload.finalWorkloadPct < currentFinalWorkload) {
            logger.info("PHASE 2 SUCCESS: Selected '{}' (score={:.3f}, final={}%) - minimal overload", 
                bestMinimalOverload.member.getName(), 
                bestMinimalOverload.score, 
                String.format("%.1f", bestMinimalOverload.finalWorkloadPct));
            return bestMinimalOverload.member;
        }
        
        logger.info("No better member found (current {}% is already optimal)", 
            String.format("%.1f", currentFinalWorkload));
        return null;
    }
    
    private static class CandidateEvaluation {
        Member member;
        double score;
        double finalWorkloadPct;
        
        CandidateEvaluation(Member member, double score, double finalWorkloadPct) {
            this.member = member;
            this.score = score;
            this.finalWorkloadPct = finalWorkloadPct;
        }
    }

    private Member findBestMember(Task task, List<Member> members) {
        Member bestMember = null;
        double bestScore = -1;
        
        for (Member member : members) {
            double score = calculateMemberScore(task, member);
            
            if (score >= MINIMUM_COMPETENCE_THRESHOLD && score > bestScore) {
                bestScore = score;
                bestMember = member;
            }
        }
        
        if (bestMember != null) {
            logger.info("Selected member '{}' with score {:.3f} (threshold: {:.2f})", 
                bestMember.getName(), bestScore, MINIMUM_COMPETENCE_THRESHOLD);
        } else {
            logger.warn("No member found with sufficient competence (threshold: {:.2f})", 
                MINIMUM_COMPETENCE_THRESHOLD);
        }
        
        return bestMember;
    }

    private double calculateMemberScore(Task task, Member member) {
        double skillScore = calculateSkillScore(task, member);
        double priorityBonus = task.getPriorityScore() * 0.025;
        
        if (skillScore == 0) {
            return 0;
        }
        
        double newWorkload = member.getCurrentWorkload() + task.getEstimatedHours();
        double newWorkloadPercentage = (newWorkload / member.getWeeklyAvailability()) * 100;
        
        double workloadScore;
        if (newWorkloadPercentage <= 100) {
            workloadScore = 1.0 - (newWorkloadPercentage / 100.0 * 0.2);
        } else {
            double overload = newWorkloadPercentage - 100;
            workloadScore = Math.max(0, 0.8 - (overload / 100.0 * 0.8));
        }
        
        double totalScore = (skillScore * 0.5) + 
                           (workloadScore * 0.4) + 
                           priorityBonus;
        
        logger.debug("Member {} score for task {}: {:.3f} (skill={:.2f}, newWorkload={:.1f}%, workloadScore={:.2f})",
                    member.getName(), task.getTitle(), totalScore, skillScore, newWorkloadPercentage, workloadScore);
        
        return totalScore;
    }

    private double calculateSkillScore(Task task, Member member) {
        List<TaskSkill> requiredSkills = task.getRequiredSkills();
        
        if (requiredSkills.isEmpty()) {
            return 0.5;
        }
        
        int matchedSkills = 0;
        int totalSkillLevel = 0;
        int maxSkillLevel = 0;
        
        for (TaskSkill taskSkill : requiredSkills) {
            maxSkillLevel += taskSkill.getRequiredLevel();
            
            Optional<MemberSkill> memberSkill = member.getSkills().stream()
                .filter(ms -> ms.getSkill().equals(taskSkill.getSkill()))
                .findFirst();
            
            if (memberSkill.isPresent()) {
                int proficiency = memberSkill.get().getProficiencyLevel();
                if (proficiency >= taskSkill.getRequiredLevel()) {
                    matchedSkills++;
                    totalSkillLevel += proficiency;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
        
        if (requiredSkills.size() == matchedSkills) {
            return Math.min(1.0, (double) totalSkillLevel / maxSkillLevel);
        }
        
        return 0;
    }

    private double calculateAvailabilityScore(Task task, Member member) {
        double availableHours = member.getAvailableHours();
        double requiredHours = task.getEstimatedHours();
        
        if (availableHours < requiredHours) {
            return 0;
        }
        
        double ratio = requiredHours / availableHours;
        
        if (ratio >= 0.5) {
            return 1.0;
        } else {
            return 0.5 + ratio;
        }
    }

    private double calculateWorkloadScore(Member member) {
        double workloadPercentage = member.getWorkloadPercentage();
        
        if (workloadPercentage >= 100) {
            return 0;
        }
        
        return 1.0 - (workloadPercentage / 100.0 * 0.9);
    }

    private List<Task> prioritizeTasks(List<Task> tasks) {
        return tasks.stream()
            .sorted((t1, t2) -> {
                int priorityCompare = Integer.compare(t2.getPriorityScore(), t1.getPriorityScore());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                
                if (t1.getDeadline() != null && t2.getDeadline() != null) {
                    return t1.getDeadline().compareTo(t2.getDeadline());
                }
                
                return 0;
            })
            .collect(Collectors.toList());
    }

    private void assignTaskToMember(Task task, Member member) throws SQLException {
        taskDAO.assignTask(task.getId(), member.getId());
        
        if (task.getAssignedMember() == null) {
            task.setAssignedMember(new Member());
        }
        task.getAssignedMember().setId(member.getId());
        task.getAssignedMember().setName(member.getName());
    }
    
    public void checkAndCreateOverloadAlert(int memberId, int taskId) throws SQLException {
        Member member = memberDAO.findById(memberId);
        Task task = taskDAO.findById(taskId);
        
        if (member != null && task != null && member.isOverloaded()) {
            createOverloadAlert(member, task);
        }
    }

    private void createOverloadAlert(Member member, Task task) throws SQLException {
        Alert alert = new Alert();
        alert.setType(Alert.AlertType.OVERLOAD);
        alert.setSeverity(Alert.Severity.HIGH);
        alert.setTitle("Member Overload Detected");
        alert.setMessage(String.format(
            "Member '%s' is now overloaded with %.1f hours (%.1f%% capacity) after assigning task '%s'",
            member.getName(), member.getCurrentWorkload(), 
            member.getWorkloadPercentage(), task.getTitle()
        ));
        
        Member alertMember = new Member();
        alertMember.setId(member.getId());
        alertMember.setName(member.getName());
        alert.setMember(alertMember);
        
        Task alertTask = new Task();
        alertTask.setId(task.getId());
        alertTask.setTitle(task.getTitle());
        alert.setTask(alertTask);
        
        alertDAO.create(alert);
    }

    private void createNoSuitableMemberAlert(Task task, int projectId) throws SQLException {
        Alert alert = new Alert();
        alert.setType(Alert.AlertType.CONFLICT);
        alert.setSeverity(Alert.Severity.CRITICAL);
        alert.setTitle("No Suitable Member Found");
        alert.setMessage(String.format(
            "Could not find a suitable member for task '%s'. " +
            "Required skills may not be available or all members are at capacity.",
            task.getTitle()
        ));
        
        Project alertProject = new Project();
        alertProject.setId(projectId);
        alert.setProject(alertProject);
        
        Task alertTask = new Task();
        alertTask.setId(task.getId());
        alertTask.setTitle(task.getTitle());
        alert.setTask(alertTask);
        
        alertDAO.create(alert);
    }

    public static class AllocationResult {
        private final int assignedCount;
        private final int failedCount;
        private final String message;

        public AllocationResult(int assignedCount, int failedCount, String message) {
            this.assignedCount = assignedCount;
            this.failedCount = failedCount;
            this.message = message;
        }

        public int getAssignedCount() {
            return assignedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return failedCount == 0;
        }
    }
    
    private void checkExistingOverloads(int projectId) throws SQLException {
        List<Member> allMembers = memberDAO.findAll();
        List<Task> projectTasks = taskDAO.findByProjectAndStatus(projectId, Task.TaskStatus.TODO);
        
        projectTasks.addAll(taskDAO.findByProjectAndStatus(projectId, Task.TaskStatus.IN_PROGRESS));
        
        for (Member member : allMembers) {
            if (member.isOverloaded()) {
                Task lastTask = null;
                for (Task task : projectTasks) {
                    if (task.getAssignedMember() != null && task.getAssignedMember().getId() == member.getId()) {
                        lastTask = task;
                    }
                }
                
                if (lastTask != null) {
                    logger.info("Creating overload alert for existing overloaded member: {} ({}%)", 
                        member.getName(), String.format("%.1f", member.getWorkloadPercentage()));
                    createOverloadAlert(member, lastTask);
                }
            }
        }
    }
}
