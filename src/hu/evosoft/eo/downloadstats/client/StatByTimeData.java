package hu.evosoft.eo.downloadstats.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JavaScript object to store statistics about downloads by time. 
 * @author Karandi Tamas
 */
public class StatByTimeData extends JavaScriptObject {
	// Overlay types always have protected, zero argument constructors.
	protected StatByTimeData() {
	}

	// JSNI methods to get statistics.
	public final native double getTimeStamp() /*-{ return this.timeStamp; }-*/;
	public final native double getCount() /*-{ return this.value; }-*/;


}
