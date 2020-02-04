package copyPaste.copyPaste.gui.copyPasteCont;


import java.io.File;

import copyPaste.copyPaste.data.OrderCopyPaste;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class OrderCopyPasteCellConroller extends ListCell<OrderCopyPaste> {
	
	private static final String PATH_WINDOW_FXML = "OrderCopyPasteElement.fxml";
	
	private OrderCopyPaste orderCopyPaste;
	
	private ObservableList<File> outputList=FXCollections.observableArrayList();
	
    @FXML
    private Button cancelButton;
	
    @FXML
    private Label inputFileLabel;
    
    @FXML
	private ListView<File>  outputListView;
    
    
    public OrderCopyPasteCellConroller() {
    	loadFXML();
	}

	private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(OrderCopyPaste.class.getClassLoader().getResource(PATH_WINDOW_FXML));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
            outputListView.setCellFactory((s)->{return new PathCellController();});
            outputListView.setItems(outputList);
            cancelButton.setOnAction((e)->{ 
            	orderCopyPaste.setCancelled(true);
            	e.consume();
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void updateItem(OrderCopyPaste item, boolean empty) {
        super.updateItem(item, empty);
        if(empty ) {
           setText(null);
           setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
        else {
        	orderCopyPaste=item;

        	inputFileLabel.setText(item.getInputFile().getAbsolutePath());
        	updateStatusColor(item);
        	outputList.clear();
        	outputList.addAll(item.getOutputFiles());

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
    
    private void updateStatusColor(OrderCopyPaste item) {
    	if(item.getCancelled().get()) {
    		inputFileLabel.setStyle("-fx-background-color: red;");
    	}else if(!item.isReading()) {
    		inputFileLabel.setStyle("-fx-background-color: yellow;");
    	}else if(!item.isWritting()) {
    		inputFileLabel.setStyle("-fx-background-color: lightblue;");
    	}else {
    		inputFileLabel.setStyle("-fx-background-color: darkblue;");
    	}

    }
}
