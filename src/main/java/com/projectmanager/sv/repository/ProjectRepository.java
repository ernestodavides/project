package com.projectmanager.sv.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.projectmanager.sv.db.DatabaseManager;
import com.projectmanager.sv.model.DeliverableSummary;
import com.projectmanager.sv.model.DeliverableTaskItem;
import com.projectmanager.sv.model.ProjectSummary;
import com.projectmanager.sv.model.Sprint;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectRepository {

    public List<ProjectSummary> findAllProjectsWithProgress() {
        String sql = """
                select
                    p.id,
                    p.title,
                    p.mission,
                    coalesce(avg(deliverable_summary.deliverable_progress), 0.00) as project_progress,
                    min(deliverable_summary.first_sprint_date) as start_date,
                    max(deliverable_summary.last_sprint_date) as end_date
                from projects p
                left join (
                    select
                        d.project_id,
                        d.id as deliverable_id,
                        coalesce(avg(t.progress), 0.00) as deliverable_progress,
                        min(s.start_date) as first_sprint_date,
                        max(s.end_date) as last_sprint_date
                    from deliverables d
                    left join deliverable_tasks t
                        on t.deliverable_id = d.id
                    left join sprints s
                        on s.id = t.sprint_id
                    group by d.project_id, d.id
                ) deliverable_summary
                    on deliverable_summary.project_id = p.id
                group by p.id, p.title, p.mission
                order by p.id
                """;

        List<ProjectSummary> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ProjectSummary(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("mission"),
                        rs.getDouble("project_progress"),
                        rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            log.error("Failed to load projects", e);
            throw new RuntimeException("Error loading projects", e);
        }
        return list;
    }

    public void createProject(String title, String mission) {
        String sql = "insert into projects (title, mission) values (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, mission.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create project", e);
            throw new RuntimeException("Error creating project", e);
        }
    }

    public void updateProject(long id, String title, String mission) {
        String sql = "update projects set title = ?, mission = ? where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, mission.trim());
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update project {}", id, e);
            throw new RuntimeException("Error updating project", e);
        }
    }

    public void deleteProject(long id) {
        String sql = "delete from projects where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete project {}", id, e);
            throw new RuntimeException("Error deleting project", e);
        }
    }

    public List<DeliverableSummary> findDeliverablesByProject(long projectId) {
        String sql = """
                select
                    d.id,
                    d.project_id,
                    d.name,
                    d.goal,
                    coalesce(avg(t.progress), 0.00) as deliverable_progress,
                    max(s.end_date) as end_date
                from deliverables d
                left join deliverable_tasks t
                    on t.deliverable_id = d.id
                left join sprints s
                    on s.id = t.sprint_id
                where d.project_id = ?
                group by d.id, d.project_id, d.name, d.goal
                order by d.id
                """;

        List<DeliverableSummary> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DeliverableSummary(
                            rs.getLong("id"),
                            rs.getLong("project_id"),
                            rs.getString("name"),
                            rs.getString("goal"),
                            rs.getDouble("deliverable_progress"),
                            rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load deliverables for project {}", projectId, e);
            throw new RuntimeException("Error loading deliverables", e);
        }
        return list;
    }

    public void createDeliverable(long projectId, String name, String goal) {
        String sql = "insert into deliverables (project_id, name, goal) values (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            ps.setString(2, name.trim());
            ps.setString(3, goal == null ? null : goal.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create deliverable for project {}", projectId, e);
            throw new RuntimeException("Error creating deliverable", e);
        }
    }

    public void deleteDeliverable(long deliverableId) {
        String sql = "delete from deliverables where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, deliverableId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete deliverable {}", deliverableId, e);
            throw new RuntimeException("Error deleting deliverable", e);
        }
    }

    public List<Sprint> findAllSprints() {
        String sql = "select id, sprint_number, start_date, end_date from sprints order by sprint_number";
        List<Sprint> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Sprint(
                        rs.getLong("id"),
                        rs.getInt("sprint_number"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            log.error("Failed to load sprints", e);
            throw new RuntimeException("Error loading sprints", e);
        }
        return list;
    }

    public void createSprint(int sprintNumber, LocalDate startDate, LocalDate endDate) {
        String sql = "insert into sprints (sprint_number, start_date, end_date) values (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sprintNumber);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create sprint {}", sprintNumber, e);
            throw new RuntimeException("No se pudo crear el sprint. Verifique que el número no esté repetido.", e);
        }
    }

    public void updateSprint(long id, int sprintNumber, LocalDate startDate, LocalDate endDate) {
        String sql = "update sprints set sprint_number = ?, start_date = ?, end_date = ? where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sprintNumber);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update sprint {}", id, e);
            throw new RuntimeException("No se pudo actualizar el sprint. Verifique que el número no esté repetido.", e);
        }
    }

    public void deleteSprint(long id) {
        String sql = "delete from sprints where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete sprint {}", id, e);
            throw new RuntimeException("No se pudo eliminar el sprint. Un sprint no puede eliminarse si tiene tareas asignadas.", e);
        }
    }

    public List<DeliverableTaskItem> findTasksByDeliverable(long deliverableId) {
        String sql = """
                select
                    t.id,
                    t.deliverable_id,
                    d.name as deliverable_name,
                    p.id as project_id,
                    p.title as project_title,
                    t.sprint_id,
                    s.sprint_number,
                    s.end_date,
                    t.title,
                    t.progress
                from deliverable_tasks t
                join deliverables d on d.id = t.deliverable_id
                join projects p on p.id = d.project_id
                join sprints s on s.id = t.sprint_id
                where t.deliverable_id = ?
                order by s.sprint_number, t.id
                """;
        return queryTasks(sql, deliverableId);
    }

    public List<DeliverableTaskItem> findTasksBySprint(long sprintId) {
        String sql = """
                select
                    t.id,
                    t.deliverable_id,
                    d.name as deliverable_name,
                    p.id as project_id,
                    p.title as project_title,
                    t.sprint_id,
                    s.sprint_number,
                    s.end_date,
                    t.title,
                    t.progress
                from deliverable_tasks t
                join deliverables d on d.id = t.deliverable_id
                join projects p on p.id = d.project_id
                join sprints s on s.id = t.sprint_id
                where t.sprint_id = ?
                order by p.title, d.name, t.id
                """;
        return queryTasks(sql, sprintId);
    }

    private List<DeliverableTaskItem> queryTasks(String sql, long paramId) {
        List<DeliverableTaskItem> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paramId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DeliverableTaskItem(
                            rs.getLong("id"),
                            rs.getLong("deliverable_id"),
                            rs.getString("deliverable_name"),
                            rs.getLong("project_id"),
                            rs.getString("project_title"),
                            rs.getLong("sprint_id"),
                            rs.getInt("sprint_number"),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getString("title"),
                            rs.getDouble("progress")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load deliverable tasks", e);
            throw new RuntimeException("Error loading deliverable tasks", e);
        }
        return list;
    }

    public void createTask(long deliverableId, long sprintId, String title, double progress) {
        String sql = "insert into deliverable_tasks (deliverable_id, sprint_id, title, progress) values (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, deliverableId);
            ps.setLong(2, sprintId);
            ps.setString(3, title.trim());
            ps.setDouble(4, progress);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create task", e);
            throw new RuntimeException("Error creating task", e);
        }
    }

    public void updateTaskProgress(long taskId, double progress) {
        String sql = "update deliverable_tasks set progress = ? where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, progress);
            ps.setLong(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update task {} progress", taskId, e);
            throw new RuntimeException("Error updating task progress", e);
        }
    }

    public void deleteTask(long taskId) {
        String sql = "delete from deliverable_tasks where id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete task {}", taskId, e);
            throw new RuntimeException("Error deleting task", e);
        }
    }
}
