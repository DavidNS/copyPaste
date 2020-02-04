package copyPaste.copyPaste;

import java.io.IOException;

import copyPaste.copyPaste.gui.CopyPasteController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CopyPasteApp extends Application {

	private static final String MAIN_WINDOW_FXML =  "MainWindow.fxml";

	private CopyPasteController copyPasteAppController;

	private ProcessesControl processesControl = ProcessesControl.getInstance();

	public static void main(String[] args) {
		
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		processesControl.startBackProcesses();
		
		copyPasteAppController = loadMainLayout(primaryStage).getController();
		copyPasteAppController.initStage(primaryStage);

		processesControl.setCopyPasteAppController(copyPasteAppController);
		copyPasteAppController.setProcessesControl(processesControl);

		primaryStage.show();
	}

	private void finish() {
		processesControl.finishAllProcesses();
	}

	private FXMLLoader loadMainLayout(Stage primaryStage) {
		try {
			CopyPasteApp.class.getClassLoader().getResource(MAIN_WINDOW_FXML);
			FXMLLoader fxmlLoader = new FXMLLoader(CopyPasteApp.class.getClassLoader().getResource(MAIN_WINDOW_FXML));
			AnchorPane mainPane = fxmlLoader.load();
			Scene scene = new Scene(mainPane);
			primaryStage.setTitle("Copy-Pasteator");
			primaryStage.setMinHeight(240);
			primaryStage.setMinWidth(160);
			primaryStage.setScene(scene);
			primaryStage.sizeToScene();
			primaryStage.centerOnScreen();
			primaryStage.setOnCloseRequest((e) -> {
				finish();
			});
			return fxmlLoader;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
