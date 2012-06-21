package com.uwusoft.timesheet.extensionpoint.model;

/**
 * 
 * 
 * @author wunut
 */
public class SubmissionEntry {
	private Long id, projectId;
	private String name, projectName, system;
	
	public SubmissionEntry(Long projectId, Long id, String name, String projectName, String system) {
		this.projectId = projectId;
		this.id = id;
		this.name = name;
		this.projectName = projectName;
		this.system = system;
	}

	public Long getId() {
		return id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getName() {
		return name;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getSystem() {
		return system;
	}
}
