package be.secMon.main.daemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import be.secMon.connect.Config;
import be.secMon.crypto.Aes;
import be.secMon.regex.RegularExpression;

public class ClientRunnable implements Runnable {
    private Socket client;
    private boolean stop =false;
    private BufferedReader in;
    private PrintWriter out;
    private ILauncher launcher;
    private Config config;

    public ClientRunnable(Socket client, ILauncher launcher, Config config) {
        this.config = config;
        this.client = client;
        this.launcher = launcher;

    }

    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.client.getInputStream(), Charset.forName("UTF-8")));
            this.out = new PrintWriter(this.client.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(!stop){
            String msg = readLine();
            if(RegularExpression.isAddServiceReq(msg)){//ajout service
                addNewService(msg);
            }else if(RegularExpression.isListServiceReq(msg)){//list service
                sendListService();
            }else if(RegularExpression.isStateServiceReq(msg)){//etat service
                sendServiceState(msg);
            }else if("exit".equals(msg)) {//quit application client
                clientExit();
            }else{
                sendMessage("");
            }
        }
    }
    private void addNewService(String message){
        String agurl = RegularExpression.getAugmentedUrl(message);
        boolean isAdd = config.addServices(agurl);
        if(isAdd) {
            sendMessage("+OK service ajoutee");
            ParserJson.addProbes(config,"monitor.json");
        }else
            sendMessage("-ERR service non ajoutee");
    }
    private void sendListService(){
        Map<String,String> srv = launcher.getState();
        Set<String> keys =srv.keySet();
        String id = "SRV";
        for(String key : keys){
            id += " "+key;
        }
        sendMessage(id);
    }
    private void sendServiceState(String message){
        Map<String,String> srv = launcher.getState();
        String id = RegularExpression.getIdMessage(message);
        if(srv.containsKey(id)){
            String state = srv.get(id);
            String url = config.getProbe(id);
            url = RegularExpression.getUrlFromAugmentedUrl(url);
            String msg = String.format("STATE %s %s %s",id, url,state);
            sendMessage(msg);
        }else{
            sendMessage("");
        }
    }
    private void clientExit(){
        try {
            System.out.println("[Client : "+client.getInetAddress().getHostAddress()+"] deconnecté");
            client.close();
            stop = true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                client.close();
            } catch (IOException ex) {
            }
        }
    }

    private void sendMessage(String message){
        message = String.format("%s\r\n",message);
        Aes aes = new Aes(config.getKey());
        String crypt = aes.encrypt(message);
        out.println(crypt);
        out.flush();
        System.out.println("[Client "+ client.getInetAddress().getHostAddress()+"] Message envoye : "+ message );
    }

    private String readLine(){
        Aes aes = new Aes(config.getKey());
        try {
            String line;
            if ((line = in.readLine())!= null) {
                String decrypt = aes.decrypt(line);
                System.out.println("[Client "+ client.getInetAddress().getHostAddress()+"] Message reecu : "+ decrypt );
                return decrypt;
            }
        } catch (IOException e) {
            return "exit";
        }
        return "exit";
    }
}
