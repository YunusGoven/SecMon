package be.secMon.connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastEmetteur extends Thread {
	private final int REFRESH = 90;
	private InetAddress groupeIP;
	private int portUdp;
	private int portTcp;
	private MulticastSocket socketEmission;
	private String protocol;
	private boolean running;

	/**
	 * Creer un emetteur multicast
	 * @param protocol HTTP
	 * @param multicastIp Addresse multicast
	 * @param portUdp Port multicast
	 * @param portTcp Port tcp
	 */
	public MulticastEmetteur(String protocol, String multicastIp, int portUdp, int portTcp) {
		try {
			this.protocol = protocol;
			groupeIP = InetAddress.getByName(multicastIp);
			this.portUdp = portUdp;
			this.portTcp = portTcp;
			this.running = true;
			socketEmission = new MulticastSocket();
			start();
		} catch (UnknownHostException e) {
			System.err.println("L'emetteur multicast n'a pu se connecter a l'ip !");
		} catch (IOException e) {
			System.err.println("Une erreur a ete causee par le MulticastSocket !");
		}
	}

	/**
	 * D�marrage de l'emission multicast
	 */
	public void run() {
		while (running) {
			try {
				emettre("IAMHERE " + protocol + " " + portTcp + "\r\n");
				Thread.sleep(REFRESH*1000);
			} catch (InterruptedException e) {
				System.err.println("Le Thread du multicast emetteur a ete interrompu !");
			}
		}
	}

	/**
	 * Envoi un message en multicast
	 * @param texte Message � envoyer
	 */
	public void emettre(String texte) {
		try {
			byte[] contenuMessage = texte.getBytes();
			DatagramPacket message = new DatagramPacket(contenuMessage, contenuMessage.length, groupeIP, portUdp);
			socketEmission.send(message);
			System.out.println("Emission en Multicast: " + new String(contenuMessage));
		} catch (IOException e) {
			System.err.println("Le message n'a pas pu s'envoyer !");
		}
	}
	
	public void finish() {
		this.emettre("GOODBYE " + this.protocol + " 0000\r\n");
		this.socketEmission.close();
		this.running = false;
		this.interrupt();
	}
}
