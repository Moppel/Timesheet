package com.uwusoft.timesheet.wizard;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.uwusoft.timesheet.Activator;

public class TaskListPage extends WizardPage {

	IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
	private TableViewer viewer;
	private String project;
	private List<String> tasks;

	protected TaskListPage(String system, String project, List<String> tasks) {
		super("Submission system: " + system);
		setTitle("Project: " + project);
		setDescription("Project " + project + " for system " + system);
		//setControl(text1);
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

	@SuppressWarnings("unchecked")
	public void addTasksToProjects(Map<String, List<String>> projects) {
		projects.put(project, ((StructuredSelection) viewer.getSelection()).toList());
	}
}
