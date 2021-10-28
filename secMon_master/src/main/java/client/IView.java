package client;

import java.util.List;

public interface IView {
	void updateListServ(List<String> serv);
	void updateStateServ(String state);
	void updateAddServ(String mesg);
}
