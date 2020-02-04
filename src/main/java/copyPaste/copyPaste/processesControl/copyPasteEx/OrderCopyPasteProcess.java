package copyPaste.copyPaste.processesControl.copyPasteEx;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import copyPaste.copyPaste.ProcessesControl;
import copyPaste.copyPaste.data.Lock;
import copyPaste.copyPaste.data.OrderCopyPaste;
import copyPaste.copyPaste.processesControl.CopyPasteExecutor;
import copyPaste.copyPaste.processesControl.copyPasteEx.copyPasteProcess.ReadingProcess;
import copyPaste.copyPaste.processesControl.copyPasteEx.copyPasteProcess.WritingProcess;
import copyPaste.copyPaste.processesControl.userRequestEx.FilesFunctions;

public class OrderCopyPasteProcess implements Runnable {

	private OrderCopyPaste orderCopyPate;

	private CopyPasteExecutor copyPasteExecutor;

	private ReadingProcess readingProcess;

	private WritingProcess writingProcess;

	private ArrayList<byte[]> allBytes = new ArrayList<>();

	private Lock lock = new Lock();

	public OrderCopyPasteProcess(CopyPasteExecutor copyPasteExecutor, OrderCopyPaste orderCopyPate) {
		this.orderCopyPate = orderCopyPate;
		this.copyPasteExecutor = copyPasteExecutor;
	}

	public OrderCopyPaste getOrderCopyPaste() {
		return orderCopyPate;
	}

	@Override
	public void run() {

		this.readingProcess = new ReadingProcess(this, orderCopyPate.getInputFile());
		new Thread(readingProcess, "Reading_" + orderCopyPate.getInputFile().getName()).start();
		lock.waitLock();

		if (!orderCopyPate.getCancelled().get()) {
			lock.setLocked(true);
			this.writingProcess = new WritingProcess(this, orderCopyPate.getOutputFiles());
			new Thread(writingProcess, "Writing_" + orderCopyPate.getInputFile().getName()).start();
			lock.waitLock();
		}

		copyPasteExecutor.removeOrder(this);
	}

	public ArrayList<byte[]> getAllBytes() {
		return allBytes;
	}

	public void closeStreams(AutoCloseable... autoCloseables) {
		for (AutoCloseable autoCloseable : autoCloseables) {
			try {
				autoCloseable.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public void addBytes(byte[] buffer, int length) {
		allBytes.add( Arrays.copyOfRange(buffer, 0, length));

	}

	public void setLocked(boolean newValue) {
		lock.setLocked(newValue);
	}

	public void notifyLock() {
		lock.notifyLock();
	}

	public void processErrorWriting(StringBuilder output, File failedFile, FileOutputStream failedOS,
			HashMap<FileOutputStream, File> streamVsOutputFile) {
		switchWriting(output, failedFile);
		closeStreams(streamVsOutputFile.keySet().stream().toArray(OutputStream[]::new));
		streamVsOutputFile.clear();
	}

	public void processErrorReading(StringBuilder output, Closeable stream, File file) {
		swithReading(output);
		closeStreams(stream);
	}

	private void swithReading(StringBuilder output) {
		switch (output.toString()) {
		case FilesFunctions.CANCEL:
		case FilesFunctions.STOP:
			orderCopyPate.getCancelled().set(true);
			ProcessesControl.getInstance().updateOrderOnGui(orderCopyPate);
			break;
		case FilesFunctions.RETRY:
			break;
		default:
			break;
		}
	}

	private void switchWriting(StringBuilder output, File failedFile) {
		switch (output.toString()) {
		case FilesFunctions.CANCEL:
		case FilesFunctions.STOP:
			orderCopyPate.getCancelled().set(true);
			ProcessesControl.getInstance().updateOrderOnGui(orderCopyPate);
			break;
		case FilesFunctions.DELETE_CONFLICT:
			orderCopyPate.getOutputFiles().remove(failedFile);
			ProcessesControl.getInstance().updateOrderOnGui(orderCopyPate);
			break;
		case FilesFunctions.RETRY:
			break;
		default:
			break;
		}

	}

}