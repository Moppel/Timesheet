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
	@SuppressWarnings("unused")
	private boolean syncStatus = false;
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
	
	public void setSyncStatus(boolean syncStatus) {
		this.syncStatus = syncStatus;
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
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + name.hashCode();
		if (project == null) return result; 
		result = 31 * result + project.getName().hashCode();
		result = 31 * result + project.getSystem().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Task)) return false;
		Task task = (Task) obj;
		return name.equals(task.getName())
				&& project.getName().equals(task.getProject().getName())
				&& project.getSystem().equals(task.getProject().getSystem());
	}

	public String display() {
		return name + (project == null ? "" : "\nProject: " + project.getName() + "\nSystem: " + project.getSystem()); 		
	}
}
