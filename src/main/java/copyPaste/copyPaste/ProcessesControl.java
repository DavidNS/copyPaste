package copyPaste.copyPaste;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import copyPaste.copyPaste.data.Lock;
import copyPaste.copyPaste.data.OrderCopyPaste;
import copyPaste.copyPaste.gui.CopyPasteController;
import copyPaste.copyPaste.processesControl.CopyPasteExecutor;
import copyPaste.copyPaste.processesControl.UserRequestExecutor;
import javafx.application.Platform;
import javafx.collections.ObservableList;

public class ProcessesControl {

	private CopyPasteController copyPasteAppController;

	private CopyPasteExecutor copyPasteExecutor = new CopyPasteExecutor();

	private UserRequestExecutor userRequestExecutor = new UserRequestExecutor();

	private static ProcessesControl instance;
	


	@FunctionalInterface
	public interface VoidConsumer {
		public void consume();
	}

	private ProcessesControl() {
	}

	public static ProcessesControl getInstance() {
		if (instance == null) {
			instance = new ProcessesControl();
		}
		return instance;
	}

	public void startBackProcesses() {
		new Thread(copyPasteExecutor, "CopyPasteExecutor").start();
		new Thread(userRequestExecutor, "UserRequestExecutor").start();
	}

	public void finishAllProcesses() {
		copyPasteExecutor.setLiving(false);
		userRequestExecutor.setLiving(false);
	}

	public void cloneRequest(ObservableList<File> inputList, ObservableList<File> outputList,
			ObservableList<OrderCopyPaste> progressList) {
		userRequestExecutor.addUserOrdercloneRequest(inputList, outputList, progressList);
	}

	public void removeOrderFromGui(OrderCopyPaste orderCopyPaste) {
		Platform.runLater(() -> {
			copyPasteAppController.removeOrder(orderCopyPaste);
		});
	}

	public void setCopyPasteAppController(CopyPasteController copyPasteAppController) {
		this.copyPasteAppController = copyPasteAppController;
	}

	public void updateOrderInGui(int joinResult) {
		Platform.runLater(() -> {
			copyPasteAppController.updateOrder(joinResult);

		});
	}

	public void addOrderInGui(OrderCopyPaste orderCopyPaste) {
		Platform.runLater(() -> {
			copyPasteAppController.addOrder(orderCopyPaste);
		});
	}

	public void addOrderCopyPasteProcessLocked(OrderCopyPaste orderCopyPaste) {
		copyPasteExecutor.addOrderCopyPasteProcessLocked(orderCopyPaste);
	}

	public void addOrderCopyPasteProcess(OrderCopyPaste orderCopyPaste) {
		copyPasteExecutor.addOrderCopyPasteProcess(orderCopyPaste);
	}

	public boolean isOrderWriting(OrderCopyPaste orderInProgress) {
		return copyPasteExecutor.isOrderWriting(orderInProgress);
	}

	public void updateOrderOnGui(OrderCopyPaste orderCopyPaste) {
		Platform.runLater(() -> {
			copyPasteAppController.updateOrder(orderCopyPaste);
		});
	}

	public void showChooseContinueOption(Lock lock, String[] options, StringBuilder result, String cause) {
		Platform.runLater(() -> {
			userRequestExecutor.choseOption(options,result,cause);
			lock.setLocked(false);
			lock.notifyLock();
		});
		lock.waitLock();
	}

	public void showChoseCloneOption(ObservableList<OrderCopyPaste> progressList,
			HashMap<File, ArrayList<File>> inVsOut, ArrayList<File> outFilesThatExist, String[] options, String cause) {
		Platform.runLater(() -> {
			userRequestExecutor.processCloneOption(progressList, inVsOut, outFilesThatExist, userRequestExecutor.choseOption(options,cause));
		});

	}	
	

}
