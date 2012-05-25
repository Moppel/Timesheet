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
	 * @param task
	 */
	public TaskEntry(Date dateTime, Task task) {
		if (dateTime != null) this.dateTime = new Timestamp(dateTime.getTime());
		this.task = task;
	}

	/**
	 * @param dateTime
	 * @param task
	 * @param total
	 */
	public TaskEntry(Date dateTime, Task task, float total) {
		this(dateTime, task);
		this.total = total;
	}
	
	/**
	 * @param dateTime
	 * @param task
	 * @param total
	 * @param wholeDay
	 */
	public TaskEntry(Date dateTime, Task task, float total, boolean wholeDay) {
		this(dateTime, task, total);
		this.wholeDay = wholeDay;
	}	
	
	/**
	 * @param id
	 * @param task
	 * @param project
	 * @param system
	 */
	public TaskEntry(Long id, String task, String project, String system) {
		this.id = id;
		this.task = new Task(task, new Project(project, system));
	}

	/**
	 * @param id
	 * @param dateTime
	 * @param task
	 * @param project
	 * @param system
	 * @param total
	 * @param isWholeDay TODO
	 */
	public TaskEntry(Long id, Date dateTime, String task, String project, String system, float total, boolean wholeDay) {
		this(dateTime, new Task(task, new Project(project, system)), total);
		this.id = id;
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
		return task.getName() + (task.getProject() == null ? "" : "\nProject: " + task.getProject().getName() + "\nSystem: " + task.getProject().getSystem()); 		
	}
}
