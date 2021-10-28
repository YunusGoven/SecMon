package be.secMon.main.http;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import be.secMon.connect.Config;

public class ParserJson {
	
	/**
	 * Methode statique transformant un fichier json en objet json
	 * 
	 * @param Chemin vers le fichier JSON
	 * @return Un objet json comprenant les donnees classees
	 */
	public static JSONObject getObjectFromFile(String file) {
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader(file)) {
			Object obj = jsonParser.parse(reader);
			return (JSONObject) obj;

		} catch (FileNotFoundException e) {
			System.err.println("Le fichier JSON n'a pas ete trouve !");
		} catch (IOException e) {
			System.err.println("Erreur du au lecteur de fichier !");
		} catch (ParseException e) {
			System.err.println("Le fichier JSON n'a pu �tre converti en object JSON !");
		}
		return null;
	}
	
	/**
	 * Cr�er une config � base d'un objet json
	 * 
	 * @param Un objet json obtenu d'un fichier
	 * @return Une config contenant les informations utiles
	 */
	
	public static Config createConfigFromJson(JSONObject json) {
		String multicastIp = (String) json.get("multicast_address");
		String portMulticast = (String) json.get("multicast_port");
		String aesKey = (String) json.get("aes_key");
		String portTcp = (String) json.get("tcp_port");
		return new Config(multicastIp, portMulticast, portTcp, aesKey);
	}
}
