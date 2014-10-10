package hu.evosoft.eo.downloadstats.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StatsByDomainServiceAsync {

	void getStats(AsyncCallback<StatsByDomain[]> callback);

}
