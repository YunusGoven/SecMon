package client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.IView;

public class Presenter implements IPresenter{

	private ClientTcpTls client;
	private IView view;
	
	public Presenter(ClientTcpTls client) {
		this.client = client;
		(new Thread(client)).start();
	}
	
	@Override
	public void setList() {
		List<String> serv = new ArrayList<>();
		serv = client.getListServ();
		
		if(view!=null) {
			view.updateListServ(serv);
		}
	}

	@Override
	public void setState(String id) {
		String state = client.getStateServ(id);
		if(view!=null) {
			view.updateStateServ(state);
		}
	}

	@Override
	public void addServ(String serv) {
		
		String message =  client.addServ(serv);
		 
		if(view!=null) {
			view.updateAddServ(message);
		}
	}

	@Override
	public void setViewController(IView v) {
		this.view = v;
	}

}
