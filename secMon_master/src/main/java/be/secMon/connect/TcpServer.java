package be.secMon.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;

import be.secMon.crypto.Aes;
import be.secMon.regex.RegularExpression;
import be.secMon.request.RequestCollection;
import be.secMon.request.RequestHttp;
import be.secMon.request.SnmpProbe;

public class TcpServer extends Thread {
	private Aes aes;
	private int portTcp;
	private MulticastEmetteur emetteur;
	private Socket clientSocket;
	private ServerSocket serveurSocket;
	private String output;
	private BufferedReader bufferedReader;
	private PrintWriter out;
	private RequestCollection services;
	private SnmpProbe probe;
	private String protocol;
	private boolean running;

	/**
	 * Creer un serveur tcp a partir de la config
	 * 
	 * @param config Config du fichier JSON
	 */
	public TcpServer(String protocol, Config config) {
		try {
			this.aes = new Aes(config.getKey());
			this.portTcp = config.getTcpPort();
			this.serveurSocket = new ServerSocket(this.portTcp);
			this.protocol = protocol;
			this.running = true;
			start();
			this.emetteur = new MulticastEmetteur(protocol, config.getAddress(), config.getMulticastPort(),
					this.portTcp);
			this.services = new RequestCollection(emetteur);
		} catch (IOException e) {
			System.err.println("Le port TCP est d�j� occup� !");
		}
	}

	/**
	 * D�marrage du serveur TCP
	 */
	@Override
	public void run() {
		try {
			while (running) {
				clientSocket = serveurSocket.accept();
				System.out.println("Connexion TCP de: " + clientSocket.getInetAddress());
				bufferedReader = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream(), Charset.forName("UTF-8")));
				while ((output = bufferedReader.readLine()) != null) {

					String decrypted = aes.decrypt(output);
					System.out.println("Message TCP recu: " + decrypted);
					String message = RegularExpression.getMessage(decrypted);
					List<String> services = RegularExpression.getServices(decrypted);
					switch (message) {
					case "CURCONFIG":
						String protocole = RegularExpression.getProtocolName(decrypted);
						if (protocole.equalsIgnoreCase("HTTP")) {
							for (String service : services) {
								String key = RegularExpression.getId(service);
								if (!this.services.contains(key)) {
									RequestHttp request = new RequestHttp(RegularExpression.getValuesFromAURL(service));
									this.services.add(request);
								}
							}
							if (!this.services.isAlive()) {
								this.services.start();
							}
						} else if(protocole.equalsIgnoreCase("SNMP")){
							this.probe = new SnmpProbe(emetteur);
							for (String service : services) {
								this.probe.addService(service);
							}
							if(!this.probe.isAlive()) {
								this.probe.start();
							}
						}
						break;

					case "STATEREQ":
						out = new PrintWriter(
								new OutputStreamWriter(clientSocket.getOutputStream(), Charset.forName("UTF-8")), true);
						String[] values = RegularExpression.getValuesFromReq(decrypted);
						String state = checkServices(values[1]);
						String sendMessageStatus = "STATERESP " + values[1] + " " + state + "\r\n";
						out.println(aes.encrypt(sendMessageStatus));
						out.flush();
						System.out.println("Envoi en TCP de: " + sendMessageStatus);
						break;
					default:
						break;
					}
				}
				clientSocket.close();
			}
		} catch (IOException e) {
			Thread.currentThread().interrupt();
			System.err.println("Erreur dans le serveur TCP");
		}
	}
	
	public String checkServices(String id) {
		if(this.probe != null && this.probe.contains(id)) {
			return this.probe.getStatus(id);
		}else if(this.services.contains(id)) {
			return this.services.getRequest(id).getStatus();
		}else {
			return null;
		}
	}

	public String getprotocol() {
		return this.protocol;
	}

	public void finish() {
		if(services != null && services.isAlive()) {
			this.services.finish();
		}
		if(probe != null && probe.isAlive()) {
			this.probe.finish();
		}
		this.emetteur.finish();
		this.interrupt();
		this.running = false;
	}
}
