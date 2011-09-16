package com.uwusoft.timesheet.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends Dialog {
	private String title, message, user, password;
	private Text txtUserName, txtPassword;
	private Display display;

	public LoginDialog(Display display, String title, String message, String user) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.display = display;
		this.title = title;
		this.message = message;
		this.user = user;
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
		if (message != null) {
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.CENTER;
			gridData.horizontalSpan = 2;
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(gridData);
			label.setForeground(display.getSystemColor(SWT.COLOR_RED));
			label.setText(message);
		}
        
		Label label = new Label(composite, SWT.NONE);
        label.setText("User: ");
        txtUserName = new Text(composite, SWT.NONE);
        txtUserName.setText(user);
        label = new Label(composite, SWT.NONE);
        label.setText("Password: ");
        txtPassword = new Text(composite, SWT.NONE);
        txtPassword.setEchoChar('*');
        //getButton(OK).setText("Login");
		return composite;
	}

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    @Override
	protected void okPressed() {
		user = txtUserName.getText();
		password = txtPassword.getText();
		super.okPressed();
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
}
