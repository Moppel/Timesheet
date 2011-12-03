package com.uwusoft.timesheet.kimaisubmission;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class KimaiSubmissionService implements SubmissionService {

	@Override
	public List<String> getAssignedTasks() {
        //Todo implement
    	List<String> assignedTasks = new ArrayList<String>();
    	assignedTasks.add("Sample Task 1");
    	assignedTasks.add("Sample Task 2");
    	assignedTasks.add("Sample Task 3");
    	return assignedTasks;
	}

	@Override
	public void submit(Date date, String task, Double total) {
        //Todo implement
        System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(date) + "\t" + task + "\t" + total);
	}

}
