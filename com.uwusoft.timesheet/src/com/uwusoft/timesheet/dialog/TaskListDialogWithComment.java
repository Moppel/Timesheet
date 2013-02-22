package com.uwusoft.timesheet.dialog;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.uwusoft.timesheet.model.Task;

public class TaskListDialogWithComment extends TaskListDialog {
    private Text commentText;
    private AutoCompleteField commentCompleteField;
    private String comment;

    public TaskListDialogWithComment(Shell shell, Task taskSelected, String comment) {
    	super(shell, taskSelected);
    	this.comment = comment;
    }
    
    @Override
	protected void setComment(Composite parent) {
		(new Label(parent, SWT.NULL)).setText("Comment: ");
		commentText = new Text(parent, SWT.NONE);
		commentCompleteField = new AutoCompleteField(commentText, new TextContentAdapter(),
				storageService.getUsedCommentsForTask(task, taskSelected.getProject().getName(), systemSelected));
		commentText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		commentText.setText(comment == null ? "" : comment);
		commentText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!StringUtils.isEmpty(task))
				comment = commentText.getText();
			}        	
		});
		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				//setProposals.cancel();
				ISelection rawSelection = getTableViewer().getSelection();
				if (rawSelection != null
						&& rawSelection instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) rawSelection;
					if (selection.size() == 1) {
						task = (String) selection.getFirstElement();
						commentCompleteField.setProposals(storageService.getUsedCommentsForTask(task, projectSelected, systemSelected));
					}
				}
			}			
		});
	}

	public String getComment() {
		return comment;
	}	
}
