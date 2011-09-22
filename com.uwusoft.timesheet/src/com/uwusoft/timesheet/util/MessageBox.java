package com.uwusoft.timesheet.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class MessageBox {
	private static IWorkbench workbench;
	private static ToolTip tip;
	private static Shell shell;
	static {
		tip = getSystemTrayToolTip();
	}

	private static ToolTip getSystemTrayToolTip() {
		if (tip != null) return tip;
		try {
			workbench = PlatformUI.getWorkbench();
			shell = new Shell(workbench.getDisplay(), SWT.NO_TRIM | SWT.ON_TOP);
			if (workbench.getDisplay().getSystemTray().getItemCount() > 0) {
				ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
				workbench.getDisplay().getSystemTray().getItem(0).setToolTip(tip);
				return tip;
			}
			return null;
		} catch (IllegalStateException e) {
			return null;
		}
	}

	public static void setMessage(String title, String message) {
		if ((tip = getSystemTrayToolTip()) == null) {
			Display display = PlatformUI.createDisplay();
			MessageDialog.openInformation(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP), title, message);
		}
		else {
			tip.setText(title); 
			tip.setMessage(message); 
			tip.setVisible(true); 
		}
	}
	
	public static void setError(String title, String message) {
		if ((tip = getSystemTrayToolTip()) == null) {
			Display display = PlatformUI.createDisplay();
			MessageDialog.openError(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP), title, message);
		}
		else {
			tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_ERROR);
			tip.setText(title); 
			tip.setMessage(message); 
			tip.setVisible(true); 
		}
	}
}
