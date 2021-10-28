package client;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class ViewController implements Initializable, IView{
	@FXML
	private ListView<String> listServLV;
	@FXML
	private TextArea stateArea;
	@FXML
	private TextField servAddTF;
	@FXML
	private Text addMessage;
	
	private String selected;
	private IPresenter presenter;
	public ViewController(IPresenter presenter) {
		selected = "";
		this.presenter = presenter;
	}
	
	@Override
    public void initialize(URL url, ResourceBundle rb) {
    }
	
	@FXML
	public void addServ() {
		presenter.addServ(getServFromUser());
	}
	@FXML
	public void stateServReq(MouseEvent mouseEvent) {
		if(!listServLV.getItems().isEmpty()) {
			String selected = (String)listServLV.getSelectionModel().getSelectedItem();
			this.selected = selected;
			presenter.setState(selected);
		}
	}
	@FXML
	public void listServReq() {
		presenter.setList();
	}
	
	@Override
	public void updateListServ(List<String> serv) {
		listServLV.getItems().clear();
		for(String val:serv) {
			listServLV.getItems().add(val);
		}
	}

	@Override
	public void updateStateServ(String state) {
		stateArea.clear();
		stateArea.setText(state);
		if("Indisponnible".equalsIgnoreCase(state) && !selected.isEmpty()) {
			listServLV.getItems().remove(selected);
			selected = "";
		}
	}

	@Override
	public void updateAddServ(String mesg) {
		addMessage.setText(mesg);
	}
	
	private String getServFromUser() {
		return servAddTF.getText().trim();
	}

}
