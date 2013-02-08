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
	public AllDayTaskEntry(Date from, Date to, Task task) {
		if (from != null) this.fromDate = new Timestamp(from.getTime());
		if (to != null) this.toDate = new Timestamp(to.getTime());
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

	@Override
	public String toString() {
		return "AllDAyTaskEntry [from=" + fromDate + ", to=" + toDate + ", task=" + task.getName() + (task.getProject() == null ? "" : " (project: " + task.getProject().getName() + ", system: " + task.getProject().getSystem() + ")") + "]";
	}
}
