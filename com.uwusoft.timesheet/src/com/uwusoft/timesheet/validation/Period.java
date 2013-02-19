
package com.uwusoft.timesheet.validation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

public class Period {

	public static final String PROP_START = "start";

	public static final String PROP_END = "end";

	private Date start;

	private Date end;

	private final PropertyChangeSupport pcs;

	public Period(final Date start, final Date end) {
		this.start = start;
		this.end = end;
		this.pcs = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public Date getStart() {
		return this.start;
	}

	public void setStart(final Date start) {
		Date oldValue = this.start;
		this.start = start;
		this.pcs.firePropertyChange(Period.PROP_START, oldValue, start);
	}

	public Date getEnd() {
		return this.end;
	}

	public void setEnd(final Date end) {
		Date oldValue = this.end;
		this.end = end;
		this.pcs.firePropertyChange(Period.PROP_END, oldValue, end);
	}

}
