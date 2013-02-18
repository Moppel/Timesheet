package com.uwusoft.timesheet.view;

import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.text.DateFormat;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.util.BusinessDayUtil;

public class AllDayTasksView extends AbstractTasksView {
	public static final String ID = "com.uwusoft.timesheet.view.alldaytasksview";
	
	protected boolean addTaskEntries() {
		if (viewer == null) return false;
		viewer.setInput(LocalStorageService.getInstance().getAllDayTaskEntries());
		return true;
	}

	protected void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "From", "To", "Requested", "Task", "Issue Key" };
		int[] bounds = { 80, 80, 70, 150, 70 };
        
		int colNum = 0;
		// First column is for the from date
		TableViewerColumn col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return DateFormat.getDateInstance(DateFormat.SHORT).format(((AllDayTaskEntry) element).getFrom());
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {			
			public String getText(Object element) {
				Timestamp date = ((AllDayTaskEntry) element).getFrom();
	    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
			}
			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
			}
		});
		// Second column is for the to date
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return DateFormat.getDateInstance(DateFormat.SHORT).format(((AllDayTaskEntry) element).getTo());
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {			
			public String getText(Object element) {
				Timestamp date = ((AllDayTaskEntry) element).getTo();
	    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
			}
			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
			}
		});
		// Third column is for the requested
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return BusinessDayUtil.getRequestedDays(((AllDayTaskEntry) element).getFrom(), ((AllDayTaskEntry) element).getTo());
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
			public String getText(Object element) {
		        return new Integer(BusinessDayUtil.getRequestedDays(((AllDayTaskEntry) element).getFrom(), ((AllDayTaskEntry) element).getTo())).toString();
			}
			public Image getImage(Object obj) {
	    		return null;
			}
		});
		// Fourth column is for the task
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return ((AllDayTaskEntry) element).getTask().getName();
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
			public String getText(Object element) {
				return ((AllDayTaskEntry) element).getTask().getName();
			}
			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
			}
		});
		// Fifth column is for the issue key
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return ((AllDayTaskEntry) element).getExternalId();
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {			
			public String getText(Object element) {
				return ((AllDayTaskEntry) element).getExternalId();
			}
			public Image getImage(Object obj) {
	    		return null;
			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
	}

	@Override
	protected void createPartBeforeViewer(Composite parent) {
	}

	@Override
	protected boolean getConditionForDarkGrey(Object element) {
		return false;
	}
}
