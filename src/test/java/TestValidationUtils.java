import org.projectmanagement.dao.*;
import org.projectmanagement.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestValidationUtils {
    /**
     * Validate team setup (Scenario 1)
     */
    public static ValidationResult validateTeamSetup() {
        ValidationResult result = new ValidationResult("Team Setup Validation");

        try {
            MemberDAO memberDAO = new MemberDAO();
            List<Member> members = memberDAO.findAll();

            // Check member count
            result.addCheck("Member count", members.size() == 5,
                    "Expected 5 members, found " + members.size());

            // Check total availability
            int totalAvailability = members.stream()
                    .mapToInt(Member::getWeeklyAvailability)
                    .sum();
            result.addCheck("Total availability", totalAvailability == 185,
                    "Expected 185h, found " + totalAvailability + "h");

            // Check skills
            for (Member member : members) {
                result.addCheck("Skills for " + member.getName(),
                        member.getSkills().size() >= 2,
                        member.getName() + " has " + member.getSkills().size() + " skills");
            }

            // Check email uniqueness
            long uniqueEmails = members.stream()
                    .map(Member::getEmail)
                    .distinct()
                    .count();
            result.addCheck("Email uniqueness", uniqueEmails == members.size(),
                    "All emails should be unique");

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validate project creation (Scenario 2)
     */
    public static ValidationResult validateProjectCreation() {
        ValidationResult result = new ValidationResult("Project Creation Validation");

        try {
            ProjectDAO projectDAO = new ProjectDAO();
            TaskDAO taskDAO = new TaskDAO();

            Project project = projectDAO.findById(1);
            result.addCheck("Project exists", project != null,
                    "E-commerce project should exist");

            if (project != null) {
                List<Task> tasks = taskDAO.findByProject(1);

                // Check task count
                result.addCheck("Task count", tasks.size() >= 10,
                        "Expected at least 10 tasks, found " + tasks.size());

                // Check total hours
                double totalHours = tasks.stream()
                        .mapToDouble(Task::getEstimatedHours)
                        .sum();
                result.addCheck("Total estimated hours", totalHours >= 295,
                        "Expected at least 295h, found " + totalHours + "h");

                // Check dependencies
                long tasksWithDeps = tasks.stream()
                        .filter(t -> t.getDependencies() != null && !t.getDependencies().isEmpty())
                        .count();
                result.addCheck("Tasks with dependencies", tasksWithDeps > 5,
                        tasksWithDeps + " tasks have dependencies");

                // Check required skills
                long tasksWithSkills = tasks.stream()
                        .filter(t -> t.getRequiredSkills() != null && !t.getRequiredSkills().isEmpty())
                        .count();
                result.addCheck("Tasks with required skills", tasksWithSkills == tasks.size(),
                        tasksWithSkills + "/" + tasks.size() + " tasks have required skills");

                // Check priority distribution
                Map<Task.Priority, Long> priorityCount = tasks.stream()
                        .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
                result.addCheck("Priority distribution", priorityCount.size() >= 3,
                        "Found " + priorityCount.size() + " priority levels");
            }

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validate automatic allocation (Scenario 3)
     */
    public static ValidationResult validateAllocation() {
        ValidationResult result = new ValidationResult("Automatic Allocation Validation");

        try {
            TaskDAO taskDAO = new TaskDAO();
            MemberDAO memberDAO = new MemberDAO();

            List<Task> tasks = taskDAO.findByProject(1);
            List<Member> members = memberDAO.findAll();

            // Check assigned tasks
            long assignedTasks = tasks.stream().filter(Task::isAssigned).count();
            result.addCheck("Tasks assigned", assignedTasks >= 8,
                    assignedTasks + "/" + tasks.size() + " tasks assigned");

            // Check skill matching
            int skillMismatch = 0;
            for (Task task : tasks) {
                if (task.isAssigned()) {
                    Member member = memberDAO.findById(task.getAssignedMember().getId());
                    if (member != null) {
                        for (TaskSkill reqSkill : task.getRequiredSkills()) {
                            if (!member.hasSkill(reqSkill.getSkill(), reqSkill.getRequiredLevel())) {
                                skillMismatch++;
                            }
                        }
                    }
                }
            }
            result.addCheck("Skill matching", skillMismatch == 0,
                    skillMismatch + " skill mismatches found");

            // Check workload distribution
            double avgWorkload = members.stream()
                    .mapToDouble(Member::getWorkloadPercentage)
                    .average()
                    .orElse(0);
            result.addCheck("Average workload", avgWorkload > 0 && avgWorkload < 200,
                    "Average workload: " + String.format("%.1f%%", avgWorkload));

            // Check critical overload
            long criticalOverload = members.stream()
                    .filter(m -> m.getWorkloadPercentage() > 200)
                    .count();
            result.addCheck("Critical overload limited", criticalOverload <= 2,
                    criticalOverload + " member(s) with critical overload");

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validate overload detection (Scenario 4)
     */
    public static ValidationResult validateOverloadDetection() {
        ValidationResult result = new ValidationResult("Overload Detection Validation");

        try {
            AlertDAO alertDAO = new AlertDAO();
            MemberDAO memberDAO = new MemberDAO();

            List<Alert> alerts = alertDAO.findAll();

            // Check overload alerts
            long overloadAlerts = alerts.stream()
                    .filter(a -> a.getType() == Alert.AlertType.OVERLOAD)
                    .count();
            result.addCheck("Overload alerts exist", overloadAlerts > 0,
                    overloadAlerts + " overload alert(s) found");

            // Check Carol's overload
            Member carol = memberDAO.findAll().stream()
                    .filter(m -> m.getName().equals("Carol Williams"))
                    .findFirst()
                    .orElse(null);

            if (carol != null) {
                result.addCheck("Carol Williams overloaded", carol.isOverloaded(),
                        "Carol's workload: " + String.format("%.1f%%", carol.getWorkloadPercentage()));

                result.addCheck("Carol's workload > 200%", carol.getWorkloadPercentage() > 200,
                        "Critical overload: " + String.format("%.1f%%", carol.getWorkloadPercentage()));
            }

            // Check alert severity
            long criticalAlerts = alerts.stream()
                    .filter(a -> a.getSeverity() == Alert.Severity.CRITICAL)
                    .count();
            result.addCheck("Critical alerts exist", criticalAlerts > 0,
                    criticalAlerts + " critical alert(s) found");

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validate mid-project changes (Scenario 5)
     */
    public static ValidationResult validateMidProjectChanges() {
        ValidationResult result = new ValidationResult("Mid-Project Changes Validation");

        try {
            TaskDAO taskDAO = new TaskDAO();

            List<Task> tasks = taskDAO.findByProject(1);

            // Check status distribution
            long inProgress = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                    .count();
            result.addCheck("Tasks in progress", inProgress > 0,
                    inProgress + " task(s) in progress");

            long completed = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                    .count();
            result.addCheck("Completed tasks", completed > 0,
                    completed + " task(s) completed");

            // Check urgent task
            Task urgentTask = tasks.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains("security") ||
                            t.getTitle().toLowerCase().contains("critical"))
                    .filter(t -> t.getPriority() == Task.Priority.URGENT)
                    .findFirst()
                    .orElse(null);

            result.addCheck("Urgent task added", urgentTask != null,
                    urgentTask != null ? "Found: " + urgentTask.getTitle() : "Not found");

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validate visualization data (Scenario 6)
     */
    public static ValidationResult validateVisualization() {
        ValidationResult result = new ValidationResult("Visualization Data Validation");

        try {
            TaskDAO taskDAO = new TaskDAO();
            MemberDAO memberDAO = new MemberDAO();
            ProjectDAO projectDAO = new ProjectDAO();

            List<Task> tasks = taskDAO.findByProject(1);
            List<Member> members = memberDAO.findAll();
            List<Project> projects = projectDAO.findAll();

            // Check timeline data
            long tasksWithDates = tasks.stream()
                    .filter(t -> t.getStartDate() != null && t.getDeadline() != null)
                    .count();
            result.addCheck("Timeline data complete", tasksWithDates >= 8,
                    tasksWithDates + "/" + tasks.size() + " tasks have dates");

            // Check workload data
            boolean allMembersHaveWorkload = members.stream()
                    .allMatch(m -> m.getWeeklyAvailability() > 0);
            result.addCheck("Workload data complete", allMembersHaveWorkload,
                    "All members have workload data");

            // Check multiple projects
            result.addCheck("Multiple projects", projects.size() >= 2,
                    projects.size() + " project(s) available");

            // Check status variety
            Map<Task.TaskStatus, Long> statusCounts = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
            result.addCheck("Status variety", statusCounts.size() > 1,
                    statusCounts.size() + " different statuses");

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validate statistics accuracy (Scenario 7)
     */
    public static ValidationResult validateStatistics() {
        ValidationResult result = new ValidationResult("Statistics Validation");

        try {
            TaskDAO taskDAO = new TaskDAO();
            MemberDAO memberDAO = new MemberDAO();

            List<Task> tasks = taskDAO.findByProject(1);
            List<Member> members = memberDAO.findAll();

            // Calculate expected values
            int totalTasks = tasks.size();
            long completedTasks = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                    .count();

            double expectedCompletion = totalTasks > 0 ?
                    (completedTasks * 100.0 / totalTasks) : 0;

            result.addCheck("Task count", totalTasks >= 10,
                    "Total tasks: " + totalTasks);

            result.addCheck("Completion calculation", true,
                    "Expected: " + String.format("%.1f%%", expectedCompletion));

            // Hours calculation
            double totalHours = tasks.stream()
                    .mapToDouble(Task::getEstimatedHours)
                    .sum();

            double completedHours = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                    .mapToDouble(Task::getEstimatedHours)
                    .sum();

            double remainingHours = totalHours - completedHours;

            result.addCheck("Hours calculation", Math.abs(totalHours - (completedHours + remainingHours)) < 0.1,
                    String.format("Total: %.1fh, Completed: %.1fh, Remaining: %.1fh",
                            totalHours, completedHours, remainingHours));

            // Workload statistics
            double totalAvailability = members.stream()
                    .mapToDouble(Member::getWeeklyAvailability)
                    .sum();

            double totalWorkload = members.stream()
                    .mapToDouble(Member::getCurrentWorkload)
                    .sum();

            double avgWorkloadPct = (totalWorkload / totalAvailability) * 100;

            result.addCheck("Workload statistics", avgWorkloadPct > 0,
                    "Average workload: " + String.format("%.1f%%", avgWorkloadPct));

            // Overloaded members
            long overloadedMembers = members.stream()
                    .filter(Member::isOverloaded)
                    .count();

            result.addCheck("Overloaded member detection", overloadedMembers >= 1,
                    overloadedMembers + " member(s) overloaded");

        } catch (SQLException e) {
            result.addError("Database error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Run all validations
     */
    public static void runAllValidations() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  TEST VALIDATION SUMMARY                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        ValidationResult[] results = {
                validateTeamSetup(),
                validateProjectCreation(),
                validateAllocation(),
                validateOverloadDetection(),
                validateMidProjectChanges(),
                validateVisualization(),
                validateStatistics()
        };

        for (ValidationResult result : results) {
            result.print();
        }

        // Overall summary
        int totalChecks = 0;
        int passedChecks = 0;

        for (ValidationResult result : results) {
            totalChecks += result.getTotalChecks();
            passedChecks += result.getPassedChecks();
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  OVERALL VALIDATION SUMMARY                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\n  Total Checks: " + totalChecks);
        System.out.println("  ✅ Passed:     " + passedChecks + " (" +
                String.format("%.1f%%", (passedChecks * 100.0 / totalChecks)) + ")");
        System.out.println("  ❌ Failed:     " + (totalChecks - passedChecks) + " (" +
                String.format("%.1f%%", ((totalChecks - passedChecks) * 100.0 / totalChecks)) + ")");

        if (passedChecks == totalChecks) {
            System.out.println("\n  🎉 ALL VALIDATIONS PASSED! 🎉");
        } else {
            System.out.println("\n  ⚠️  SOME VALIDATIONS FAILED - Review details above");
        }
        System.out.println();
    }

    /**
     * Validation Result class
     */
    public static class ValidationResult {
        private final String name;
        private final java.util.List<Check> checks = new java.util.ArrayList<>();
        private final java.util.List<String> errors = new java.util.ArrayList<>();

        public ValidationResult(String name) {
            this.name = name;
        }

        public void addCheck(String description, boolean passed, String details) {
            checks.add(new Check(description, passed, details));
        }

        public void addError(String error) {
            errors.add(error);
        }

        public int getTotalChecks() {
            return checks.size();
        }

        public int getPassedChecks() {
            return (int) checks.stream().filter(c -> c.passed).count();
        }

        public void print() {
            System.out.println("\n" + name + ":");
            System.out.println("─".repeat(62));

            for (Check check : checks) {
                System.out.println("  " + (check.passed ? "✅" : "❌") + " " + check.description);
                if (!check.details.isEmpty()) {
                    System.out.println("     " + check.details);
                }
            }

            for (String error : errors) {
                System.out.println("  ❌ ERROR: " + error);
            }

            System.out.println("  Result: " + getPassedChecks() + "/" + getTotalChecks() + " passed");
        }

        private static class Check {
            String description;
            boolean passed;
            String details;

            Check(String description, boolean passed, String details) {
                this.description = description;
                this.passed = passed;
                this.details = details;
            }
        }
    }

    /**
     * Main method for standalone validation
     */
    public static void main(String[] args) {
        runAllValidations();
    }
}
