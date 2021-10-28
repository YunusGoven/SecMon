package be.secMon.request;

import java.util.ArrayList;
import java.util.List;

import be.secMon.connect.MulticastEmetteur;

public class RequestCollection extends Thread {
	private final MulticastEmetteur emetteur;
	private final List<RequestHttp> activeRequests;
	private boolean running;

	public RequestCollection(MulticastEmetteur emetteur) {
		this.running = true;
		this.emetteur = emetteur;
		this.activeRequests = new ArrayList<RequestHttp>();
	}

	public void add(RequestHttp request) {
		this.activeRequests.add(request);
	}

	public RequestHttp getRequest(String identifiant) {
		for (RequestHttp requestHttp : activeRequests) {
			if (requestHttp.getIdentifiant().equals(identifiant)) {
				return requestHttp;
			}
		}
		return null;
	}

	public boolean contains(String identifiant) {
		for (RequestHttp requestHttp : activeRequests) {
			if (requestHttp.getIdentifiant().equals(identifiant)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		while (running) {
			try {
				requestAll();
				this.emetteur.emettre("NOTIFY " + "HTTP " + 5555 + "\r\n");
				Thread.sleep(this.activeRequests.get(0).getFrequency() * 1000);
			} catch (InterruptedException e) {
				System.err.println("Le Thread des requetes a ete interrompu !");
			}
		}
	}

	public void requestAll() {
		for (RequestHttp requestHttp : activeRequests) {
			requestHttp.executeRequest();
		}
	}
	
	public void finish() {
		this.running = false;
		this.interrupt();
	}
}
