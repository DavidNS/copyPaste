package copyPaste.copyPaste.processesControl.copyPasteEx.copyPasteProcess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import copyPaste.copyPaste.ProcessesControl;
import copyPaste.copyPaste.data.Lock;
import copyPaste.copyPaste.processesControl.copyPasteEx.OrderCopyPasteProcess;
import copyPaste.copyPaste.processesControl.userRequestEx.FilesFunctions;

public class WritingProcess implements Runnable {

	private HashMap<FileOutputStream, File> streamVsOutputFile = new HashMap<>();

	private OrderCopyPasteProcess orderCopyPasteProcess;

	private ArrayList<File> outputFiles;

	public WritingProcess(OrderCopyPasteProcess orderCopyPasteProcess, ArrayList<File> outputFiles) {
		this.orderCopyPasteProcess = orderCopyPasteProcess;
		this.outputFiles = outputFiles;
	}

	@Override
	public void run() {
		orderCopyPasteProcess.getOrderCopyPaste().setWritting(true);
		writeFile();

		ProcessesControl.getInstance().updateOrderOnGui(orderCopyPasteProcess.getOrderCopyPaste());
		orderCopyPasteProcess.notifyLock();
		orderCopyPasteProcess.setLocked(false);
	}

	private void writeFile() {

		FileOutputStream failedOS = null;
		File failedFile = null;
		StringBuilder output = new StringBuilder();
		while (!orderCopyPasteProcess.getOrderCopyPaste().getCancelled().get() && !orderCopyPasteProcess.getOrderCopyPaste().isWritted()) {
			try {
				createOutputStreams(outputFiles, failedFile, failedOS);
				for (int i = 0; i <orderCopyPasteProcess.getAllBytes().size() && !orderCopyPasteProcess.getOrderCopyPaste().getCancelled().get(); i++) {
					byte[] bytes=orderCopyPasteProcess.getAllBytes().get(i);
					writeBufferInOss(streamVsOutputFile.keySet(), bytes, bytes.length, failedOS);
				}
				orderCopyPasteProcess.getOrderCopyPaste().setWritted(true);
			} catch (IOException | SecurityException e) {
				processControledException(failedOS, failedFile, output);
			} catch (Exception e) {
				processUnexpectedException(output);
			} finally {
				orderCopyPasteProcess.closeStreams(streamVsOutputFile.keySet().stream().toArray(OutputStream[]::new));
			}

		}

	}

	private void processUnexpectedException(StringBuilder output) {
		ProcessesControl.getInstance().showChooseContinueOption(new Lock(), FilesFunctions.CONTINUE_OPTIONS_UNEXPECTED,
				output, "Unexpected error");
		orderCopyPasteProcess.processErrorWriting(output, null, null, streamVsOutputFile);

	}

	private void processControledException(FileOutputStream failedOS, File failedFile, StringBuilder output) {
		ProcessesControl.getInstance().showChooseContinueOption(new Lock(), FilesFunctions.CONTINUE_OPTIONS_CONFLICT,
				output, "Output file with a trouble " + tryGetOutpufFileName(failedOS));
		orderCopyPasteProcess.processErrorWriting(output, failedFile, failedOS, streamVsOutputFile);
	}

	private String tryGetOutpufFileName(FileOutputStream failedOS) {
		String name = "";
		if (failedOS != null && streamVsOutputFile.containsKey(failedOS)) {
			name = streamVsOutputFile.get(failedOS).getName();
		}
		return name;
	}

	private HashMap<File, FileOutputStream> createOutputStreams(ArrayList<File> dest, File failedFile,
			FileOutputStream failedOS) throws IOException, SecurityException {
		HashMap<File, FileOutputStream> oss = new HashMap<File, FileOutputStream>();
		for (File d : dest) {
			failedOS = new FileOutputStream(d);
			failedFile = d;
			streamVsOutputFile.put(failedOS, failedFile);
		}
		return oss;
	}

	private void writeBufferInOss(Collection<FileOutputStream> collection, byte[] buffer, int length,
			OutputStream failedStream) throws IOException, SecurityException {
		Iterator<FileOutputStream> it= collection.iterator();
		while (it.hasNext() && !orderCopyPasteProcess.getOrderCopyPaste().getCancelled().get()) {
			failedStream=it.next();
			failedStream.write(buffer, 0, length);
			
		}

	}
}
