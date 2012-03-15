package com.uwusoft.timesheet.submission.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class SubmissionTask {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
    @ManyToOne
	private SubmissionProject project;
	
	/**
	 * JPA requires a no-arg constructor
	 */
	protected SubmissionTask() {
	}	

	/**
	 * @param name
	 */
	public SubmissionTask(String name) {
		this.name = name;
	}
	
	/**
	 * @param name
	 * @param project
	 */
	public SubmissionTask(String name, SubmissionProject project) {
		this.name = name;
		this.project = project;
	}
		
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public SubmissionProject getProject() {
		return project;
	}

	public void setProject(SubmissionProject project) {
		this.project = project;
	}

	@Override
	public String toString() {
		return "Task [name=" + name + (project == null ? "" : " (project: " + project.getName() + ")]");
	}
	
	public String display() {
		return name + (project == null ? "" : " (" + project.getName() + ")"); 		
	}
}
