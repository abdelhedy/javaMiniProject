package org.projectmanagement;


import com.google.gson.*;
import com.sun.net.httpserver.*;
import org.projectmanagement.dao.*;
import org.projectmanagement.model.Alert;
import org.projectmanagement.model.Member;
import org.projectmanagement.model.Project;
import org.projectmanagement.model.Task;
import org.projectmanagement.service.TaskAllocationService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;


public class SimpleServer {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return new Date(sdf.parse(json.getAsString()).getTime());
                } catch (Exception e) {
                    return null;
                }
            })
            .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return new JsonPrimitive(sdf.format(src));
            })
            .create();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            try {
                // Load from classpath
                InputStream is = SimpleServer.class.getResourceAsStream("/webapp" + path);

                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    String contentType = path.endsWith(".html") ? "text/html; charset=UTF-8" :
                            path.endsWith(".css") ? "text/css" :
                                    path.endsWith(".js") ? "application/javascript" : "text/plain";
                    exchange.getResponseHeaders().add("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                    is.close();
                } else {
                    String notFound = "<h1>404 - Page not found</h1><p>Path: " + path + "</p>";
                    byte[] bytes = notFound.getBytes("UTF-8");
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(404, bytes.length);
                    exchange.getResponseBody().write(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String error = "<h1>Error</h1><p>" + e.getMessage() + "</p>";
                byte[] bytes = error.getBytes("UTF-8");
                exchange.sendResponseHeaders(500, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });

        // API Members
        server.createContext("/api/members/", exchange -> {
            cors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }

            MemberDAO dao = new MemberDAO();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";

            try {
                // POST /api/members/{id}/skills - Add skill to member
                if ("POST".equals(method) && path.contains("/skills")) {
                    String body = read(exchange);
                    Map<String, Object> data = gson.fromJson(body, Map.class);
                    int memberId = getId(exchange);
                    int skillId = ((Double)data.get("skillId")).intValue();
                    int proficiency = ((Double)data.get("proficiencyLevel")).intValue();
                    dao.addSkill(memberId, skillId, proficiency);
                    response = "{\"success\":true}";
                }
                // DELETE /api/members/{memberId}/skills/{skillId} - Remove skill
                else if ("DELETE".equals(method) && path.contains("/skills")) {
                    String[] parts = path.split("/");
                    int memberId = Integer.parseInt(parts[3]);
                    int skillId = Integer.parseInt(parts[5]);
                    dao.removeSkill(memberId, skillId);
                    response = "{\"success\":true}";
                }
                // Standard CRUD
                else if ("GET".equals(method)) {
                    response = gson.toJson(dao.findAll());
                } else if ("POST".equals(method)) {
                    Member m = gson.fromJson(read(exchange), Member.class);
                    dao.create(m);
                    response = gson.toJson(m);
                } else if ("PUT".equals(method)) {
                    Member m = gson.fromJson(read(exchange), Member.class);
                    dao.update(m);
                    response = gson.toJson(m);
                } else if ("DELETE".equals(method)) {
                    dao.delete(getId(exchange));
                    response = "{\"success\":true}";
                }
                send(exchange, response);
            } catch (Exception e) {
                error(exchange, e);
            }
        });

        // API Projects
        server.createContext("/api/projects/", exchange -> {
            cors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }

            ProjectDAO dao = new ProjectDAO();
            TaskDAO taskDAO = new TaskDAO();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";

            try {
                // GET /api/projects/{id}/tasks - Get tasks for a project
                if ("GET".equals(method) && path.contains("/tasks")) {
                    String[] parts = path.split("/");
                    int projectId = 0;
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].equals("projects") && i + 1 < parts.length) {
                            try {
                                projectId = Integer.parseInt(parts[i + 1]);
                                break;
                            } catch (Exception e) {}
                        }
                    }
                    List<Task> tasks = taskDAO.findByProject(projectId);
                    response = gson.toJson(tasks);
                }
                // GET /api/projects/{id} - Get single project
                else if ("GET".equals(method) && path.matches(".*/\\d+/?$")) {
                    int id = getId(exchange);
                    Project project = dao.findById(id);
                    response = gson.toJson(project);
                }
                // GET /api/projects/ - Get all projects
                else if ("GET".equals(method)) {
                    response = gson.toJson(dao.findAll());
                } else if ("POST".equals(method)) {
                    Project p = gson.fromJson(read(exchange), Project.class);
                    dao.create(p);
                    response = gson.toJson(p);
                } else if ("PUT".equals(method)) {
                    Project p = gson.fromJson(read(exchange), Project.class);
                    dao.update(p);
                    response = gson.toJson(p);
                } else if ("DELETE".equals(method)) {
                    dao.delete(getId(exchange));
                    response = "{\"success\":true}";
                }
                send(exchange, response);
            } catch (Exception e) {
                error(exchange, e);
            }
        });

        // API Tasks
        server.createContext("/api/tasks/", exchange -> {
            cors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }

            TaskDAO dao = new TaskDAO();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";

            try {
                // POST /api/tasks/{taskId}/assign - Manually assign task to member
                if ("POST".equals(method) && path.contains("/assign")) {
                    String body = read(exchange);
                    Map<String, Object> data = gson.fromJson(body, Map.class);
                    int taskId = getId(exchange);
                    int memberId = ((Double)data.get("memberId")).intValue();
                    dao.assignTaskToMember(taskId, memberId);

                    // Vérifier si l'assignation a causé une surcharge et créer une alerte si nécessaire
                    TaskAllocationService allocationService = new TaskAllocationService();
                    allocationService.checkAndCreateOverloadAlert(memberId, taskId);

                    response = "{\"success\":true,\"message\":\"Task assigned successfully\"}";
                }
                // DELETE /api/tasks/{taskId}/assign - Unassign task (only if TODO)
                else if ("DELETE".equals(method) && path.contains("/assign")) {
                    int taskId = getId(exchange);
                    dao.unassignTask(taskId);
                    response = "{\"success\":true,\"message\":\"Task unassigned successfully\"}";
                }
                // Standard CRUD
                else if ("GET".equals(method)) {
                    response = gson.toJson(dao.findById(getId(exchange)));
                } else if ("POST".equals(method)) {
                    Task t = gson.fromJson(read(exchange), Task.class);
                    dao.create(t);
                    response = gson.toJson(t);
                } else if ("PUT".equals(method)) {
                    Task t = gson.fromJson(read(exchange), Task.class);
                    dao.update(t);
                    response = gson.toJson(t);
                } else if ("DELETE".equals(method)) {
                    dao.delete(getId(exchange));
                    response = "{\"success\":true}";
                }
                send(exchange, response);
            } catch (Exception e) {
                error(exchange, e);
            }
        });

        // API Skills
        server.createContext("/api/skills/", exchange -> {
            cors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }

            try {
                SkillDAO dao = new SkillDAO();
                send(exchange, gson.toJson(dao.findAll()));
            } catch (Exception e) {
                error(exchange, e);
            }
        });

        // API Allocation
        server.createContext("/api/allocate/", exchange -> {
            cors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }

            try {
                TaskAllocationService service = new TaskAllocationService();
                TaskAllocationService.AllocationResult result = service.allocateTasks(getId(exchange));
                Map<String, Object> map = new HashMap<>();
                map.put("success", result.getAssignedCount() > 0);
                map.put("assignedCount", result.getAssignedCount());
                map.put("failedCount", result.getFailedCount());
                map.put("message", result.getMessage());
                send(exchange, gson.toJson(map));
            } catch (Exception e) {
                error(exchange, e);
            }
        });

        // API Alerts
        server.createContext("/api/alerts/", exchange -> {
            cors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }

            AlertDAO dao = new AlertDAO();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String response = "";

            try {
                if ("GET".equals(method) && path.endsWith("/count")) {
                    List<Alert> unread = dao.findAll(true);
                    response = "{\"count\":" + unread.size() + "}";
                } else if ("GET".equals(method) && query != null && query.contains("unread=true")) {
                    response = gson.toJson(dao.findAll(true));
                } else if ("GET".equals(method)) {
                    response = gson.toJson(dao.findAll());
                } else if ("DELETE".equals(method)) {
                    dao.delete(getId(exchange));
                    response = "{\"success\":true}";
                } else if ("PUT".equals(method) && path.contains("/read")) {
                    dao.markAsRead(getId(exchange));
                    response = "{\"success\":true}";
                }
                send(exchange, response);
            } catch (Exception e) {
                error(exchange, e);
            }
        });

        // API Statistics
//        server.createContext("/api/statistics/", exchange -> {
//            cors(exchange);
//            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(200, -1); exchange.close(); return; }
//
//            StatisticsService service = new StatisticsService();
//            String path = exchange.getRequestURI().getPath();
//            String response = "";
//
//            try {
//                if (path.contains("/workload")) {
//                    response = gson.toJson(service.getMemberWorkloadStatistics());
//                } else if (path.matches(".*/project/\\d+/?")) {
//                    int id = Integer.parseInt(path.split("/")[4]);
//                    response = gson.toJson(service.getProjectStatistics(id));
//                } else {
//                    response = gson.toJson(service.getOverallStatistics());
//                }
//                send(exchange, response);
//            } catch (Exception e) {
//                error(exchange, e);
//            }
//        });

        server.start();
        System.out.println("\n======================================");
        System.out.println("  Serveur demarre !");
        System.out.println("======================================");
        System.out.println("\nURL: http://localhost:8080\n");
        System.out.println("Appuyez sur Ctrl+C pour arreter\n");
    }

    static void cors(HttpExchange ex) {
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static String read(HttpExchange ex) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(ex.getRequestBody(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    static int getId(HttpExchange ex) {
        String[] parts = ex.getRequestURI().getPath().split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            try { return Integer.parseInt(parts[i]); } catch (Exception e) {}
        }
        return 0;
    }

    static void send(HttpExchange ex, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    static void error(HttpExchange ex, Exception e) throws IOException {
        e.printStackTrace();
        String err = "{\"success\":false,\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        byte[] bytes = err.getBytes("UTF-8");
        ex.sendResponseHeaders(500, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}