package be.secMon.main.daemon;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import be.secMon.connect.Config;
import be.secMon.connect.MulticastReceiver;

public class Launcher implements ILauncher {
    private boolean stop = false;
    private ServerSocket clientServer;
    private SSLServerSocket sslServerSocket;
    private Config config;
    private Map<String ,String> state;

    public Launcher(Config config){
        state = new HashMap<>();
        this.config = config;
        startMulticastRecepter();
        startClientServer();
    }

    private void startClientServer() {
        try {
            if(!config.getTls()){
                clientServer = new ServerSocket(2555);
                System.out.println("[Client] Serveur ip: "+clientServer.getInetAddress()+" sur le port : "+2555);
            }else{
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(new FileInputStream(getClass().getResource("/group11.monitor.p12").getPath()),"group11".toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SUNX509");
                kmf.init(ks, "group11".toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                tmf.init(ks);

                SSLContext sc = SSLContext.getInstance("TLS");
                TrustManager[]trustManagers = tmf.getTrustManagers();
                sc.init(kmf.getKeyManagers(),trustManagers,null);

                SSLServerSocketFactory sslServerSocketFactory = sc.getServerSocketFactory();
                sslServerSocket = (SSLServerSocket)sslServerSocketFactory.createServerSocket(2555);
                System.out.println("[Client] Serveur ip: "+ InetAddress.getLocalHost().getHostAddress() +" sur le port : "+2555);


            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }catch (Exception e){
            System.out.println("[PROBLEME] Erreur lors du demarrage du serveur client :");
            e.printStackTrace();
        }
        launch();
    }

    private void launch() {
        while(!stop){
            try{
                if(!config.getTls()){
                    Socket client = clientServer.accept();
                    ClientRunnable cr = new ClientRunnable(client,this,config);
                    (new Thread(cr)).start();
                    System.out.println("[Client] "+ client.getInetAddress().getHostAddress()+ " s'est connecté !");
                }else{
                    SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                    ClientRunnable cr = new ClientRunnable(sslSocket,this,config);
                    (new Thread(cr)).start();
                    System.out.println("[Client] "+ sslSocket.getInetAddress().getHostAddress()+ " s'est connecté !");
                }
            }catch(IOException e){
                System.out.println("[PROBLEME] Erreur lors de la tentative de connexion d'un programme client :");
                e.printStackTrace();
            }
        }
    }

    private void startMulticastRecepter() {
        MulticastReceiver m = new MulticastReceiver(config, this);
        new Thread(m).start();
    }
    @Override
    public void setState(Map<String,String>states){
        Set<String> keys = states.keySet();
        for(String key:keys){
            String val = states.get(key);
            if(state.containsKey(key)) {
                state.replace(key, val);
            }else{
                state.put(key, val);
            }
        }
    }
    public Map<String, String> getState(){
        return  state;
    }

    @Override
    public void removeAllStateForProtocol(String protocol) {
        state.entrySet().removeIf(state -> state.getKey().contains(protocol.toLowerCase()));
    }


}
