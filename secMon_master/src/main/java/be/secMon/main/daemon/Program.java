package be.secMon.main.daemon;

import org.json.simple.JSONObject;

import be.secMon.connect.Config;

public class Program {
    public static void main(String[] args) {
        System.out.println("Demarrage du moniteur daemon");
        JSONObject configObject = ParserJson.getObjectFromFile("resources/monitor.json");
        Config config = ParserJson.setConfigFromJson(configObject);
        new Launcher(config);
    }
}
