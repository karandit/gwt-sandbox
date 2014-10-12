package hu.evosoft.eo.downloadstats.client;

import com.google.gwt.core.client.JavaScriptObject;

class StatByDomainData extends JavaScriptObject {
  // Overlay types always have protected, zero argument constructors.
  protected StatByDomainData() {}

  // JSNI methods to get statistics
  public final native String getDomain() /*-{ return this.name; }-*/;
  public final native double getCount() /*-{ return this.value; }-*/;

}
