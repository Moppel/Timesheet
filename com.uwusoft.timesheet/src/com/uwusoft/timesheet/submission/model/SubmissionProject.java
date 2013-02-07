package com.uwusoft.timesheet.submission.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class SubmissionProject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@OneToMany(fetch=FetchType.EAGER)
	private List<SubmissionTask> tasks = new ArrayList<SubmissionTask>();

	/**
	 * JPA requires a no-arg constructor
	 */
	public SubmissionProject() {
	}

	/**
	 * 
	 * @param name
	 */
	public SubmissionProject(String name) {
		this.name = name;
	}

	public SubmissionProject(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SubmissionTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<SubmissionTask> tasks) {
		this.tasks = tasks;
	}

	public void addTask(SubmissionTask task) {
		tasks.add(task);
	}

	public void removeTask(SubmissionTask task) {
		tasks.remove(task);
	}
}
