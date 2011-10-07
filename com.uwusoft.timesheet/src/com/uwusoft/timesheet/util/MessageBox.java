package com.uwusoft.timesheet.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class MessageBox {
	private static ToolTip toolTip;
	private static Shell shell;

	private static ToolTip getSystemTrayToolTip() {
		if (toolTip != null) return toolTip;
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			shell = new Shell(workbench.getDisplay(), SWT.NO_TRIM | SWT.ON_TOP);
			if (workbench.getDisplay().getSystemTray().getItemCount() > 0) {
				ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
				workbench.getDisplay().getSystemTray().getItem(0).setToolTip(tip);
				return tip;
			}
			return null;
		} catch (IllegalStateException e) {
			shell = new Shell(Display.getDefault(), SWT.NO_TRIM | SWT.ON_TOP);
			return null;
		}
	}

	public static void setMessage(String title, String message) {
		if ((toolTip = getSystemTrayToolTip()) == null) {
			MessageDialog.openInformation(shell, title, message);
		}
		else {
			toolTip.setText(title); 
			toolTip.setMessage(message); 
			toolTip.setVisible(true); 
		}
	}
	
	public static void setError(String title, String message) {
		if ((toolTip = getSystemTrayToolTip()) == null) {
			MessageDialog.openError(shell, title, message);
		}
		else {
			toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_ERROR);
			toolTip.setText(title); 
			toolTip.setMessage(message == null ? "" : message); 
			toolTip.setVisible(true); 
		}
	}
}
