package be.secMon.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CustomConsole extends Thread {
	private TcpServer serveur;
	private boolean running;
	private MulticastEmetteur emetteur;

	public CustomConsole(TcpServer serveur) {
		this.serveur = serveur;
		this.running = true;
		start();
	}

	public CustomConsole(TcpServer serveur, MulticastEmetteur emetteur) {
		this.serveur = serveur;
		this.emetteur = emetteur;
		this.running = true;
		start();
	}

	@Override
	public void run() {
		while (running) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String name = reader.readLine();
				if (name.equalsIgnoreCase("STOP")) {
					finish();
				}
			} catch (IOException e) {
				System.err.println("Une erreur s'est produite dans la console !");
			}
		}
		System.exit(-1);
	}

	private void finish() {
		System.out.println("Fermeture de la probe " + this.serveur.getprotocol());
		this.running = false;
		if (this.emetteur != null) {
			this.emetteur.finish();
		}
		this.serveur.finish();
		this.interrupt();
	}
}
