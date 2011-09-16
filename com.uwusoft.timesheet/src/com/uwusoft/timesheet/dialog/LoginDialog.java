package com.uwusoft.timesheet.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends Dialog {
	private String user, password;
	private Text txtUserName, txtPassword;

	public LoginDialog(Display display, String user) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.user = user;
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
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
