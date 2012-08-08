package com.uwusoft.timesheet.submission;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.submission.model.SubmissionTaskEntry;
import com.uwusoft.timesheet.util.MessageBox;

public class LocalSubmissionService implements SubmissionService {
	private EntityManager em;
	private static EntityManagerFactory factory;
	
	static {
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.jdbc.url",
				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/submission;create=true");
		factory = Persistence.createEntityManagerFactory("submission", configOverrides);
	}

	public LocalSubmissionService() {
		em = factory.createEntityManager();
		Query q = em.createQuery("select p from SubmissionProject p");
		if (q.getResultList().isEmpty())
		{
			em.getTransaction().begin();
			SubmissionProject project = new SubmissionProject("Overhead");
			em.persist(project);
			SubmissionTask task = new SubmissionTask("General Administration & Time Entry", project);
			em.persist(task);
			task = new SubmissionTask("Vacation", project);
			em.persist(task);
			task = new SubmissionTask("Time in Lieu", project);
			em.persist(task);
			task = new SubmissionTask("Sickness", project);
			em.persist(task);
			task = new SubmissionTask("Statutory Holiday", project);
			em.persist(task);
			project = new SubmissionProject("Sample Project");
			em.persist(project);
			task = new SubmissionTask("Sample Task 1", project);
			em.persist(task);
			task = new SubmissionTask("Sample Task 2", project);
			em.persist(task);
			task = new SubmissionTask("Sample Task 3", project);
			em.persist(task);
			em.getTransaction().commit();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, SubmissionProject> getAssignedProjects() {
		Map<String, SubmissionProject> assignedProjects = new HashMap<String, SubmissionProject>();
		Query q = em.createQuery("select p from SubmissionProject p");
		for (SubmissionProject project : (List<SubmissionProject>)q.getResultList())
			assignedProjects.put(project.getName(), project);
		return assignedProjects;
	}

	@Override
	public void submit(Date date, SubmissionEntry task, Double total) {
		if (task.getId() == null) return;
		em.getTransaction().begin();
		SubmissionTaskEntry entry = new SubmissionTaskEntry();
		entry.setDateTime(new Timestamp(date.getTime()));
		entry.setTotal(total.floatValue());
		@SuppressWarnings("unchecked")
		List<SubmissionTask> tasks = em.createQuery("select t from SubmissionTask t where t.id = :id")
				.setParameter("id", entry.getTask().getId()).getResultList();
		if (tasks.isEmpty()) return;
		entry.setTask(tasks.iterator().next());
		em.persist(entry);
		em.getTransaction().commit();
		
		MessageBox.setMessage(getClass().getSimpleName(), "Task " + task.getName() + " successfully submitted.");
	}

	@Override
	public void openUrl() {
	}

	@Override
	public String getPreferencePageId() {
		return null;
	}
}
