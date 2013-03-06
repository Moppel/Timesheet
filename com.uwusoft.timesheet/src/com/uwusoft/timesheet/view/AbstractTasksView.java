package com.uwusoft.timesheet.view;

import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import com.uwusoft.timesheet.extensionpoint.LocalStorageService;

public abstract class AbstractTasksView extends ViewPart implements PropertyChangeListener {

	protected LocalStorageService storageService;
	protected TableViewer viewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		storageService = LocalStorageService.getInstance();
        
		createPartBeforeViewer(parent);
		
		viewer = new TableViewer(parent, SWT.BORDER|SWT.HIDE_SELECTION|SWT.FULL_SELECTION|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL);
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
		
		storageService.addPropertyChangeListener(this);
	}
	
	protected abstract void createPartBeforeViewer(Composite parent);
	
	protected abstract void createColumns(final Composite parent, final TableViewer viewer);
	
	protected abstract boolean addTaskEntries();

	protected TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;

	}

	// see http://dev.eclipse.org/viewcvs/viewvc.cgi/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet041TableViewerAlternatingColors.java?view=markup
	protected class OptimizedIndexSearcher {
		private int lastIndex = 0;

		public boolean isEven(TableItem item) {
			TableItem[] items = item.getParent().getItems();

			// 1. Search the next ten items
			for (int i = lastIndex; i < items.length && lastIndex + 10 > i; i++) {
				if (items[i] == item) {
					lastIndex = i;
					return lastIndex % 2 == 0;
				}
			}

			// 2. Search the previous ten items
			for (int i = lastIndex; i < items.length && lastIndex - 10 > i; i--) {
				if (items[i] == item) {
					lastIndex = i;
					return lastIndex % 2 == 0;
				}
			}

			// 3. Start from the beginning
			for (int i = 0; i < items.length; i++) {
				if (items[i] == item) {
					lastIndex = i;
					return lastIndex % 2 == 0;
				}
			}

			return false;
		}
	}
	
	protected class AlternatingColumnProvider extends ColumnLabelProvider {
		private OptimizedIndexSearcher searcher = new OptimizedIndexSearcher();
        private boolean even = true;        
        
		@Override
        public void update(ViewerCell cell) {
            even = searcher.isEven((TableItem)cell.getItem());
            super.update(cell);
		}
		
        @Override
		public Color getBackground(Object element) {
			return getColor(viewer, element, even);
		}
	}

    /**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected abstract boolean getConditionForDarkGrey(Object element);
	
	protected Color getColor(final TableViewer viewer, Object element, boolean even) {
		if (getConditionForDarkGrey(element)) return viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		if(even) return null;
		return viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_GRAY);
	}
}
