package de.Fuchsi_II.Fuchsis_server_explorer;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.Fuchsi_II.Fuchsis_server_explorer.Index;

public class Index extends JFrame{
	//just there to remove warning
	private static final long serialVersionUID = 1L;
	
	String version = "1.1.1";
	boolean update = false;
	String ghversion = "";
	
	Gson gson = new Gson();
	JsonObject json;
	JsonArray vjson;
	
	//create windows items
	JPanel jpPanel = new JPanel();
	
	DefaultListModel<Object> model = new DefaultListModel<Object>();
	JList<Object> list = new JList<Object>(model);
	JScrollPane scrollp = new JScrollPane(list,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	JLabel lblerr = new JLabel();
	JButton btnopendef = new JButton();
    
	
	public Index(){
		super();
		this.setTitle("Fuchsis-server Explorer V" + version);
		this.setSize(700,600);
		try {
			FensterAufbauen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getCurrentListItem() {
		if (list.getSelectedIndex() == -1) {
			return "";
		}else {
			return model.get(list.getSelectedIndex()).toString();
		}
	}
	
	public void FensterAufbauen() throws Exception{
		jpPanel.setLayout(null);
		
		//setup erroer label
		lblerr.setText("");
		lblerr.setBounds(25, 20, 700, 40);
		lblerr.setFont(lblerr.getFont().deriveFont(10.0f));
		lblerr.setForeground(Color.RED);
		jpPanel.add(lblerr);
	    
		
		//get ports.json
		String url = "http://207.180.246.155/ports.json";
	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	    // optional default is GET
	    con.setRequestMethod("GET");
	    //add request header
	    con.setRequestProperty("User-Agent", "Mozilla/5.0");
	    int responseCode = con.getResponseCode();
	    if (responseCode != 200) {
	    	lblerr.setText("Server antwortete mit status " + responseCode + ". Es ist möglicherweise ein fehler aufgetreten");
	    }
	    BufferedReader in = new BufferedReader(
	            new InputStreamReader(con.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();
	    while ((inputLine = in.readLine()) != null) {
	    	response.append(inputLine);
	    }
	    in.close();
	    //print in String
	    String tojson = response.toString();
	    tojson.replaceAll("	", "");
	    //System.out.println(tojson);
	    
	    json = gson.fromJson(tojson, JsonObject.class);
		
		//list model setup
		for(int i = 0; i< json.size();i++) {
			model.addElement("Game: " + json.keySet().toArray()[i]);
			for(int ii = 0; ii< json.getAsJsonArray((String) json.keySet().toArray()[i]).size();ii++) {
				model.addElement("    server: " + json.getAsJsonArray((String) json.keySet().toArray()[i]).get(ii).getAsJsonObject().get("name").getAsString());
				model.addElement("        IP: 207.180.246.155:" + json.getAsJsonArray((String) json.keySet().toArray()[i]).get(ii).getAsJsonObject().get("port").getAsInt());
			}
		}
		//setup list
		list.setSelectedIndex(0); 
		
		//setup ScrollPane
		scrollp.setBounds(25, 100, 400, 400);
		jpPanel.add(scrollp);
		
		//setup open default button
		btnopendef.setText("IP adresse kopieren");
		btnopendef.setBounds(450, 200, 200, 50);
		jpPanel.add(btnopendef);
		btnopendef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if (!update) {
					if (getCurrentListItem().contains("IP:")) {
						StringSelection stringSelection = new StringSelection(getCurrentListItem().replaceAll("        IP: ", ""));
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(stringSelection, null);
					}
				}else {
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					    try {
							Desktop.getDesktop().browse(new URI("https://github.com/Fuchsi2/Fuchsis-server_Explorer/releases/tag/" + ghversion + "/Fuchsis-server_explorer.jar"));
						} catch (IOException | URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		
		//check for update on github
		String vurl = "https://api.github.com/repos/Fuchsi2/Fuchsis-server_Explorer/releases";
	    URL vobj = new URL(vurl);
	    HttpURLConnection vcon = (HttpURLConnection) vobj.openConnection();
	    vcon.setRequestMethod("GET");
	    vcon.setRequestProperty("User-Agent", "Mozilla/5.0");
	    int vresponseCode = vcon.getResponseCode();
	    if (vresponseCode != 200) {
	    	lblerr.setText("Server antwortete mit status " + vresponseCode + ". Es ist möglicherweise ein fehler aufgetreten");
	    }
	    BufferedReader vin = new BufferedReader(new InputStreamReader(vcon.getInputStream()));
	    String vinputLine;
	    StringBuffer vresponse = new StringBuffer();
	    while ((vinputLine = vin.readLine()) != null) {
	    	vresponse.append(vinputLine);
	    }
	    vin.close();
	    String vtojson = vresponse.toString();
	    System.out.println(vtojson);
	    vjson = gson.fromJson(vtojson, JsonArray.class);
	    ghversion = vjson.get(0).getAsJsonObject().get("tag_name").getAsString();
	    if (!ghversion.contentEquals(version)) {
	    	lblerr.setText("Neue version verfügbar! bitte lade dir die neue version herunter.");
	    	update = true;
	    	btnopendef.setText("Update herunterladen");
	    }
		
		this.add(jpPanel);
	}
	
	public static void main(String[] args){
		Index g  = new Index();
		g.setVisible(true);
		
	}
}
