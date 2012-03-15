package com.uwusoft.timesheet;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		/*String editorArea = layout.getEditorArea();

		layout.addStandaloneView(NavigationView.ID, false, IPageLayout.LEFT,
				0.25f, editorArea);
		IFolderLayout folder = layout.createFolder("messages", IPageLayout.TOP,
				0.5f, editorArea);
		folder.addPlaceholder(View.ID + ":*");
		folder.addView(View.ID);

		IFolderLayout consoleFolder = layout.createFolder("console",
				IPageLayout.BOTTOM, 0.65f, "messages");
		consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.getViewLayout(NavigationView.ID).setCloseable(false);*/
	}

}
