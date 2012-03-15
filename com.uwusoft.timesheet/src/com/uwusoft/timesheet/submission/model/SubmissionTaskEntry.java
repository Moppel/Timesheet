package com.uwusoft.timesheet.submission.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class SubmissionTaskEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Timestamp dateTime;
    @ManyToOne
	private SubmissionTask task;
	private String comment;
	private float total=0;
	private boolean status = false;
	
	/**
	 * JPA requires a no-arg constructor
	 */
	public SubmissionTaskEntry() {
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

	public SubmissionTask getTask() {
		return task;
	}
	
	public void setTask(SubmissionTask task) {
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

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
}
