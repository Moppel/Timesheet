package com.uwusoft.timesheet.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
    @ManyToOne
	private Project project;
    private Long externalId;

	/**
	 * JPA requires a no-arg constructor
	 */
	protected Task() {
	}	

	/**
	 * @param name
	 */
	public Task(String name) {
		this.name = name;
	}
	
	/**
	 * @param name
	 * @param project
	 */
	public Task(String name, Project project) {
		this.name = name;
		this.project = project;
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

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	@Override
	public String toString() {
		return "Task [name=" + name + (project == null ? "" : " (project: " + project.getName() + ", system: " + project.getSystem() + ")") + "]";
	}
	
	public String display() {
		return name + (project == null ? "" : "\nProject: " + project.getName() + "\nSystem: " + project.getSystem()); 		
	}
}
