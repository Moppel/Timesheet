package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class AllDayTaskEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Timestamp fromDate;
	private Timestamp toDate;
    @ManyToOne
	private Task task;
	@SuppressWarnings("unused")
	private boolean syncStatus = false;
    private String externalId;
	
    /**
	 * JPA requires a no-arg constructor
	 */
	public AllDayTaskEntry() {
	}	

	/**
	 * @param from
	 * @param to
	 * @param task
	 */
	public AllDayTaskEntry(Date from, Date to, String externalId, Task task) {
		if (from != null) this.fromDate = new Timestamp(from.getTime());
		if (to != null) this.toDate = new Timestamp(to.getTime());
		this.externalId = externalId;
		this.task = task;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	public Timestamp getFrom() {
		return fromDate;
	}

	public Timestamp getTo() {
		return toDate;
	}

	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
        //Task oldTask = this.task;
		this.task = task;
        //firePropertyChange(PROPERTY_TASK, oldTask, task);
	}

	public void setSyncStatus(boolean syncStatus) {
		this.syncStatus = syncStatus;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String toString() {
		return "AllDAyTaskEntry [from=" + fromDate + ", to=" + toDate + ", task=" + task.getName() + (task.getProject() == null ? "" : " (project: " + task.getProject().getName() + ", system: " + task.getProject().getSystem() + ")") + "]";
	}
}
