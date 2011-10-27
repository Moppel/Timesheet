package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Task {
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Timestamp dateTime;
	private String task;
	private float total=0;
	private boolean wholeDay=false;	
	
	/**
	 * JPA requires a no-arg constructor
	 */
	protected Task() {
	}	

	/**
	 * @param dateTime
	 * @param task
	 */
	public Task(Date dateTime, String task) {
		this.dateTime = new Timestamp(dateTime.getTime());
		this.task = task;
	}

	/**
	 * @param dateTime
	 * @param task
	 * @param total
	 */
	public Task(Date dateTime, String task, float total) {
		this(dateTime, task);
		this.total = total;
	}

	/**
	 * @param dateTime
	 * @param task
	 * @param total
	 * @param wholeDay
	 */
	public Task(Date dateTime, String task, float total, boolean wholeDay) {
		this(dateTime, task, total);
		this.wholeDay = wholeDay;
	}

	public Timestamp getDateTime() {
		return dateTime;
	}

	public void setDateTime(Timestamp dateTime) {
		this.dateTime = dateTime;
	}

	public String getTask() {
		return task;
	}
	
	public float getTotal() {
		return total;
	}

	public boolean isWholeDay() {
		return wholeDay;
	}

	@Override
	public String toString() {
		return "Task [Date=" + dateTime + ", task=" + task + ", total=" + total + ", wholeDay=" + wholeDay + "]";
	}
}
