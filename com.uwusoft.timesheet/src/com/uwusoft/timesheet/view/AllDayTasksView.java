package com.uwusoft.timesheet.view;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class AllDayTasksView extends ViewPart {
	public static final String ID = "com.uwusoft.timesheet.view.alldaytasksview";

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
