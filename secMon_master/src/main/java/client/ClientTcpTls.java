package client;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import be.secMon.crypto.Aes;



public class ClientTcpTls implements Runnable {

    private SSLSocket sslSocket = null;
    private Socket socket =null;
    private BufferedReader in;
    private boolean serverOn=true;
    private Aes aes = new Aes("ThWmZq4t7w!z%C*F-JaNdRgUjXn2r5u8");
    private PrintWriter out;
    private boolean isTls;
    
    public ClientTcpTls(int port, InetAddress ip, boolean isTls) {
    	this.isTls = isTls;
        try {
        	
        	if(!isTls) {
        		socket = new Socket(ip,port);
        		serverOn = true;
        	}else {
	            KeyStore keystore = KeyStore.getInstance("PKCS12");
	            keystore.load(new FileInputStream(getClass().getResource("/group11.monitor.p12").getPath()), "group11".toCharArray());
	            KeyManagerFactory kf = KeyManagerFactory.getInstance("SUNX509");
	            kf.init(keystore, "group11".toCharArray());
	            TrustManagerFactory t = TrustManagerFactory.getInstance("X509");
	            t.init(keystore);
	            SSLContext sc = SSLContext.getInstance("TLS");
	            TrustManager[] tm = t.getTrustManagers();
	            sc.init(kf.getKeyManagers(), tm, null);
	            SSLSocketFactory ssf = sc.getSocketFactory();
	            this.sslSocket = (SSLSocket)ssf.createSocket(ip, port);
	            this.sslSocket.startHandshake();
	            serverOn = true;
        	}
        }catch(ConnectException e) {
        	serverOn = false;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
            serverOn = false;
        }

    }
    public boolean serverIsOpen() {
    	return serverOn;
    }
    
    @Override
	public void run() {
    	if(!serverOn)return;
		try {
	    	if(!isTls) {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
	            out = new PrintWriter(socket.getOutputStream(), true);
			}else {
				 in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), Charset.forName("UTF-8")));
		         out = new PrintWriter(sslSocket.getOutputStream(), true);
			}
		}catch(Exception e ) {
			e.printStackTrace();
		}
		
	}

    public String addServ(String augmentedUrl) {
    	String message = "N\'est pas ajoute";
    	if((sslSocket!=null && sslSocket.isConnected())||(socket!=null && socket.isConnected())) {
	        String addServiceReq = String.format("ADDSRV %s\r\n",augmentedUrl);
	        out.println(aes.encrypt(addServiceReq));
	        out.flush();
	        try {
	        	String recipt = in.readLine();
	            String msg = aes.decrypt(recipt);
	            if(RegularExpression.addServiceResqMatch(msg)) {
	               message = msg;
	            }
	        }catch(SSLException e) {
	        	return "Connection interrompu";
	        } catch (IOException e) {
	           e.printStackTrace();
	        }
    	}
        
        return message;
    }

    public List<String> getListServ(){
        List<String> serv = new ArrayList<>();
        if((sslSocket!=null && sslSocket.isConnected())||(socket!=null && socket.isConnected())){
            out.println(aes.encrypt("LISTSRV\r\n"));
            out.flush();
            try {
                
                String msg = aes.decrypt(in.readLine());
                if(RegularExpression.isServerList(msg)) {
                    serv = RegularExpression.getServerList(msg);
                }
            }catch(SSLException e) {
            	return new ArrayList<String>();
            
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serv;
    }
    public String getStateServ(String id){
        String stat = "";
        if((sslSocket!=null && sslSocket.isConnected())||(socket!=null && socket.isConnected())) {
            out.println(aes.encrypt(String.format("STATESRV %s\r\n",id)));
            out.flush();
            try {
                
                String msg = aes.decrypt(in.readLine());
                if(RegularExpression.isStateReq(msg)) {
                    stat = msg;
                }else {
                	stat = "Indisponnible";
                }
            }catch(SSLException e) {
            	return "Connection interrompu";
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
        return stat;
    }

	public void close() {
		out.print(aes.encrypt("exit"));
		try {
			if(sslSocket!=null)sslSocket.close();
			if(socket!=null)socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	


}
