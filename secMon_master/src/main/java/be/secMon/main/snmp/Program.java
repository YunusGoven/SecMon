package be.secMon.main.snmp;

import be.secMon.connect.Config;
import be.secMon.connect.CustomConsole;
import be.secMon.connect.MulticastEmetteur;
import be.secMon.connect.TcpServer;
import be.secMon.main.http.ParserJson;

public class Program {

	MulticastEmetteur emetteur;
	TcpServer server;
	Config config = ParserJson.createConfigFromJson(ParserJson.getObjectFromFile("resources/probe_snmp.json"));

	public Program() {
		server = new TcpServer("SNMP", config);
		new CustomConsole(server, emetteur);
	}

	public static void main(String[] args) {
		new Program();
		System.out.println("Démarrage de la probe SNMP !");
	}

}
