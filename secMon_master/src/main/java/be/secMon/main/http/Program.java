package be.secMon.main.http;

import org.json.simple.JSONObject;

import be.secMon.connect.Config;
import be.secMon.connect.CustomConsole;
import be.secMon.connect.TcpServer;

public class Program {
	public static void main(String[] args){
		/**
		 * 1. Affiche le demarrage de la probe
		 * 2. Recupere le fichier json en objet json
		 * 3. Transforme l'objet json en config
		 * 4. Creer un serveur TCP
		 */
		System.out.println("Demarrage de la probe HTTP");
		
		JSONObject json = ParserJson.getObjectFromFile("resources/probe_http.json");
		Config config = ParserJson.createConfigFromJson(json);
		
		TcpServer serveur = new TcpServer("HTTP", config);
		new CustomConsole(serveur);
	}
}