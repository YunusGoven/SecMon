package be.secMon.request;

import java.util.HashMap;
import java.util.Map;

import be.secMon.connect.MulticastEmetteur;
import be.secMon.regex.RegularExpression;

public class SnmpProbe extends Thread {
	MulticastEmetteur emetteur;
	private Map<String, SnmpRequest> services = new HashMap<String, SnmpRequest>();
	private boolean isRunning;

	public SnmpProbe(MulticastEmetteur emetteur) {
		this.emetteur = emetteur;
		isRunning = true;
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				setServicesStatus();
				emetteur.emettre("NOTIFY SNMP 5550\r\n");
				Thread.sleep(120000);
			}
		} catch (InterruptedException e) {
			System.err.println("Le thread SNMP probe a été interrompu !");
		}
	}

	public void addService(String service) {
		SnmpRequest currentService = RegularExpression.getAugmentedUrlValues(service);
		if (!this.services.containsKey(currentService.getId())) {
			this.services.put(currentService.getId(), currentService);
		}
	}

	public void setServicesStatus() {
		for (Map.Entry<String, SnmpRequest> entry : services.entrySet()) {
			SnmpRequest service = entry.getValue();
			service.checkStatus(service.snmpGet());
		}
	}

	public String getStatus(String id) {
		return services.get(id).getStatus();
	}
	
	public boolean contains(String id) {
		if(this.services.containsKey(id)) {
			return true;
		}else {
			return false;
		}
	}

	public void finish() {
		isRunning = false;
		interrupt();
	}

}
