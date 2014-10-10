package hu.evosoft.eo.downloadstats.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("statsByDomain")
public interface StatsByDomainService extends RemoteService {

  StatsByDomain[] getStats();
}