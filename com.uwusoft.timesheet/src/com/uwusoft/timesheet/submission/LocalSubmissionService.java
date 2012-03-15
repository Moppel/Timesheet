package com.uwusoft.timesheet.submission;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.persistence.config.SystemProperties;

import com.uwusoft.timesheet.MyArchiveFactoryImpl;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.MessageBox;

public class LocalSubmissionService implements SubmissionService {
	private EntityManager em;
	private static EntityManagerFactory factory;
	
	static {
		System.setProperty(SystemProperties.ARCHIVE_FACTORY, MyArchiveFactoryImpl.class.getName()); // see http://stackoverflow.com/a/7982008
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
	public Map<String, Set<SubmissionEntry>> getAssignedProjects() {
		Map<String, Set<SubmissionEntry>> assignedProjects = new HashMap<String, Set<SubmissionEntry>>();
		Query q = em.createQuery("select p from SubmissionProject p");
		for (SubmissionProject project : (List<SubmissionProject>)q.getResultList()) {
			assignedProjects.put(project.getName(), new HashSet<SubmissionEntry>());
			q = em.createQuery("select t from SubmissionTask t where t.project.id='" + project.getId() + "'");
			for (SubmissionTask task : (List<SubmissionTask>)q.getResultList()) {
				assignedProjects.get(project.getName()).add(new SubmissionEntry(project.getId(), task.getId(), task.getName(), project.getName(), "Local"));
			}
		}
		return assignedProjects;
	}

	@Override
	public void submit(Date date, SubmissionEntry task, Double total) {
		
		MessageBox.setMessage(getClass().getSimpleName(), "Task " + task.getName() + " successfully submitted.");
	}

	@Override
	public void openUrl() {
	}
}
