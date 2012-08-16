package com.uwusoft.timesheet.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.uwusoft.timesheet.util.ImportTasksUtil;

public class ImportTasksHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return ImportTasksUtil.execute(null);
	}
}
