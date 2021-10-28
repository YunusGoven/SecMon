package be.secMon.connect;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import be.secMon.main.daemon.ILauncher;
import be.secMon.regex.RegularExpression;

public class MulticastReceiver implements Runnable{

    private MulticastSocket multicastSocket;
    private InetAddress group;
    private Config config;
    private ILauncher launcher;

    public MulticastReceiver(Config config, ILauncher launcher){
        this.config = config;
        this.launcher=launcher;
        try{
            multicastSocket = new MulticastSocket(config.getMulticastPort());
            group = InetAddress.getByName(config.getAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
        	System.out.println(group.getHostAddress());
            multicastSocket.joinGroup(group);
            System.out.printf("[MULTICAST] Connectee au groupe : %s sur le port %d\r\n",config.getAddress(), config.getMulticastPort());
        }catch (Exception e){
            e.printStackTrace();
        }
        byte[]buffer = new byte[1024];
        DatagramPacket messageReceived = new DatagramPacket(buffer, buffer.length);
        while(true){
            try {
                multicastSocket.receive(messageReceived);
                String message = new String(buffer);
                System.out.println("[MULTICAST] Message recu : "+message);
                checkMessage(message,messageReceived.getAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkMessage(String message, InetAddress address) {
        if(RegularExpression.isAnnouceMessage(message)){
            checkMessageAndInterractWithTcp(message,address,true);
        }else if(RegularExpression.isNotificationMessage(message)){
            checkMessageAndInterractWithTcp(message,address,false);
        }else if(RegularExpression.isGoodByeMessage(message)){
            String protocol = RegularExpression.getProtocolName(message);
            System.out.println("["+protocol+"] n'est plus actif!") ;
            launcher.removeAllStateForProtocol(protocol);
        }
    }

    private void checkMessageAndInterractWithTcp(String message, InetAddress address,boolean isConfig){
        String protocol = RegularExpression.getProtocolName(message);
        int port = RegularExpression.getPort(message);
        if(protocol ==null || port ==-1) return;
        try{
            Socket socket = new Socket(address,port);
            TcpProbeClient tcpProbeClient = new TcpProbeClient(socket, config, protocol);
            if(isConfig)
                tcpProbeClient.sendConfig();
            else {
                launcher.setState(tcpProbeClient.askStateReq());
            }
        }catch (ConnectException r){
            System.out.println(address.getHostAddress()+ " vous a refuse l'acces");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
