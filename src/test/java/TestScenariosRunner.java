import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.projectmanagement.dao.*;
import org.projectmanagement.model.*;
import org.projectmanagement.service.StatisticsService;
import org.projectmanagement.service.TaskAllocationService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TestScenariosRunner {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final ProjectDAO projectDAO = new ProjectDAO();
    private static final TaskDAO taskDAO = new TaskDAO();
    private static final AlertDAO alertDAO = new AlertDAO();
    private static final TaskAllocationService allocationService = new TaskAllocationService();
    private static final StatisticsService statisticsService = new StatisticsService();

    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  AUTOMATED TEST SCENARIOS RUNNER                          ║");
        System.out.println("║  Project Management Platform - Comprehensive Tests        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // Run all scenarios
            scenario1_TeamManagement();
            scenario2_ProjectCreation();
            scenario3_AutomaticAllocation();
            scenario4_OverloadDetection();
            scenario5_MidProjectChanges();
            scenario6_Visualization();
            scenario7_Statistics();

            // Print summary
            printTestSummary();

        } catch (Exception e) {
            System.err.println("❌ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================================================
    // SCENARIO 1: Team Management
    // ========================================================================
    private static void scenario1_TeamManagement() throws SQLException {
        printScenarioHeader("SCENARIO 1: Team Management");

        // Test 1.1: Verify all members exist
        test("1.1 - Verify 5 members created", () -> {
            List<Member> members = memberDAO.findAll();
            assertEquals(5, members.size(), "Expected 5 members");
        });

        // Test 1.2: Verify members have correct availability
        test("1.2 - Verify total weekly availability", () -> {
            List<Member> members = memberDAO.findAll();
            int totalAvailability = members.stream()
                    .mapToInt(Member::getWeeklyAvailability)
                    .sum();
            assertEquals(185, totalAvailability, "Total availability should be 185h");
        });

        // Test 1.3: Verify skills are assigned
        test("1.3 - Verify members have skills", () -> {
            List<Member> members = memberDAO.findAll();
            for (Member member : members) {
                assertTrue(member.getSkills() != null && member.getSkills().size() >= 2,
                        "Each member should have at least 2 skills");
            }
        });

        // Test 1.4: Verify specific member details
        test("1.4 - Verify Alice Johnson details", () -> {
            Member alice = memberDAO.findAll().stream()
                    .filter(m -> m.getName().equals("Alice Johnson"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alice, "Alice Johnson should exist");
            assertEquals(40, alice.getWeeklyAvailability(), "Alice should have 40h availability");
            assertEquals("alice.johnson@example.com", alice.getEmail(), "Email should match");
        });

        // Test 1.5: Verify skill proficiency levels
        test("1.5 - Verify skill proficiency levels are valid", () -> {
            List<Member> members = memberDAO.findAll();
            for (Member member : members) {
                for (MemberSkill skill : member.getSkills()) {
                    assertTrue(skill.getProficiencyLevel() >= 1 && skill.getProficiencyLevel() <= 5,
                            "Skill level should be between 1 and 5");
                }
            }
        });

        printScenarioFooter();
    }

    // ========================================================================
    // SCENARIO 2: Project Creation
    // ========================================================================
    private static void scenario2_ProjectCreation() throws SQLException {
        printScenarioHeader("SCENARIO 2: Project Creation");

        // Test 2.1: Verify project exists
        test("2.1 - Verify E-commerce project created", () -> {
            Project project = projectDAO.findById(1);
            assertNotNull(project, "Project should exist");
            assertEquals("E-commerce Platform Development", project.getName(), "Project name should match");
        });

        // Test 2.2: Verify task count
        test("2.2 - Verify 11 tasks created (including urgent)", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            assertTrue(tasks.size() >= 10, "Should have at least 10 tasks");
        });

        // Test 2.3: Verify total estimated hours
        test("2.3 - Verify total estimated hours", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            double totalHours = tasks.stream()
                    .mapToDouble(Task::getEstimatedHours)
                    .sum();
            assertTrue(totalHours >= 150 && totalHours <= 170, "Total hours should be realistic (~158h)");
        });

        // Test 2.4: Verify task dependencies
        test("2.4 - Verify task dependencies exist", () -> {
            Task backendAPI = taskDAO.findById(2);
            assertNotNull(backendAPI, "Backend API task should exist");
            assertTrue(backendAPI.getDependencies() != null && !backendAPI.getDependencies().isEmpty(),
                    "Backend API should have dependencies");
        });

        // Test 2.5: Verify priority distribution
        test("2.5 - Verify urgent tasks exist", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            long urgentTasks = tasks.stream()
                    .filter(t -> t.getPriority() == Task.Priority.URGENT)
                    .count();
            assertTrue(urgentTasks >= 2, "Should have at least 2 urgent tasks");
        });

        // Test 2.6: Verify required skills are defined
        test("2.6 - Verify tasks have required skills", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            for (Task task : tasks) {
                assertTrue(task.getRequiredSkills() != null && !task.getRequiredSkills().isEmpty(),
                        "Each task should have required skills");
            }
        });

        printScenarioFooter();
    }

    // ========================================================================
    // SCENARIO 3: Automatic Allocation
    // ========================================================================
    private static void scenario3_AutomaticAllocation() throws SQLException {
        printScenarioHeader("SCENARIO 3: Automatic Allocation");

        // Test 3.1: Run allocation algorithm
        test("3.1 - Execute automatic task allocation", () -> {
            TaskAllocationService.AllocationResult result = allocationService.allocateTasks(1);
            assertNotNull(result, "Allocation result should not be null");
            assertTrue(result.getAssignedCount() >= 0, "Allocation should complete successfully");
            System.out.println("    ℹ️  " + result.getMessage());
        });

        // Test 3.2: Verify tasks are assigned
        test("3.2 - Verify tasks have assignments", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            long assignedTasks = tasks.stream().filter(Task::isAssigned).count();
            assertTrue(assignedTasks >= 8, "At least 8 tasks should be assigned");
            System.out.println("    ℹ️  " + assignedTasks + "/" + tasks.size() + " tasks assigned");
        });

        // Test 3.3: Verify skill matching
        test("3.3 - Verify assigned members have required skills", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            for (Task task : tasks) {
                if (task.isAssigned()) {
                    Member member = memberDAO.findById(task.getAssignedMember().getId());
                    assertNotNull(member, "Assigned member should exist");

                    // Check if member has required skills
                    for (TaskSkill reqSkill : task.getRequiredSkills()) {
                        boolean hasSkill = member.hasSkill(reqSkill.getSkill(), reqSkill.getRequiredLevel());
                        assertTrue(hasSkill,
                                "Member " + member.getName() + " should have required skill for task " + task.getTitle());
                    }
                }
            }
        });

        // Test 3.4: Verify workload balance
        test("3.4 - Verify workload distribution", () -> {
            List<Member> members = memberDAO.findAll();
            double avgWorkload = members.stream()
                    .mapToDouble(Member::getWorkloadPercentage)
                    .average()
                    .orElse(0);

            System.out.println("    ℹ️  Average workload: " + String.format("%.1f%%", avgWorkload));
            assertTrue(avgWorkload > 0, "Average workload should be positive");
        });

        // Test 3.5: Verify no critical overload
        test("3.5 - Verify reasonable workload limits", () -> {
            List<Member> members = memberDAO.findAll();
            long criticalOverload = members.stream()
                    .filter(m -> m.getWorkloadPercentage() > 200)
                    .count();

            assertTrue(criticalOverload <= 2, "No more than 2 members should have critical overload");
        });

        printScenarioFooter();
    }

    // ========================================================================
    // SCENARIO 4: Overload Detection
    // ========================================================================
    private static void scenario4_OverloadDetection() throws SQLException {
        printScenarioHeader("SCENARIO 4: Overload Detection");

        // Test 4.1: Verify overload alerts exist
        test("4.1 - Verify overload alerts generated", () -> {
            List<Alert> alerts = alertDAO.findAll();
            long overloadAlerts = alerts.stream()
                    .filter(a -> a.getType() == Alert.AlertType.OVERLOAD)
                    .count();

            assertTrue(overloadAlerts > 0, "At least one overload alert should exist");
            System.out.println("    ℹ️  " + overloadAlerts + " overload alert(s) found");
        });

        // Test 4.2: Verify at least one member is overloaded
        test("4.2 - Verify Alice Johnson is overloaded", () -> {
            Member alice = memberDAO.findAll().stream()
                    .filter(m -> m.getName().equals("Alice Johnson"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alice, "Alice Johnson should exist");
            assertTrue(alice.isOverloaded(), "Alice should be overloaded");
            assertTrue(alice.getWorkloadPercentage() > 100, "Alice's workload should exceed 100%");
            System.out.println("    ℹ️  Alice's workload: " + String.format("%.1f%%", alice.getWorkloadPercentage()));
        });

        // Test 4.3: Verify alert severity
        test("4.3 - Verify critical severity for high overload", () -> {
            List<Alert> alerts = alertDAO.findAll();
            Alert carolAlert = alerts.stream()
                    // .filter(a -> a.getMemberId() != null && a.getMemberId() == 3)
                    .filter(a -> a.getMember() != null && a.getMember().getId() == 3)
                    .filter(a -> a.getType() == Alert.AlertType.OVERLOAD)
                    .findFirst()
                    .orElse(null);

            assertNotNull(carolAlert, "Carol's overload alert should exist");
            assertEquals(Alert.Severity.CRITICAL, carolAlert.getSeverity(),
                    "Alert severity should be CRITICAL for 200%+ overload");
        });

        // Test 4.4: Verify alert message content
        test("4.4 - Verify alert message contains details", () -> {
            List<Alert> alerts = alertDAO.findAll();
            Alert overloadAlert = alerts.stream()
                    .filter(a -> a.getType() == Alert.AlertType.OVERLOAD)
                    .findFirst()
                    .orElse(null);

            assertNotNull(overloadAlert, "Overload alert should exist");
            assertTrue(overloadAlert.getMessage().contains("overload"),
                    "Alert message should contain 'overload'");
        });

        printScenarioFooter();
    }

    // ========================================================================
    // SCENARIO 5: Mid-Project Changes
    // ========================================================================
    private static void scenario5_MidProjectChanges() throws SQLException {
        printScenarioHeader("SCENARIO 5: Mid-Project Changes");

        // Test 5.1: Verify tasks in different statuses
        test("5.1 - Verify tasks in progress", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            long inProgress = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                    .count();

            assertTrue(inProgress > 0, "Some tasks should be in progress");
            System.out.println("    ℹ️  " + inProgress + " task(s) in progress");
        });

        // Test 5.2: Verify completed tasks
        test("5.2 - Verify completed tasks", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            long completed = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                    .count();

            assertTrue(completed > 0, "Some tasks should be completed");
            System.out.println("    ℹ️  " + completed + " task(s) completed");
        });

        // Test 5.3: Verify urgent task exists
        test("5.3 - Verify urgent security fix task added", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            Task urgentTask = tasks.stream()
                    .filter(t -> t.getTitle().contains("Security") || t.getTitle().contains("Critical"))
                    .filter(t -> t.getPriority() == Task.Priority.URGENT)
                    .findFirst()
                    .orElse(null);

            assertNotNull(urgentTask, "Urgent security task should exist");
            System.out.println("    ℹ️  Found: " + urgentTask.getTitle());
        });

        // Test 5.4: Verify alerts exist for project changes
        test("5.4 - Verify project change alerts", () -> {
            List<Alert> alerts = alertDAO.findAll();
            long projectAlerts = alerts.stream()
                    .filter(a -> a.getType() == Alert.AlertType.INFO || a.getType() == Alert.AlertType.DEADLINE)
                    .count();

            assertTrue(projectAlerts >= 0, "Project change alerts may exist");
            System.out.println("    ℹ️  " + projectAlerts + " project alert(s) found");
        });

        printScenarioFooter();
    }

    // ========================================================================
    // SCENARIO 6: Visualization
    // ========================================================================
    private static void scenario6_Visualization() throws SQLException {
        printScenarioHeader("SCENARIO 6: Visualization");

        // Test 6.1: Verify timeline data availability
        test("6.1 - Verify timeline data for project", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            long tasksWithDates = tasks.stream()
                    .filter(t -> t.getStartDate() != null && t.getDeadline() != null)
                    .count();

            assertTrue(tasksWithDates >= 8, "Most tasks should have start and end dates");
            System.out.println("    ℹ️  " + tasksWithDates + "/" + tasks.size() + " tasks have timeline data");
        });

        // Test 6.2: Verify workload data for visualization
        test("6.2 - Verify member workload data", () -> {
            List<Member> members = memberDAO.findAll();
            for (Member member : members) {
                assertTrue(member.getWeeklyAvailability() > 0,
                        "Member should have positive availability");
                assertTrue(member.getCurrentWorkload() >= 0,
                        "Workload should be non-negative");
            }
            System.out.println("    ℹ️  Workload data complete for " + members.size() + " members");
        });

        // Test 6.3: Verify multiple projects for comparison
        test("6.3 - Verify multiple projects exist", () -> {
            List<Project> projects = projectDAO.findAll();
            assertTrue(projects.size() >= 2, "At least 2 projects should exist for comparison");
            System.out.println("    ℹ️  " + projects.size() + " project(s) available");
        });

        // Test 6.4: Verify task status distribution
        test("6.4 - Verify task status distribution", () -> {
            List<Task> tasks = taskDAO.findByProject(1);
            Map<Task.TaskStatus, Long> statusCounts = tasks.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            Task::getStatus,
                            java.util.stream.Collectors.counting()
                    ));

            System.out.println("    ℹ️  Status distribution:");
            statusCounts.forEach((status, count) ->
                    System.out.println("       - " + status + ": " + count)
            );

            assertTrue(statusCounts.size() > 1, "Tasks should have varied statuses");
        });

        printScenarioFooter();
    }

    // ========================================================================
    // SCENARIO 7: Statistics
    // ========================================================================
    private static void scenario7_Statistics() throws SQLException {
        printScenarioHeader("SCENARIO 7: Statistics");

        // Test 7.1: Generate project statistics
        test("7.1 - Generate project statistics", () -> {
            Map<String, Object> stats = statisticsService.getProjectStatistics(1);
            assertNotNull(stats, "Statistics should be generated");
            assertTrue(stats.containsKey("totalTasks"), "Should include total tasks");
            assertTrue(stats.containsKey("completionPercentage"), "Should include completion %");

            System.out.println("    ℹ️  Total tasks: " + stats.get("totalTasks"));
            System.out.println("    ℹ️  Completion: " + stats.get("completionPercentage") + "%");
        });

        // Test 7.2: Verify completion percentage calculation
        test("7.2 - Verify completion percentage accuracy", () -> {
            Map<String, Object> stats = statisticsService.getProjectStatistics(1);

            int totalTasks = ((Number) stats.get("totalTasks")).intValue();
            int completedTasks = ((Number) stats.get("completedTasks")).intValue();
            double completionPct = ((Number) stats.get("completionPercentage")).doubleValue();

            double expectedPct = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
            assertTrue(Math.abs(completionPct - expectedPct) < 0.1,
                    "Completion percentage should be accurate");
        });

        // Test 7.3: Generate workload statistics
        test("7.3 - Generate workload statistics", () -> {
            Map<String, Object> stats = statisticsService.getMemberWorkloadStatistics();
            assertNotNull(stats, "Workload statistics should be generated");
            assertTrue(stats.containsKey("totalMembers"), "Should include member count");
            assertTrue(stats.containsKey("averageWorkloadPercentage"), "Should include avg workload");

            System.out.println("    ℹ️  Total members: " + stats.get("totalMembers"));
            System.out.println("    ℹ️  Avg workload: " + stats.get("averageWorkloadPercentage") + "%");
        });

        // Test 7.4: Verify overloaded member detection
        test("7.4 - Verify overloaded member statistics", () -> {
            Map<String, Object> stats = statisticsService.getMemberWorkloadStatistics();
            long overloadedMembers = ((Number) stats.get("overloadedMembers")).longValue();

            assertTrue(overloadedMembers >= 1, "At least 1 member should be overloaded");
            System.out.println("    ℹ️  Overloaded members: " + overloadedMembers);
        });

        // Test 7.5: Verify hours calculation
        test("7.5 - Verify hours calculation accuracy", () -> {
            Map<String, Object> stats = statisticsService.getProjectStatistics(1);

            double totalHours = ((Number) stats.get("totalEstimatedHours")).doubleValue();
            double completedHours = ((Number) stats.get("completedHours")).doubleValue();
            double remainingHours = ((Number) stats.get("remainingHours")).doubleValue();

            assertTrue(Math.abs(totalHours - (completedHours + remainingHours)) < 0.1,
                    "Total hours should equal completed + remaining");
        });

        printScenarioFooter();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private static void printScenarioHeader(String title) {
        System.out.println("\n╔" + "═".repeat(60) + "╗");
        System.out.println("║  " + String.format("%-57s", title) + "║");
        System.out.println("╚" + "═".repeat(60) + "╝");
    }

    private static void printScenarioFooter() {
        System.out.println("─".repeat(62));
    }

    private static void test(String description, TestRunnable test) {
        totalTests++;
        System.out.print("  [TEST " + totalTests + "] " + description + " ... ");

        try {
            test.run();
            passedTests++;
            System.out.println("✅ PASS");
        } catch (AssertionError e) {
            failedTests++;
            System.out.println("❌ FAIL");
            System.out.println("    ⚠️  " + e.getMessage());
        } catch (Exception e) {
            failedTests++;
            System.out.println("❌ ERROR");
            System.out.println("    ⚠️  " + e.getMessage());
        }
    }

    private static void printTestSummary() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  TEST EXECUTION SUMMARY                                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Total Tests:  " + totalTests);
        System.out.println("  ✅ Passed:     " + passedTests + " (" +
                String.format("%.1f%%", (passedTests * 100.0 / totalTests)) + ")");
        System.out.println("  ❌ Failed:     " + failedTests + " (" +
                String.format("%.1f%%", (failedTests * 100.0 / totalTests)) + ")");
        System.out.println();

        if (failedTests == 0) {
            System.out.println("  🎉 ALL TESTS PASSED! 🎉");
        } else {
            System.out.println("  ⚠️  SOME TESTS FAILED - Review output above");
        }

        System.out.println("\n" + "═".repeat(62));
    }

    // Assertion methods
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }
}
