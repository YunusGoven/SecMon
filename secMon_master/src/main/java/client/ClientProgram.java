package client;

import java.io.IOException;
import java.net.InetAddress;

import client.ClientTcpTls;
import client.IPresenter;
import client.Presenter;
import client.IView;
import client.ViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientProgram extends Application {

	private static ClientTcpTls client;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root;
		try {
			//TODO mettre son adresse ipv4 au lieu de 0.0.0.0
			client =new ClientTcpTls(2555,InetAddress.getByName("0.0.0.0"),true);
			FXMLLoader load = new FXMLLoader(getClass().getResource("/main.fxml"));
			IPresenter presenter = new Presenter(client);
			IView view = new ViewController(presenter);
			load.setController(view);
			presenter.setViewController(view);
			root = load.load();
			Scene scene = new Scene(root, 800,400);
			primaryStage.setTitle("Reseaux");
            primaryStage.setScene(scene);
            primaryStage.show();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void stop() throws Exception {
		if(client.serverIsOpen())client.close();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	
}
