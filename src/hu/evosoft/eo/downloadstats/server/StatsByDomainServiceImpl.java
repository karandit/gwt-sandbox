package hu.evosoft.eo.downloadstats.server;

import hu.evosoft.eo.downloadstats.client.StatsByDomain;
import hu.evosoft.eo.downloadstats.client.StatsByDomainService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StatsByDomainServiceImpl extends RemoteServiceServlet implements StatsByDomainService {

	private static final long serialVersionUID = 1L;

	@Override
	public StatsByDomain[] getStats() {
		StatsByDomain[] stats = createStats( 
				new StatsByDomain("index.hu", 1336060),
				new StatsByDomain("origo.hu", 1538156),
				new StatsByDomain("totalcar.hu", 1576579),
				new StatsByDomain("noklapja.hu", 1600652),
				new StatsByDomain("news.ycombinator.com", 2400652)
				);
		return stats;
	}

	private StatsByDomain[] createStats(StatsByDomain... array) {
		return array;
	}

}
