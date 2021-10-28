package be.secMon.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RequestHttp {
	private String identifiant;
	private String link;
	private int min;
	private int max;
	private int frequency;
	private double value;
	private String status;

	/*
	 * http1 https://sensor.cg.helmo.be/api/get-temp/ 5 35 60
	 */

	public RequestHttp(String[] response) {
		this.identifiant = response[0];
		this.link = response[1];
		this.min = Integer.valueOf(response[2]);
		this.max = Integer.valueOf(response[3]);
		this.frequency = Integer.valueOf(response[4]);
	}

	public void executeRequest() {
		try {
			String request = requestURL();
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(request);
			this.value = Double.valueOf((String) obj.values().iterator().next());
			changeState();
		} catch (ParseException e) {
			System.err.println("Le resultat n'est pas sous le format JSON !");
		}
	}

	private void changeState() {
		if (this.value > this.min && this.value < this.max) {
			this.status = "OK";
		} else {
			this.status = "ALARM";
		}
		System.out.println("Etat de " + this.getIdentifiant() + " : " + this.getStatus());
	}

	public String getLink() {
		return this.link;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public int getFrequency() {
		return this.frequency;
	}

	public String getIdentifiant() {
		return this.identifiant;
	}

	public String getStatus() {
		return this.status;
	}

	private String requestURL() {
		URL url;
		try {
			url = new URL(this.link);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return response.toString();
			} else {
				System.err.println("Requete n'a pas fonctionne !");
			}
		} catch (MalformedURLException e) {
			System.err.println("L'URL suivant n'est pas inscrit correctement: " + this.link);
		} catch (ProtocolException e) {
			System.err.println("Le protocole utilise n'est pas supporte !");
		} catch (IOException e) {
			System.err.println("Une erreur dans le Request a ete trouve !");
		} finally {
			this.status = "DOWN";
		}
		return "Error";
	}
}
