package com.uwusoft.timesheet.wizard;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;

public class TaskListPage extends WizardPage {

	private TableViewer viewer;
	private String project;
	private Set<SubmissionEntry> tasks;

	protected TaskListPage(String system, String project, Set<SubmissionEntry> tasks) {
		super("Submission system: " + system);
		setTitle("Project: " + project);
		setDescription("System: " + system);
		this.project = project;
		this.tasks = tasks;
	}

	@Override
	public void createControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setWidth(300);
		column.setResizable(true);
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
            public String getText(Object element) {
		        return ((SubmissionEntry) element).getName();
			}
			public Image getImage(Object obj) {
				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
			}
		});
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setInput(tasks);
		// Required to avoid an error in the system
		setControl(parent);
		setPageComplete(true);
	}

	@SuppressWarnings("unchecked")
	public void addTasksToProjects(Map<String, Set<SubmissionEntry>> projects) {
		projects.put(project, new HashSet<SubmissionEntry>(((StructuredSelection) viewer.getSelection()).toList()));
	}
}
