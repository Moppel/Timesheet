package com.uwusoft.timesheet.wizard;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;

public class TaskListPage extends WizardPage {

	private TableViewer viewer;
	private String project;
	private Set<SubmissionTask> tasks;

	protected TaskListPage(String system, String project, Set<SubmissionTask> tasks) {
		super("Submission system: " + system);
		setTitle("Project: " + project);
		setDescription("System: " + system);
		this.project = project;
		this.tasks = tasks;
	}

	@Override
	public void createControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput(tasks);
		// Required to avoid an error in the system
		setControl(parent);
		setPageComplete(true);
	}

	public void addTasksToProjects(Map<String, Set<SubmissionTask>> projects) {
		projects.put(project, tasks); // TODO
	}
}
