package com.uwusoft.timesheet.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Project {
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String system;
	@OneToMany
	private List<Task> tasks = new ArrayList<Task>();

	/**
	 * JPA requires a no-arg constructor
	 */
	protected Project() {
	}

	/**
	 * 
	 * @param name
	 */
	public Project(String name, String system) {
		this.name = name;
		this.system = system;
	}

	public String getName() {
		return name;
	}

	public String getSystem() {
		return system;
	}

	public List<Task> getTasks() {
		return tasks;
	}
}
