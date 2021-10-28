package client;

import client.IView;

public interface IPresenter {
	void setList();
	void setState(String id);
	void addServ(String serv );
	void setViewController(IView v);

}
