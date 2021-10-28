package be.secMon.main.daemon;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import be.secMon.connect.Config;
import be.secMon.crypto.Aes;

public class ParserJson {
    private  static final String key = "A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbP";

   /* public static void main(String[] args) {
        Aes aes = new Aes(key);
        System.out.println(aes.encrypt("snmp1!snmp://public@192.168.128.38:161/1.3.6.1.4.1.2021.4.11.0!10000!99999999!120"));
        System.out.println(aes.encrypt("snmp2!snmp://public@192.168.128.38:161/1.3.6.1.4.1.2021.11.11.0!10!99999999!120"));
        System.out.println(aes.encrypt("http1!https://sensor.cg.helmo.be/api/get-temp/!5!35!60"));
        System.out.println(aes.encrypt("http2!https://sensor.cg.helmo.be/api/get-humidity/!0!80!60"));
    }*/

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
			System.err.println("Le fichier JSON n'a pu etre converti en object JSON !");
		}
		return null;
	}

    @SuppressWarnings("unchecked")
	public static void addProbes(Config config, String file) {
        Aes aes = new Aes(key);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("multicast_address", aes.encrypt(config.getAddress()));
        jsonObject.put("multicast_port", aes.encrypt(String.valueOf(config.getMulticastPort())));
        jsonObject.put("tls",config.getTls());
        jsonObject.put("aes_key", aes.encrypt(config.getKey()));
        List<String> probesCrypt = new ArrayList<>();
        Set<String> keySet = config.getProbes().keySet();
        for(String probes:keySet)
            probesCrypt.add(aes.encrypt(probes));
        jsonObject.put("probes", probesCrypt);
        try {
            Files.delete(Paths.get("src","main","resources",file).toAbsolutePath());
            FileWriter fileWriter = new FileWriter(Paths.get("src","main","resources",file).toAbsolutePath().toString());
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config setConfigFromJson(JSONObject json) {
        Aes aes = new Aes(key);
        String multicastIp = (String) json.get("multicast_address");
        multicastIp = aes.decrypt(multicastIp);
        String portMulticast = (String) json.get("multicast_port");
        portMulticast = aes.decrypt(portMulticast);
        String aesKey = (String) json.get("aes_key");
        aesKey = aes.decrypt(aesKey);
        boolean tls = (boolean)json.get("tls");
        @SuppressWarnings("unchecked")
		List<String> probeServices = (ArrayList<String>) json.get("probes");
        List<String> probesServices = new ArrayList<>();
        for (String probe : probeServices)
            probesServices.add(aes.decrypt(probe));
        return new Config(multicastIp, portMulticast, tls,aesKey, probesServices);
    }
}
