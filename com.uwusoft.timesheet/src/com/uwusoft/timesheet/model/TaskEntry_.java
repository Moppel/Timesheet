package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TaskEntry.class)
public abstract class TaskEntry_ {

	public static volatile SingularAttribute<TaskEntry, Float> total;
	public static volatile SingularAttribute<TaskEntry, Long> id;
	public static volatile SingularAttribute<TaskEntry, Boolean> status;
	public static volatile SingularAttribute<TaskEntry, Timestamp> dateTime;
	public static volatile SingularAttribute<TaskEntry, Boolean> allDay;
	public static volatile SingularAttribute<TaskEntry, Task> task;
	public static volatile SingularAttribute<TaskEntry, Boolean> syncStatus;
	public static volatile SingularAttribute<TaskEntry, String> comment;
	public static volatile SingularAttribute<TaskEntry, Long> rowNum;

}

