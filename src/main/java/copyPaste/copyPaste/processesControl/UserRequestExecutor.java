package copyPaste.copyPaste.processesControl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;

import copyPaste.copyPaste.ProcessesControl;
import copyPaste.copyPaste.ProcessesControl.VoidConsumer;
import copyPaste.copyPaste.data.OrderCopyPaste;
import copyPaste.copyPaste.processesControl.userRequestEx.FilesFunctions;
import javafx.collections.ObservableList;

public class UserRequestExecutor implements Runnable {

	private boolean living = false;

	private SynchronousQueue<VoidConsumer> userOrders = new SynchronousQueue<>();

	private FilesFunctions filesFuncitons = new FilesFunctions();

	@Override
	public void run() {
		living = true;
		while (living) {
			executeBackCopyPasteProcesses();
		}
	}

	private void executeBackCopyPasteProcesses() {
		try {
			VoidConsumer orderCopyPasteProcess = userOrders.take();
			orderCopyPasteProcess.consume();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	




	public void addUserOrdercloneRequest(ObservableList<File> inputList, ObservableList<File> outputList,
			ObservableList<OrderCopyPaste> progressList) {
		if (inputList.size() > 0 && outputList.size() > 0) {
			try {
				userOrders.put(() -> {
					cloneOrder(inputList, outputList, progressList);
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void cloneOrder(ObservableList<File> inputList, ObservableList<File> outputList,
			ObservableList<OrderCopyPaste> progressList) {
		HashMap<File, ArrayList<File>> inVsOut = filesFuncitons.generateUserRequestOutputFiles(inputList, outputList);
		ArrayList<File> outFilesThatExist = filesFuncitons.checkCoincidences(inVsOut, progressList);
		if (outFilesThatExist.size() > 0) {
			ProcessesControl.getInstance().showChoseCloneOption(progressList, inVsOut, outFilesThatExist,FilesFunctions.CLONE_OPTIONS, outFilesThatExist.size() +" files coincidences");
		} else {
			updateOrders(inVsOut, progressList);
		}
	}
	
	public Optional<String> choseOption(String[] options, String cause) {
		return filesFuncitons.choseOption(options,cause);
	}

	public void  choseOption(String[] options, StringBuilder result, String cause) {
		filesFuncitons.choseOption(options,result,cause);
	}

	public void processCloneOption(ObservableList<OrderCopyPaste> progressList, HashMap<File, ArrayList<File>> inVsOut,
			ArrayList<File> outFilesThatExist, Optional<String> optionSelected) {
		if (optionSelected.isPresent()) {
			userOrders.add(() -> {
				if (optionSelected.get().equals(FilesFunctions.CREATE_NEW_FILES)) {
					filesFuncitons.generateNewOutputFilesWithoutCoincidences(outFilesThatExist, progressList, inVsOut);
					updateOrders(inVsOut, progressList);
				} else {
					createNewLockedOrders(inVsOut, progressList, outFilesThatExist);
				}
			});
		}

	}

	private void updateOrders(HashMap<File, ArrayList<File>> inVsOut, ObservableList<OrderCopyPaste> progressList) {

		if (inVsOut != null && inVsOut.size() > 0) {
			for (Entry<File, ArrayList<File>> inVsOutEntry : inVsOut.entrySet()) {
				File inFile = inVsOutEntry.getKey();
				ArrayList<File> outFiles = inVsOutEntry.getValue();
				int joinResult = canJoinToPrevoiusOrder(progressList, inFile, outFiles);
				if (joinResult == -1) {
					createNewOrder(inFile, outFiles);
				} else {
					ProcessesControl.getInstance().updateOrderInGui(joinResult);
				}
			}

		}
	}

	private void createNewLockedOrders(HashMap<File, ArrayList<File>> inVsOut,
			ObservableList<OrderCopyPaste> progressList, ArrayList<File> outFilesThatExist) {
		if (inVsOut != null && inVsOut.size() > 0) {
			for (Entry<File, ArrayList<File>> inVsOutEntry : inVsOut.entrySet()) {
				File inFile = inVsOutEntry.getKey();
				ArrayList<File> outFiles = inVsOutEntry.getValue();
				if (Collections.disjoint(outFilesThatExist, outFiles)) {
					createNewOrder(inFile, outFiles);
				} else {
					createNewLockedOrder(inFile, outFiles);
				}

			}
		}
	}

	private void createNewLockedOrder(File inFile, ArrayList<File> outFiles) {
		OrderCopyPaste orderCopyPaste = new OrderCopyPaste(inFile, outFiles);
		ProcessesControl.getInstance().addOrderInGui(orderCopyPaste);
		ProcessesControl.getInstance().addOrderCopyPasteProcessLocked(orderCopyPaste);
	}

	private void createNewOrder(File inFile, ArrayList<File> outFiles) {
		OrderCopyPaste orderCopyPaste = new OrderCopyPaste(inFile, outFiles);
		ProcessesControl.getInstance().addOrderInGui(orderCopyPaste);
		ProcessesControl.getInstance().addOrderCopyPasteProcess(orderCopyPaste);
	}

	private int canJoinToPrevoiusOrder(ObservableList<OrderCopyPaste> progressList, File inFile,
			ArrayList<File> outFiles) {
		int indexUpdated = -1;
		for (int i = 0; i < progressList.size(); i++) {
			OrderCopyPaste orderInProgress = progressList.get(i);
			File inFileInProggress = orderInProgress.getInputFile();
			if (filesFuncitons.chechAbsolutePaths(inFile, inFileInProggress)
					&& !ProcessesControl.getInstance().isOrderWriting(orderInProgress)) {
				orderInProgress.getOutputFiles().addAll(outFiles);
				return i;
			}
		}
		return indexUpdated;
	}

	public void setLiving(boolean b) {
		living = b;
		try {
			userOrders.put(() -> {
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
