package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class TaskEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Timestamp dateTime;
    @ManyToOne
	private Task task;
	private String comment;
	private float total=0;
	private boolean wholeDay=false;	
	private boolean status = false;
	
	/**
	 * JPA requires a no-arg constructor
	 */
	public TaskEntry() {
	}	

	/**
	 * @param dateTime
	 * @param name
	 */
	public TaskEntry(Date dateTime, String name) {
		if (dateTime != null) this.dateTime = new Timestamp(dateTime.getTime());
		task = new Task(name);
	}
	
	/**
	 * @param dateTime
	 * @param name
	 * @param project
	 */
	public TaskEntry(Date dateTime, String name, Project project) {
		this(dateTime, name);
		task.setProject(project);
	}
	
	/**
	 * @param id
	 * @param name
	 * @param comment
	 * @param project
	 */

	public TaskEntry(Long id, String name, Project project) {
		this.id = id;
		task = new Task(name);
		task.setProject(project);
	}

	/**
	 * @param id
	 * @param dateTime
	 * @param task
	 * @param project
	 */
	public TaskEntry(Long id, Date dateTime, String task, float total, Project project) {
		this(dateTime, task, project);
		this.id = id;
		this.total = total;
	}

	/**
	 * @param dateTime
	 * @param task
	 * @param total
	 */
	public TaskEntry(Date dateTime, String task, float total) {
		this(dateTime, task);
		this.total = total;
	}

	/**
	 * @param dateTime
	 * @param task
	 * @param project
	 * @param total
	 */
	public TaskEntry(Date dateTime, String task, Project project, float total) {
		this(dateTime, task, project);
		this.total = total;
	}
	
	/**
	 * @param dateTime
	 * @param task
	 * @param total
	 * @param wholeDay
	 */
	public TaskEntry(Date dateTime, String task, float total, boolean wholeDay) {
		this(dateTime, task, total);
		this.wholeDay = wholeDay;
	}	
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	public Timestamp getDateTime() {
		return dateTime;
	}

	public void setDateTime(Timestamp dateTime) {
		this.dateTime = dateTime;
	}

	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public float getTotal() {
		return total;
	}

	public void setTotal(float total) {
		this.total = total;
	}

	public boolean isWholeDay() {
		return wholeDay;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Task [Date=" + dateTime + ", task=" + task.getName() + (task.getProject() == null ? "" : " (project: " + task.getProject().getName() + ", system: " + task.getProject().getSystem() + ")") + ", total=" + total + ", wholeDay=" + wholeDay + "]";
	}
	
	public String display() {
		return task.getName() + (task.getProject() == null ? "" : " (" + task.getProject().getName() + ")" + "\nSystem: " + task.getProject().getSystem()); 		
	}
}
