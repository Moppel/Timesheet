package com.uwusoft.timesheet;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "com.uwusoft.timesheet.perspective";

    public void initialize(IWorkbenchConfigurer configurer) {
        configurer.setSaveAndRestore(true);
    }

    public IStatus saveState(IMemento memento) {
        memento.createChild("myApp").putString(
            "lastOpenedDate", DateFormat.getDateTimeInstance().format(new Date()));
        return super.saveState(memento);
    }

    @Override
    public IStatus restoreState(IMemento memento) {
        if (memento != null) {
            IMemento myAppMemento = memento.getChild("myApp");
            if (myAppMemento != null)
                System.out.println("Last opened on: " + myAppMemento.getString("lastOpenedDate"));
        }
        return super.restoreState(memento);
    }

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

}
