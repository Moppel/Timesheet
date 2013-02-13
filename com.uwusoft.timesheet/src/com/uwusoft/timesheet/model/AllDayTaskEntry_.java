package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AllDayTaskEntry.class)
public abstract class AllDayTaskEntry_ {

	public static volatile SingularAttribute<AllDayTaskEntry, Timestamp> to;
	public static volatile SingularAttribute<AllDayTaskEntry, Long> id;
	public static volatile SingularAttribute<AllDayTaskEntry, Task> task;
	public static volatile SingularAttribute<AllDayTaskEntry, Timestamp> from;
	public static volatile SingularAttribute<Task, Boolean> syncStatus;
	public static volatile SingularAttribute<Task, String> externalId;

}

