package hu.evosoft.eo.downloadstats.client;

import java.io.Serializable;

public class StatsByDomain implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String domain;
	private int count;

	public StatsByDomain() {
	}

	public StatsByDomain(final String domain, final int count) {
		this.domain = domain;
		this.count = count;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	
}
