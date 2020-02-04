package copyPaste.copyPaste.gui.copyPasteCont;

import java.io.File;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class PathCellController extends ListCell<File> {
	
	private static final String PATH_WINDOW_FXML = "PathElement.fxml";
	
	private File item;
	
    @FXML
    private Button buttonCancel;
	
    @FXML
    private Label pathLabel;
    
    
    public PathCellController() {
    	loadFXML();
    	buttonCancel.setVisible(false);
	}
    

	public PathCellController(ObservableList<File> observaleList) {
	 	loadFXML();
	 	loadHandlers(observaleList);
	}


	private void loadHandlers(ObservableList<File> observaleList) {
		buttonCancel.setOnAction((e)->{observaleList.remove(item);});
		
	}


	private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PathCellController.class.getClassLoader().getResource(PATH_WINDOW_FXML));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if(empty) {
           setText(null);
           setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
        else {
        	this.item=item;
        	pathLabel.setText(item.getAbsolutePath());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
