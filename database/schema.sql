-- Database Schema for Collaborative Project Management Platform
-- Drop existing database and create fresh
DROP DATABASE IF EXISTS project_management;
CREATE DATABASE project_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE project_management;

-- Table: members
CREATE TABLE members (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         email VARCHAR(100) UNIQUE NOT NULL,
                         weekly_availability INT NOT NULL DEFAULT 40, -- hours per week
                         current_workload DECIMAL(10, 2) DEFAULT 0.0, -- current hours assigned
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table: skills
CREATE TABLE skills (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(50) UNIQUE NOT NULL,
                        description VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table: member_skills (many-to-many relationship)
CREATE TABLE member_skills (
                               member_id INT NOT NULL,
                               skill_id INT NOT NULL,
                               proficiency_level INT NOT NULL DEFAULT 1, -- 1-5 scale
                               PRIMARY KEY (member_id, skill_id),
                               FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                               FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: projects
CREATE TABLE projects (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          start_date DATE NOT NULL,
                          deadline DATE NOT NULL,
                          status ENUM('PLANNING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNING',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table: tasks
CREATE TABLE tasks (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       project_id INT NOT NULL,
                       title VARCHAR(200) NOT NULL,
                       description TEXT,
                       estimated_hours DECIMAL(10, 2) NOT NULL,
                       priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
                       status ENUM('TODO', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED') DEFAULT 'TODO',
                       start_date DATE,
                       deadline DATE,
                       assigned_member_id INT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
                       FOREIGN KEY (assigned_member_id) REFERENCES members(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Table: task_skills (required skills for tasks)
CREATE TABLE task_skills (
                             task_id INT NOT NULL,
                             skill_id INT NOT NULL,
                             required_level INT NOT NULL DEFAULT 1, -- minimum proficiency required
                             PRIMARY KEY (task_id, skill_id),
                             FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
                             FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: task_dependencies
CREATE TABLE task_dependencies (
                                   task_id INT NOT NULL,
                                   depends_on_task_id INT NOT NULL,
                                   PRIMARY KEY (task_id, depends_on_task_id),
                                   FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
                                   FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: alerts
CREATE TABLE alerts (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        type ENUM('OVERLOAD', 'CONFLICT', 'DELAY', 'DEADLINE', 'INFO') NOT NULL,
                        severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
                        title VARCHAR(200) NOT NULL,
                        message TEXT NOT NULL,
                        member_id INT,
                        project_id INT,
                        task_id INT,
                        is_read BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
                        FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: task_history (for tracking changes)
CREATE TABLE task_history (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              task_id INT NOT NULL,
                              action VARCHAR(50) NOT NULL,
                              old_member_id INT,
                              new_member_id INT,
                              changed_by VARCHAR(100),
                              changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Indexes for performance
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_assigned ON tasks(assigned_member_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_alerts_member ON alerts(member_id);
CREATE INDEX idx_alerts_project ON alerts(project_id);
CREATE INDEX idx_alerts_read ON alerts(is_read);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_members_email ON members(email);

-- Insert default skills
INSERT INTO skills (name, description) VALUES
                                           ('Java Development', 'Backend development with Java'),
                                           ('Frontend Development', 'HTML, CSS, JavaScript development'),
                                           ('UI/UX Design', 'User interface and experience design'),
                                           ('Testing', 'Software testing and QA'),
                                           ('Database Design', 'Database architecture and optimization'),
                                           ('DevOps', 'Deployment and CI/CD'),
                                           ('Project Management', 'Project planning and coordination'),
                                           ('Mobile Development', 'iOS/Android development'),
                                           ('API Development', 'RESTful API design and development'),
                                           ('Documentation', 'Technical documentation and writing');
