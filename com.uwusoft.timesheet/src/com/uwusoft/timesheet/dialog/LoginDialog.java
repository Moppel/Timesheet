package com.uwusoft.timesheet.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends Dialog {
	private String title, message, user, password;
	private Text txtUserName, txtPassword;
	private Button checkbox;
	private boolean storePassword;
	private Display display;

	public LoginDialog(Display display, String title, String message, String user, String password) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.display = display;
		this.title = title;
		this.message = message;
		this.user = user;
		this.password = password;
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
	        label.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
			label.setLayoutData(gridData);
			label.setForeground(display.getSystemColor(SWT.COLOR_RED));
			label.setText(message);
		}
        
		new Label(composite, SWT.NONE).setText("User: ");
    	GridData gridData = new GridData();
    	gridData.widthHint = 150;
    	gridData.horizontalAlignment = SWT.FILL;
        txtUserName = new Text(composite, SWT.NONE);
        txtUserName.setLayoutData(gridData);
        if (user != null) txtUserName.setText(user);
        new Label(composite, SWT.NONE).setText("Password: ");
        txtPassword = new Text(composite, SWT.NONE);
        txtPassword.setLayoutData(gridData);
        txtPassword.setEchoChar('*');
        if (password != null) txtPassword.setText(password);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
        checkbox = new Button(composite, SWT.CHECK);
        checkbox.setText("Save password");
        checkbox.setLayoutData(gridData);
        Label label = new Label(composite, SWT.NONE);
        label.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
        label.setLayoutData(gridData);
        label.setText("Saved passwords are stored on your computer in a file\n" +
        		"that is difficult, but not impossible, for an intruder to read.");
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
		storePassword = checkbox.getSelection();
		super.okPressed();
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * @return the storePassword
	 */
	public boolean isStorePassword() {
		return storePassword;
	}	
}
