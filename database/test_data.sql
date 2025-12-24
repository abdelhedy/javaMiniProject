-- Test Data for Collaborative Project Management Platform
-- This file contains sample data to test all scenarios

USE project_management;



-- Clear existing data safely

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM task_history;
DELETE FROM alerts;
DELETE FROM task_dependencies;
DELETE FROM task_skills;
DELETE FROM tasks;
DELETE FROM projects;
DELETE FROM member_skills;
DELETE FROM members;

SET FOREIGN_KEY_CHECKS = 1;

-- Insert test members (Scenario 1)
INSERT INTO members (name, email, weekly_availability, current_workload) VALUES
                                                                             ('Alice Johnson', 'alice.johnson@example.com', 40, 0),
                                                                             ('Bob Smith', 'bob.smith@example.com', 35, 0),
                                                                             ('Carol Williams', 'carol.williams@example.com', 40, 0),
                                                                             ('David Brown', 'david.brown@example.com', 30, 0),
                                                                             ('Emma Davis', 'emma.davis@example.com', 40, 0);

-- Assign skills to members
-- Alice: Java Development (5), Database Design (4)
INSERT INTO member_skills (member_id, skill_id, proficiency_level) VALUES
                                                                       (1, 1, 5), -- Java Development
                                                                       (1, 5, 4); -- Database Design

-- Bob: Frontend Development (5), UI/UX Design (4)
INSERT INTO member_skills (member_id, skill_id, proficiency_level) VALUES
                                                                       (2, 2, 5), -- Frontend Development
                                                                       (2, 3, 4); -- UI/UX Design

-- Carol: Testing (5), Documentation (4)
INSERT INTO member_skills (member_id, skill_id, proficiency_level) VALUES
                                                                       (3, 4, 5), -- Testing
                                                                       (3, 10, 4); -- Documentation

-- David: Java Development (4), API Development (5)
INSERT INTO member_skills (member_id, skill_id, proficiency_level) VALUES
                                                                       (4, 1, 4), -- Java Development
                                                                       (4, 9, 5); -- API Development

-- Emma: UI/UX Design (5), Frontend Development (3)
INSERT INTO member_skills (member_id, skill_id, proficiency_level) VALUES
                                                                       (5, 3, 5), -- UI/UX Design
                                                                       (5, 2, 3); -- Frontend Development

-- Create test project (Scenario 2)
INSERT INTO projects (name, description, start_date, deadline, status) VALUES
    ('E-Commerce Application',
     'Full-stack e-commerce platform with modern UI and robust backend',
     '2025-11-15',
     '2025-12-31',
     'PLANNING');

-- Create tasks for the project
-- Backend tasks (Java Development)
INSERT INTO tasks (project_id, title, description, estimated_hours, priority, status, deadline) VALUES
                                                                                                    (1, 'Setup database schema', 'Design and implement database tables for the e-commerce platform', 8, 'HIGH', 'TODO', '2025-11-20'),
                                                                                                    (1, 'Create REST API endpoints', 'Implement RESTful API for products, cart, and orders', 16, 'HIGH', 'TODO', '2025-11-28'),
                                                                                                    (1, 'Implement authentication', 'Add JWT-based authentication and authorization', 12, 'URGENT', 'TODO', '2025-11-25');

-- Frontend tasks (Frontend Development + Design)
INSERT INTO tasks (project_id, title, description, estimated_hours, priority, status, deadline) VALUES
                                                                                                    (1, 'Design UI mockups', 'Create wireframes and mockups for all pages', 10, 'MEDIUM', 'TODO', '2025-11-22'),
                                                                                                    (1, 'Implement homepage', 'Build responsive homepage with product showcase', 15, 'HIGH', 'TODO', '2025-11-30'),
                                                                                                    (1, 'Create product catalog', 'Develop product listing, filtering, and detail pages', 20, 'HIGH', 'TODO', '2025-12-05');

-- Testing tasks
INSERT INTO tasks (project_id, title, description, estimated_hours, priority, status, deadline) VALUES
                                                                                                    (1, 'Unit tests', 'Write unit tests for backend services', 12, 'MEDIUM', 'TODO', '2025-12-08'),
                                                                                                    (1, 'Integration tests', 'Create integration tests for API endpoints', 16, 'MEDIUM', 'TODO', '2025-12-15'),
                                                                                                    (1, 'User acceptance testing', 'Conduct UAT with stakeholders', 8, 'HIGH', 'TODO', '2025-12-20');

-- Documentation task
INSERT INTO tasks (project_id, title, description, estimated_hours, priority, status, deadline) VALUES
    (1, 'Technical documentation', 'Write comprehensive technical and user documentation', 10, 'LOW', 'TODO', '2025-12-28');

-- Add skill requirements for tasks
-- Task 1: Database schema (Java Dev, DB Design)
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
                                                                (1, 1, 3), -- Java Development
                                                                (1, 5, 3); -- Database Design

-- Task 2: API endpoints (Java Dev, API Dev)
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
                                                                (2, 1, 4), -- Java Development
                                                                (2, 9, 4); -- API Development

-- Task 3: Authentication (Java Dev)
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
    (3, 1, 4); -- Java Development

-- Task 4: UI mockups (UI/UX Design)
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
    (4, 3, 4); -- UI/UX Design

-- Task 5: Homepage (Frontend, UI/UX)
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
                                                                (5, 2, 4), -- Frontend Development
                                                                (5, 3, 3); -- UI/UX Design

-- Task 6: Product catalog (Frontend)
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
    (6, 2, 4); -- Frontend Development

-- Task 7-9: Testing
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
                                                                (7, 4, 4), -- Testing
                                                                (8, 4, 4), -- Testing
                                                                (9, 4, 3); -- Testing

-- Task 10: Documentation
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
    (10, 10, 3); -- Documentation

-- Add task dependencies
INSERT INTO task_dependencies (task_id, depends_on_task_id) VALUES
                                                                (2, 1),  -- API depends on Database
                                                                (3, 2),  -- Auth depends on API
                                                                (5, 4),  -- Homepage depends on Mockups
                                                                (6, 5),  -- Catalog depends on Homepage
                                                                (7, 2),  -- Unit tests depend on API
                                                                (7, 3),  -- Unit tests depend on Auth
                                                                (8, 6),  -- Integration tests depend on Catalog
                                                                (8, 7),  -- Integration tests depend on Unit tests
                                                                (9, 8),  -- UAT depends on Integration tests
                                                                (10, 1), -- Documentation depends on all major tasks
                                                                (10, 2),
                                                                (10, 3),
                                                                (10, 5),
                                                                (10, 6);

-- Additional project for testing (Optional)
INSERT INTO projects (name, description, start_date, deadline, status) VALUES
    ('Mobile App Development',
     'Cross-platform mobile application for the e-commerce platform',
     '2025-12-01',
     '2026-02-28',
     'PLANNING');

-- Sample tasks for mobile project
INSERT INTO tasks (project_id, title, description, estimated_hours, priority, status, deadline) VALUES
                                                                                                    (2, 'Setup React Native project', 'Initialize React Native with necessary dependencies', 6, 'HIGH', 'TODO', '2025-12-05'),
                                                                                                    (2, 'Design mobile UI', 'Create mobile-specific UI designs', 12, 'MEDIUM', 'TODO', '2025-12-10'),
                                                                                                    (2, 'Implement authentication', 'Mobile authentication flow', 10, 'HIGH', 'TODO', '2025-12-15'),
                                                                                                    (2, 'Product browsing feature', 'Mobile product catalog and search', 18, 'HIGH', 'TODO', '2025-12-25'),
                                                                                                    (2, 'Shopping cart', 'Implement shopping cart functionality', 14, 'MEDIUM', 'TODO', '2026-01-05');

-- Add skills for mobile tasks
INSERT INTO task_skills (task_id, skill_id, required_level) VALUES
                                                                (11, 8, 4), -- Mobile Development
                                                                (12, 3, 4), -- UI/UX Design
                                                                (12, 8, 3), -- Mobile Development
                                                                (13, 8, 4), -- Mobile Development
                                                                (14, 8, 4), -- Mobile Development
                                                                (15, 8, 3); -- Mobile Development

-- Note: After running this script, you can:
-- 1. View the members in the Team Members page
-- 2. View the projects in the Projects page
-- 3. Click "Auto-Allocate" to test the allocation algorithm
-- 4. View the timeline to see task distribution
-- 5. Check alerts for any overload warnings
-- 6. View statistics to see workload balance

SELECT 'Test data loaded successfully!' as Status;
SELECT COUNT(*) as MemberCount FROM members;
SELECT COUNT(*) as ProjectCount FROM projects;
SELECT COUNT(*) as TaskCount FROM tasks;
