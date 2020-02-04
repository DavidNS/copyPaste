package copyPaste.copyPaste.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import copyPaste.copyPaste.ProcessesControl;
import copyPaste.copyPaste.data.OrderCopyPaste;
import copyPaste.copyPaste.gui.copyPasteCont.OrderCopyPasteCellConroller;
import copyPaste.copyPaste.gui.copyPasteCont.PathCellController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CopyPasteController implements Initializable {

	private ProcessesControl processesControl;

	private ObservableList<File> inputList=FXCollections.observableArrayList();

	private ObservableList<File> outputList=FXCollections.observableArrayList();
	
	private ObservableList<OrderCopyPaste> progressList=FXCollections.observableArrayList();
	
	
	private static final String CSS_MAIN_ANCHOR_PANE = "main-anchor-pane";

	private static final String CSS_TITTLE_ANCHOR_PANE = "tittle-anchor-pane";

	private static final String CSS_CONTENT_ANCHOR_PANE = "content-anchor-pane";
	
	@FXML
	private Button buttonCancelAllFiles;
	
	@FXML
	private Button buttonCancelAllFolders;
	
	@FXML
	private Button buttonCancelAllOrders;
	
	@FXML
	private Button buttonClone;
	
	@FXML
	private Button buttonFiles;
	
	@FXML
	private Button buttonFolders;
	
	@FXML
	private AnchorPane mainAnchorPane;

	@FXML
	private AnchorPane tittleAnchorPane;

	@FXML
	private AnchorPane contentAnchorPane;

	@FXML
	private ListView<File> inputListView;

	@FXML
	private ListView<File>  outputListView;

	@FXML
	private ListView<OrderCopyPaste> progressListView;


	public void initialize(URL location, ResourceBundle resources) {
		mainAnchorPane.getStyleClass().add(CSS_MAIN_ANCHOR_PANE);
		tittleAnchorPane.getStyleClass().add(CSS_TITTLE_ANCHOR_PANE);
		contentAnchorPane.getStyleClass().add(CSS_CONTENT_ANCHOR_PANE);
	}

	public void initStage(Stage stage) {
		createEntries(stage,(f)->{return f.isDirectory();}, inputListView, inputList,buttonCancelAllFiles );		
		buttonFiles.setOnAction((e)->{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Resource File");
            addFileToList(fileChooser.showOpenDialog(stage), inputList);
		});
		createEntries(stage,(f)->{return f.isFile();}, outputListView, outputList,buttonCancelAllFolders );
		buttonFolders.setOnAction((e)->{
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Open Resource Folder");
            addFileToList(chooser.showDialog(stage), outputList);
		});
		
		progressListView.setCellFactory((pE)->{return new OrderCopyPasteCellConroller();});
		progressListView.setItems(progressList);
		buttonClone.setOnAction(
				(e)->{
					processesControl.cloneRequest(inputList, outputList, progressList); });
		buttonCancelAllOrders.setOnAction((e)->{
			progressList.stream().forEach((f)->{f.setCancelled(true);
		});
		
		});
		
	}

	private void addFileToList(File file, ObservableList<File> observaleList) {
		if(file!=null&&!observaleList.stream().anyMatch((f)->{return f.getAbsolutePath().equals(file.getAbsolutePath());})) {
			observaleList.add(file);
		}
	}
	
	private void createEntries(Stage stage, Predicate<? super File> excludePred, ListView<File> listView, ObservableList<File>  observaleList, Button cancellAllButton) {
		listView.setCellFactory((s)->{return new PathCellController(observaleList);});
		listView.setItems(observaleList);
		listView.setOnDragOver((evnt)->{
             Dragboard db = evnt.getDragboard();
             if (db.hasFiles()&& !db.getFiles().stream().anyMatch(excludePred))
             {
                 evnt.acceptTransferModes(TransferMode.COPY);
            	 
             }
             evnt.consume();
		});
		listView.setOnDragDropped((evnt)->{
            Dragboard db = evnt.getDragboard();
            if (db.hasFiles())
            {
            	db.getFiles().stream().forEach((file)->{addFileToList(file, observaleList);});
            }
            evnt.consume();
		});
		
		cancellAllButton.setOnAction((e)->{ observaleList.clear(); });
	}

	public void setProcessesControl(ProcessesControl processesControl) {
		this.processesControl=processesControl;
	}

	public void removeOrder(OrderCopyPaste orderCopyPaste) {
		progressList.remove(orderCopyPaste);
	}

	public void updateOrder(int joinResult) {
		OrderCopyPaste orderCopyPaste= progressList.get(joinResult);
		progressList.remove(joinResult);
		progressList.add(joinResult, orderCopyPaste);
	}

	public void updateOrder(OrderCopyPaste orderCopyPaste) {
		int  index=	progressList.lastIndexOf(orderCopyPaste);
		progressList.remove(orderCopyPaste);
		progressList.add(index,orderCopyPaste);
	}

	public void addOrder(OrderCopyPaste orderCopyPaste) {
		progressList.add(orderCopyPaste);	
	}
}