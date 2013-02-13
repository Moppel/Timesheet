package com.uwusoft.timesheet.view;

import java.sql.Timestamp;
import java.text.DateFormat;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;

public class AllDayTasksView extends ViewPart {
	public static final String ID = "com.uwusoft.timesheet.view.alldaytasksview";

	private TableViewer viewer;
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(ArrayContentProvider.getInstance());

		addTaskEntries();
		// Make the selection available to other views
		getSite().setSelectionProvider(viewer);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
	}

	private void addTaskEntries() {
		if (viewer == null) return;
		viewer.setInput(LocalStorageService.getInstance().getAllDayTaskEntries());
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "From", "To", "Task" };
		int[] bounds = { 80, 80, 300 };
        
		//final OptimizedIndexSearcher searcher = new OptimizedIndexSearcher();

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
		col.setLabelProvider(new ColumnLabelProvider() {
            //boolean even = true;
			
			public String getText(Object element) {
				Timestamp date = ((AllDayTaskEntry) element).getFrom();
	    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
			}
			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
			}
            /*@Override public void update(ViewerCell cell) {
                even = searcher.isEven((TableItem)cell.getItem());
                super.update(cell);
			}
			@Override public Color getBackground(Object element) {
				return getColor(viewer, element, even);
			}*/
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
		col.setLabelProvider(new ColumnLabelProvider() {
            //boolean even = true;
			
			public String getText(Object element) {
				Timestamp date = ((AllDayTaskEntry) element).getTo();
	    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
			}
			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
			}
            /*@Override public void update(ViewerCell cell) {
                even = searcher.isEven((TableItem)cell.getItem());
                super.update(cell);
			}
			@Override public Color getBackground(Object element) {
				return getColor(viewer, element, even);
			}*/
		});
		// Third column is for the task
		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
		col.setEditingSupport(new EditingSupport(viewer) {

		    protected boolean canEdit(Object element) {
		        return false;
		    }

		    protected CellEditor getCellEditor(Object element) {
		        return null;
		    }

		    protected Object getValue(Object element) {
		        return DateFormat.getDateInstance(DateFormat.SHORT).format(((AllDayTaskEntry) element).getTask().getName());
		    }

		    protected void setValue(Object element, Object value) {
		    }
		});
		col.setLabelProvider(new ColumnLabelProvider() {
            //boolean even = true;
			
			public String getText(Object element) {
				return ((AllDayTaskEntry) element).getTask().getName();
			}
			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
			}
            /*@Override public void update(ViewerCell cell) {
                even = searcher.isEven((TableItem)cell.getItem());
                super.update(cell);
			}
			@Override public Color getBackground(Object element) {
				return getColor(viewer, element, even);
			}*/
		});
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;

	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
