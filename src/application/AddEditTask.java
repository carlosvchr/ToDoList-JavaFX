package application;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class AddEditTask implements Initializable{

	@FXML Pane rootpane;
	@FXML DatePicker dateField;
	@FXML TextField titleField;
	@FXML Label percentLblField;
	@FXML Slider percentField;
	@FXML TextArea descriptionField;
	@FXML Button addsavebtn;
	@FXML Button cancelbtn;
	
	// Error Checkpoint: he añadido el campo tabla para escribir en el fichero, pero parece que no lo almacena bien
	
	private static final String RESOURCE_DATA = "entryData";
	private static final String RESOURCE_CHECK = "checkNotNullEntry";
	ResourceBundle rb;
	
	private final String FALSE = "false";
	private final String TRUE = "true";
	
	public void loadPanel(TaskEntry te, ObservableList<TaskEntry> dataTb) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addEditTask.fxml"));
		ResourceBundle rb = new ListResourceBundle() {
			@Override
			protected Object[][] getContents() {
				if(te != null) {
					return new Object[][] {
						{RESOURCE_CHECK, TRUE},
						{RESOURCE_DATA, te}
					};
				}else {
					return new Object[][] {
						{RESOURCE_CHECK, FALSE}
					};
				}
			}
		};
		fxmlLoader.setResources(rb);
		Scene scene = null;
		try {
			scene = new Scene(fxmlLoader.load());
		} catch (IOException e) {
			System.out.println("FXML couldn't be loaded.");
			e.printStackTrace();
			return;
		}
		
		Stage s = new Stage();
		s.setScene(scene);
		s.setTitle("Create new task");
		//s.setAlwaysOnTop(true);
		s.showAndWait();
	}
	
	public void loadPanel(ObservableList<TaskEntry> dataTb) {
		loadPanel(null, dataTb);
	}
	
	private void showIncompleteFieldDialog() {
		Alert a = new Alert(AlertType.ERROR);
		a.setTitle("Incompleted fields");
		a.setHeaderText(null);
		a.setContentText("Please, complete all the fields.");
		a.showAndWait();
	}
	
	@FXML
	public void addOrSave(ActionEvent event) {
		LocalDate ld = dateField.getValue();
		if(ld == null || titleField.getText().trim().length() == 0 || descriptionField.getText().trim().length() == 0) {
			showIncompleteFieldDialog();
			return;
		}
		
		DateFormat dateFormat = new SimpleDateFormat(Main.DATE_FORMAT);
		Date d = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
		
		int percent = (int)percentField.getValue();
		String te = dateFormat.format(d)+Main.SPLIT_REGEX+titleField.getText().trim()+Main.SPLIT_REGEX+
				percent+Main.SPLIT_REGEX+descriptionField.getText().trim();
		
		IOManager writer = new IOManager();
		
		String reg = "";
		if(((String)rb.getObject(RESOURCE_CHECK)).compareTo(TRUE) == 0) {
			TaskEntry tereg = (TaskEntry)rb.getObject(RESOURCE_DATA);
			reg = tereg.getDate()+Main.SPLIT_REGEX+tereg.getTitle()+Main.SPLIT_REGEX+tereg.getPercent()+
					Main.SPLIT_REGEX+tereg.getDescription();
		}
		
		/* Read all the database */
		ArrayList<String> db = new ArrayList<>();
		IOManager dbReader = new IOManager();
		try {
			dbReader.open(Main.DB_PATH, true, false);
			String line;
			while((line = dbReader.getLine()) != null) {
				db.add(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			try {
				dbReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		try {
			writer.open(Main.DB_PATH, false, false);
			if(reg.length() > 0) {
				boolean readed = false;	// This delete duplicated registers
				for(int i=0; i<db.size(); i++) {
					if(reg.compareTo(db.get(i))==0) {
						if(!readed) {
							writer.putLine(te);
							readed = true;
						}
					}else {
						writer.putLine(db.get(i));
					}	
				}
			}else {
				for(int i=0; i<db.size(); i++) {
					writer.putLine(db.get(i));
				}
				writer.putLine(te);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Main.reloadTable();
		Stage stage = (Stage) cancelbtn.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void cancelTask(ActionEvent event) {
		Stage stage = (Stage) cancelbtn.getScene().getWindow();
		stage.close();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// Slider listener
		percentField.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            		percentLblField.setText((int)percentField.getValue()+"% Compl.");
                }
            });
		
		// Disable horizontal scroll bar from the textarea
		descriptionField.setWrapText(true);
		
		// This set the date format for the date picker
		dateField.setConverter(new StringConverter<LocalDate>()
		{
		    private DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern(Main.DATE_FORMAT);

		    @Override
		    public String toString(LocalDate localDate){
		        if(localDate==null)
		            return "";
		        return dateTimeFormatter.format(localDate);
		    }

		    @Override
		    public LocalDate fromString(String dateString){
		        if(dateString==null || dateString.trim().isEmpty()){
		            return null;
		        }
		        return LocalDate.parse(dateString,dateTimeFormatter);
		    }
		});
		
		rb = resources;
		
		// Obtain the resources
		TaskEntry data = null;
		if(resources.getString(RESOURCE_CHECK).compareTo(FALSE) != 0) {
			data = (TaskEntry)resources.getObject(RESOURCE_DATA);
		}
		
		// If data is null when loading this class, mean that it is a new entry
		if(data == null) {
			addsavebtn.setText("Add");
		}else {
			addsavebtn.setText("Modify");
			titleField.setText(data.getTitle());
			descriptionField.setText(data.getDescription());
			percentField.setValue(Integer.parseInt(data.getPercent()));			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Main.DATE_FORMAT);
			LocalDate ld = LocalDate.parse(data.getDate(), formatter);		
			dateField.setValue(ld);
		}
		
	}
}
