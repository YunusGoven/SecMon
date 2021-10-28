package be.secMon.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.secMon.crypto.Aes;
import be.secMon.regex.RegularExpression;

public class TcpProbeClient{
    private Socket socket;
    private Config config;
    private String protocol;
    private BufferedReader in;
    private PrintWriter out;
    private List<String> services = new ArrayList<>();

    public TcpProbeClient(Socket socket, Config config, String protocol) {
    	this.config = config;
        this.protocol = protocol;
        try{
            this.socket = socket;
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")), true);
        }catch (IOException ex){
            ex.printStackTrace();
        }
	}

	public void closeConnection(){
        out.close();
        System.out.println("["+protocol+"]deconnecte de la probes " );
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) { }
        }

    }

    public void sendConfig() {
        services.clear();
        setProbes(true);
        String allServices = "";
        for(String service: services){
            allServices += String.format(" %s",service);
        }
        String confi = String.format("CURCONFIG%s",allServices);
        sendMessage(confi);
        closeConnection();
    }

    private void sendMessage(String message){
        message = String.format("%s\r\n",message);
        Aes aes = new Aes(config.getKey());
        String crypt = aes.encrypt(message);
        out.println(crypt);
        out.flush();
        System.out.println(" ["+protocol+"]" +"Message envoyee a "+socket.getInetAddress().getHostAddress()+" "+message);
    }

    public Map<String, String> askStateReq() {
        Map<String,String> info = new HashMap<>();
        services.clear();
        setProbes(false);
        for(String service: services){
            String id = RegularExpression.getIdAugmentUrl(service);
            if(id!=null){
                String req = String.format("STATEREQ %s",id);
                sendMessage(req);
                String resp =readLine();
                if("erreur".equals(resp)){
                    closeConnection();
                    return info;
                }
                if(RegularExpression.isStateResq(resp)){
                    if(id.equals(RegularExpression.getIdFromResq(resp))){
                        info.put(id, RegularExpression.getState(resp));
                    }
                }
            }
        }
        closeConnection();
        return info;
    }
    private String readLine(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Aes aes = new Aes(config.getKey());
        try {
            String line = in.readLine();
            if(line!=null){
                String decrypt = aes.decrypt(line);
                System.out.print("["+protocol+"]" +"Message reecu depuis "+socket.getInetAddress().getHostAddress());
                System.out.println(": "+decrypt);
                return  decrypt;
            }
        } catch (IOException e) {
            return "erreur";
        }
        return "erreur";
    }

    private void setProbes(boolean sendConfig){
        Map<String,Integer> probes = config.getProbes();
        for(String probe : probes.keySet()) {
            if(probe.contains(protocol.toLowerCase()))
                if(sendConfig) {
                    this.services.add(probe);
                    config.setProbe(probe,1);
                }else{
                    if(probes.get(probe)==1)
                        this.services.add(probe);
                }
        }
    }
}
