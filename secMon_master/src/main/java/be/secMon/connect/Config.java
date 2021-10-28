package be.secMon.connect;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.secMon.regex.RegularExpression;

public class Config {

	private String address;
	private String multicastPort;
	private String tcpPort;
	private String aesKey;
	private Map<String, Integer> probes = new HashMap<>();
	private boolean tls;
	
	public Config(String address, String port, boolean tls, String aesKey, List<String> probes) {
		this.address = address;
		this.multicastPort = port;
		this.tls = tls;
		this.aesKey = aesKey;
		for (String service : probes) {
			this.probes.put(service, 1);
		}
	}
	public Config(String multicastAdress, String multicastPort, String tcpPort, String aesKey) {
		this.address = multicastAdress;
		this.multicastPort = multicastPort;
		this.tcpPort = tcpPort;
		this.aesKey = aesKey;
	}
	public String getAddress() {
		return this.address;
	}
	
	public int getMulticastPort() {
		return Integer.valueOf(this.multicastPort);
	}
	
	public int getTcpPort() {
		return Integer.valueOf(this.tcpPort);
	}
	
	public String getKey() {
		return this.aesKey;
	}
	public Map<String, Integer> getProbes() {
		return probes;
	}

	public boolean addServices(String service) {
		String id = RegularExpression.getIdAugmentUrl(service);
		boolean add = true;
		for (String serv : probes.keySet()) {
			String servId = RegularExpression.getIdAugmentUrl(serv);
			if (id.equals(servId)) {
				add = false;
				break;
			}
		}
		if (add)
			probes.put(service, 0);

		return add;
	}

	public boolean getTls() {
		return tls;
	}

	public String getProbe(String id) {
		for (String probe : probes.keySet()) {
			if (probe.startsWith(id)) {
				return probe;
			}
		}
		return "url non trouve";
	}

	public void setProbe(String probe, int i) {
		probes.replace(probe, i);
	}
}
