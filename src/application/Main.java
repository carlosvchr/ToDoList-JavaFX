package application;
	
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Main extends Application {
	
	public static final String RBTN_TOOLBAR_CLASS = "radiobutton-toolbar";
	
	public static String DB_PATH = System.getProperty("user.dir")+System.getProperty("file.separator")+"tasksdatabase.txt";
	public static final String SPLIT_REGEX = ";;";
	public static final String DATE_FORMAT = "yyyy/MM/dd";
	//public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	private static RadioButton rbAll, rbOverdue, rbToday, rbThisWeek;
	private static CheckBox cbTasksFinished;
	
	private static ObservableList<TaskEntry> dataTb;
	private static Stage stage;

	
	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		primaryStage.setMinWidth(400);
		primaryStage.setMinHeight(300);
		primaryStage.setTitle("ToDo List");
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,640,480);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			dataTb = FXCollections.observableArrayList();
			
			loadMenuBar(root);
			loadToolBar(root);
			loadContent(root);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	/** Load the entire toolbar component */
	public static void loadToolBar(BorderPane root) {
		BorderPane bp = new BorderPane();
		bp.setId("toolbar-container");
		
		HBox filters = new HBox();
		ToggleGroup group = new ToggleGroup();
		
		rbAll = new RadioButton("All");
		rbAll.setToggleGroup(group);
		rbAll.setSelected(true);
		rbAll.getStyleClass().add(RBTN_TOOLBAR_CLASS);
		rbAll.setOnAction(value->{ reloadTable(); });
		
		rbOverdue = new RadioButton("Overdue");
		rbOverdue.setToggleGroup(group);
		rbOverdue.getStyleClass().add(RBTN_TOOLBAR_CLASS);
		rbOverdue.setOnAction(value->{ reloadTable(); });
		
		rbToday = new RadioButton("Today");
		rbToday.setToggleGroup(group);
		rbToday.getStyleClass().add(RBTN_TOOLBAR_CLASS);
		rbToday.setOnAction(value->{ reloadTable(); });
		
		rbThisWeek = new RadioButton("This week");
		rbThisWeek.setToggleGroup(group);
		rbThisWeek.getStyleClass().add(RBTN_TOOLBAR_CLASS);
		rbThisWeek.setOnAction(value->{ reloadTable(); });
		
		filters.getChildren().addAll(rbAll, rbOverdue, rbToday, rbThisWeek);
		
		cbTasksFinished = new CheckBox("Not completed");
		cbTasksFinished.setSelected(false);
		cbTasksFinished.setOnAction(value->{ reloadTable(); });
		
		Label filterTitle = new Label("Filters");
		filterTitle.setId("filter-title-lbl");
		
		bp.setTop(filterTitle);
		bp.setCenter(filters);
		bp.setRight(cbTasksFinished);
		
		VBox cont = new VBox();
		cont.setId("filters-container");
		cont.getChildren().addAll(filterTitle, bp);
			
		root.setCenter(cont);
	}
	
	
	/** Read the database and load the table again */
	public static void reloadTable() {
		// Reading the database
		readDataBase();
		
		// Checking the selected filter. If the element doesn't match, then is removed
		if(cbTasksFinished.isSelected()) {
			for(int i=0; i<dataTb.size(); i++) {
				if(dataTb.get(i).getTaskFinished().isSelected()) {
					dataTb.remove(i);
					i--;
				}
			}
		}
		
		if(rbOverdue.isSelected()) {
			for(int i=0; i<dataTb.size(); i++) {
				if(!dataTb.get(i).isOverdue()) {
					dataTb.remove(i);
					i--;
				}
			}
		}
		
		if(rbToday.isSelected()) {
			for(int i=0; i<dataTb.size(); i++) {
				if(!dataTb.get(i).isForToday()) {
					dataTb.remove(i);
					i--;
				}
			}
		}
		
		if(rbThisWeek.isSelected()) {
			for(int i=0; i<dataTb.size(); i++) {
				if(!dataTb.get(i).isForThisWeek()) {
					dataTb.remove(i);
					i--;
				}
			}
		}
	}
	
	/** Read the data from a external file */
	public static void readDataBase(){
		dataTb.clear();
		
		IOManager reader = new IOManager();
		try {
			reader.open(DB_PATH, true, false);
			String line;
			while((line = reader.getLine()) != null) {
				String[] attrs = line.split(SPLIT_REGEX);
				dataTb.add(new TaskEntry(attrs[0], attrs[1], attrs[2], attrs[3]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ;
	}
	
	
	/** Create the table, set the colums and laod the data */
	public static void loadContent(BorderPane root){
		VBox vcont = new VBox();
		
		vcont.getChildren().add(loadTable());
		
		root.setBottom(vcont);
	}
	
	
	@SuppressWarnings("unchecked")
	public static TableView<TaskEntry> loadTable(){
		TableView<TaskEntry> contentTable = new TableView<TaskEntry>();
		contentTable.setEditable(false);
		contentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		String centerColClass = "table-col-center";
		
        TableColumn<TaskEntry, CheckBox> colCheck = new TableColumn<TaskEntry, CheckBox>();
        colCheck.getStyleClass().add(centerColClass);
        colCheck.setMaxWidth(40);
        colCheck.setMinWidth(40);
        colCheck.setPrefWidth(40);
        //colCheck.setId("coltaskdone");	// This specify the same as above but in css. But it doesn't work properly
        colCheck.setEditable(false);
        colCheck.setCellValueFactory( new PropertyValueFactory<TaskEntry, CheckBox>("taskFinished"));
        TableColumn<TaskEntry, String> colDate = new TableColumn<TaskEntry, String>("DueDate");
        colDate.setCellValueFactory( new PropertyValueFactory<TaskEntry, String>("date"));
        colDate.getStyleClass().add(centerColClass);
        colDate.setMaxWidth(100);
        colDate.setMinWidth(100);
        colDate.setPrefWidth(100);
        //colDate.setId("coldate");		// This specify the same as above but in css. But it doesn't work properly
        TableColumn<TaskEntry, String> colTitle = new TableColumn<TaskEntry, String>("Title");
        colTitle.setCellValueFactory( new PropertyValueFactory<TaskEntry, String>("title"));
        TableColumn<TaskEntry, String> colPercentage = new TableColumn<TaskEntry, String>("% Complete");
        colPercentage.setCellValueFactory( new PropertyValueFactory<TaskEntry, String>("percent"));
        colPercentage.getStyleClass().add(centerColClass);
        colPercentage.setMaxWidth(100);
        colPercentage.setMinWidth(100);
        colPercentage.setPrefWidth(100);
        TableColumn<TaskEntry, String> colDescription = new TableColumn<TaskEntry, String>("Description");
        colDescription.setCellValueFactory( new PropertyValueFactory<TaskEntry, String>("description"));
        
        
        contentTable.getColumns().addAll(colCheck, colDate, colTitle, colPercentage, colDescription);
       
        // When clicking on the table, TaskEntry associated with the row is obtained
        contentTable.setOnMouseClicked(value->{
        	if(value.getButton().equals(MouseButton.PRIMARY)){
                if(value.getClickCount() == 2){
                	// And then, we open a dialog to modify it
                	TaskEntry te = contentTable.getSelectionModel().getSelectedItem();
                	AddEditTask aet = new AddEditTask();
                	aet.loadPanel(te, dataTb);
                }
            }
        });
        
        contentTable.setItems(dataTb);
        
        readDataBase();
      
        return contentTable;		
	}
	
	/** Load the menu bar and set the menu item actions */
	public static void loadMenuBar(BorderPane root){
		 final Menu menu1 = new Menu("File");
		 
		 final MenuItem mi1 = new MenuItem("New", null);
		 mi1.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		 mi1.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				AddEditTask adt = new AddEditTask();
				adt.loadPanel(dataTb);
			}		 
		 });
		 
		 final MenuItem miChangeDb = new MenuItem("Change database", null);
		 miChangeDb.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				Alert a = new Alert(AlertType.CONFIRMATION);
				a.setTitle("Change workspace");
				a.setHeaderText("Do you want to change the source data file?");
			
				a.getDialogPane().setContent(new Label("Current source: "+DB_PATH));
				
				ButtonType buttonTypeChange = new ButtonType("Change");
				ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

				a.getButtonTypes().setAll(buttonTypeChange, buttonTypeCancel);

				Optional<ButtonType> result = a.showAndWait();
				if (result.get() == buttonTypeChange){
				    FileChooser fd = new FileChooser();
				    File f = fd.showOpenDialog(stage);
				    if(f != null) {
				    	DB_PATH = f.getAbsolutePath();
				    	Alert infoConf = new Alert(AlertType.CONFIRMATION);
				    	infoConf.setTitle("Information");
				    	infoConf.setHeaderText(null);
				    	infoConf.setContentText("Database has been changed successfully.");
				    	infoConf.showAndWait();
				    	Main.reloadTable();
				    }
				}
			}		 
		 });
		 
		 
		 final MenuItem mi2 = new MenuItem("Exit", null);
		 mi2.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				Platform.exit();
				System.exit(0);
			}		 
		 });
		 
		 menu1.getItems().add(mi1);
		 menu1.getItems().add(miChangeDb);
		 menu1.getItems().add(new SeparatorMenuItem());
		 menu1.getItems().add(mi2);
		 MenuBar menuBar = new MenuBar();
		 menuBar.getMenus().addAll(menu1);
		 root.setTop(menuBar);
	}
}
