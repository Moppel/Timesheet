package com.uwusoft.timesheet.model;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Date dateTime;
	private String task;
	private boolean wholeDay;

	public Date getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	
	public String getTask() {
		return task;
	}
	
	public void setTask(String task) {
		this.task = task;
	}

	public boolean isWholeDay() {
		return wholeDay;
	}

	public void setWholeDay(boolean wholeDay) {
		this.wholeDay = wholeDay;
	}
	
	@Override
	public String toString() {
		return "Task [Date=" + dateTime + ", task=" + task + "]";
	}
}
