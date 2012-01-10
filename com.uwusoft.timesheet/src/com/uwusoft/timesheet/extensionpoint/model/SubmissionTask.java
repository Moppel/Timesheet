package com.uwusoft.timesheet.extensionpoint.model;

/**
 * 
 * 
 * @author wunut
 */
public class SubmissionTask {
	private Long id, projectId;
	private String name, projectName;
	
	public SubmissionTask(Long projectId, Long id, String name,	String projectName) {
		this.projectId = projectId;
		this.id = id;
		this.name = name;
		this.projectName = projectName;
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
}
