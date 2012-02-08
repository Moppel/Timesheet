package com.uwusoft.timesheet;

import java.io.IOException;
import java.net.URL;

import org.eclipse.persistence.internal.jpa.deployment.ArchiveFactoryImpl;

/**
 * see http://stackoverflow.com/a/7982008
 * @author wunut
 *
 */
public class MyArchiveFactoryImpl extends ArchiveFactoryImpl {

	@Override
	protected boolean isJarInputStream(URL arg0) throws IOException {
		return false;
	}

}
