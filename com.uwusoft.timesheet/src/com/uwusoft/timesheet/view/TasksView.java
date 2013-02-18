package com.uwusoft.timesheet.view;

import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.WeekComposite;

public class TasksView extends AbstractTasksView {
	public static final String ID = "com.uwusoft.timesheet.tasksview";
	
    private static final String timeFormat = "HH:mm";

	private WeekComposite weekComposite;
	
	private int currentWeekNum = 0;
	
	protected void createPartBeforeViewer(Composite parent) {
        Calendar cal = new GregorianCalendar();
    	cal.setTime(new Date());        
    	currentWeekNum = cal.get(Calendar.WEEK_OF_YEAR);
    	if (storageService.getLastTaskEntryDate() != null)
    		cal.setTime(storageService.getLastTaskEntryDate());
    	
    	weekComposite = new WeekComposite(this, currentWeekNum, cal.get(Calendar.WEEK_OF_YEAR));
    	weekComposite.createComposite(new Composite(parent, SWT.NONE));
	}

	protected boolean addTaskEntries() {
		if (viewer == null) return false;
        Calendar cal = new GregorianCalendar();
    	cal.setTime(DateUtils.truncate(new Date(), Calendar.DATE));
    	if (currentWeekNum == 0) currentWeekNum = weekComposite.getWeekNum();
    	else weekComposite.setCurrentWeekNum(currentWeekNum);
		List<TaskEntry> taskEntries = new ArrayList<TaskEntry>(storageService.getTaskEntries(weekComposite.getStartDate(), weekComposite.getEndDate()));
		if (!taskEntries.isEmpty() && cal.get(Calendar.WEEK_OF_YEAR) == currentWeekNum) {
			TaskEntry lastTask = storageService.getLastTask();
			if (lastTask != null) taskEntries.add(lastTask);
		}
		viewer.setInput(taskEntries);
		return true;
	}

	protected void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Date", "Time", "Task", "Project", "Total" };
		int[] bounds = { 80, 81, 300, 300, 60 };
        
		int colNum = 0;
		// First column is for the date
		TableViewerColumn col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return DateFormat.getDateInstance(DateFormat.SHORT).format(((TaskEntry) element).getDateTime());
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
			public String getText(Object element) {
				Timestamp date = ((TaskEntry) element).getDateTime();
		    	if (StorageService.CHECK_IN.equals(((TaskEntry) element).getTask().getName()) || ((TaskEntry) element).isAllDay()) {
		    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
		    	}
				return "";				
			}
			public Image getImage(Object obj) {
		    	if (StorageService.CHECK_IN.equals(((TaskEntry) obj).getTask().getName()) || ((TaskEntry) obj).isAllDay())
		    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
		    	return null;
			}
		});
		
		// Second column is for the time
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.getColumn().setAlignment(SWT.RIGHT);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return !((TaskEntry) element).isStatus() && ((TaskEntry) element).getDateTime() != null;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return new TimeDialogCellEditor(viewer.getTable(), (TaskEntry) element);
		    }

		    protected Object getValue(Object element) {
		        return DateFormat.getTimeInstance(DateFormat.SHORT).format(((TaskEntry) element).getDateTime());
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
			public String getText(Object element) {
				Timestamp date = ((TaskEntry) element).getDateTime();
				if (date == null) return "Incomplete";
				else if (!((TaskEntry) element).isAllDay()) return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
				return "";
			}
			public Image getImage(Object obj) {
				if (!((TaskEntry) obj).isAllDay() && ((TaskEntry) obj).getDateTime()!= null) return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/clock.png").createImage();
				return null;
			}
		});

		// Third column is for the task
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return !((TaskEntry) element).isStatus() && !StorageService.CHECK_IN.equals(((TaskEntry) element).getTask().getName());
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return new TaskListDialogCellEditor(viewer.getTable(), (TaskEntry) element);
		    }

		    protected Object getValue(Object element) {
		        return ((TaskEntry) element).getTask().getName();
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
            public String getText(Object element) {
		        return ((TaskEntry) element).getTask().getName();
			}
			public Image getImage(Object obj) {
				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
			}
		});
		// Fourth column is for the project
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				return false;
			}

			@Override
			protected Object getValue(Object element) {
		        return ((TaskEntry) element).getTask().getProject().getName();
			}

			@Override
			protected void setValue(Object element, Object value) {
			}
			
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
            public String getText(Object element) {
            	if (((TaskEntry) element).getTask().getProject() == null) return "";
				return ((TaskEntry) element).getTask().getProject().getName();
			}
			public Image getImage(Object obj) {
				TaskEntry task = (TaskEntry) obj;
				if (StorageService.CHECK_IN.equals(task.getTask().getName()) || StorageService.BREAK.equals(task.getTask().getName())) return null;
				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
			}
		});
		// Fifth column is for the total
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.getColumn().setAlignment(SWT.RIGHT);
		col.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				return false;
			}

			@Override
			protected Object getValue(Object element) {
		        return ((TaskEntry) element).getTotal();
			}

			@Override
			protected void setValue(Object element, Object value) {
			}			
		});
		col.setLabelProvider(new AlternatingColumnProvider() {
            public String getText(Object element) {
				TaskEntry entry = (TaskEntry) element;
				if (entry.getId() == null || StorageService.CHECK_IN.equals(entry.getTask().getName())) return "";
				DecimalFormat df = new DecimalFormat( "0.00" );
		        return df.format(((TaskEntry) element).getTotal());
			}
			public Image getImage(Object obj) {
				TaskEntry entry = (TaskEntry) obj;
				if (entry.getId() == null || StorageService.CHECK_IN.equals(entry.getTask().getName())) return null;
				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/clock.png").createImage();				
			}
		});
	}
	
	class TimeDialogCellEditor extends DialogCellEditor {

		private TaskEntry entry;
		
		/**
		 * @param parent
		 */
		public TimeDialogCellEditor(Composite parent, TaskEntry entry) {
			super(parent);
			this.entry = entry;
		}

		@Override
		protected Object openDialogBox(Control cellEditorWindow) {
			TimeDialog timeDialog = new TimeDialog(cellEditorWindow.getDisplay(), entry.getTask().display(), entry.getDateTime());
			if (timeDialog.open() == Dialog.OK) {
    			Calendar oldCal = Calendar.getInstance();
    			oldCal.setTime(entry.getDateTime());
    			Calendar newCal = Calendar.getInstance();
    			newCal.setTime(timeDialog.getTime());
    			if (oldCal.get(Calendar.HOUR) != newCal.get(Calendar.HOUR)
    					|| oldCal.get(Calendar.MINUTE) != newCal.get(Calendar.MINUTE)
    					|| oldCal.get(Calendar.AM_PM) != newCal.get(Calendar.AM_PM)) {
    				newCal.set(Calendar.YEAR, oldCal.get(Calendar.YEAR));
    				newCal.set(Calendar.MONTH, oldCal.get(Calendar.MONTH));
    				newCal.set(Calendar.DAY_OF_MONTH, oldCal.get(Calendar.DAY_OF_MONTH));
    				entry.setDateTime(new Timestamp(newCal.getTimeInMillis()));
    				storageService.updateTaskEntry(entry);
    				storageService.synchronize();
		    		viewer.refresh(entry);
    			}
				return new SimpleDateFormat(timeFormat).format(timeDialog.getTime());
			}
			return null;
		}		
	}

	class TaskListDialogCellEditor extends DialogCellEditor {

		private TaskEntry entry;
		
		/**
		 * @param parent
		 */
		public TaskListDialogCellEditor(Composite parent, TaskEntry entry) {
			super(parent);
			this.entry = entry;
		}

		@Override
		protected Object openDialogBox(Control cellEditorWindow) {
			TaskListDialog listDialog = new TaskListDialog(cellEditorWindow.getShell(),	entry.getTask(), entry.getComment());
			if (listDialog.open() == Dialog.OK) {
			    String selectedTask = Arrays.toString(listDialog.getResult());
			    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
				if (StringUtils.isEmpty(selectedTask)) return null;
				
		    	if (!selectedTask.equals(entry.getTask().getName())
		    			|| !listDialog.getProject().equals(entry.getTask().getProject().getName())
		    			|| !listDialog.getSystem().equals(entry.getTask().getProject().getSystem())
		    			|| listDialog.getComment() != null && !listDialog.getComment().equals(entry.getComment())) {
		    		entry.setTask(storageService.findTaskByNameProjectAndSystem(selectedTask, listDialog.getProject(), listDialog.getSystem()));
		    		entry.setComment(listDialog.getComment());
		    		storageService.updateTaskEntry(entry);
		    		storageService.synchronize();
		    		viewer.refresh(entry);
					return selectedTask;
		    	}				
			}
			return null;
		}		
	}

	protected boolean getConditionForDarkGrey(Object element) {
		return StorageService.CHECK_IN.equals(((TaskEntry) element).getTask().getName()) || ((TaskEntry) element).isAllDay();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (StorageService.PROPERTY_WEEK.equals(evt.getPropertyName()) && evt.getNewValue() != null) {
			currentWeekNum = (Integer) evt.getNewValue();
			if (addTaskEntries()) viewer.refresh();
		}
	}
}