package com.uwusoft.timesheet.extensionpoint.model;

public class TaskEntry {
	private String time, task;
	private Long id;

	/**
	 * @param time
	 * @param task
	 * @param id
	 */
	public TaskEntry(String time, String task, Long id) {
		this.time = time;
		this.task = task;
		this.id = id;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the task
	 */
	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
}
