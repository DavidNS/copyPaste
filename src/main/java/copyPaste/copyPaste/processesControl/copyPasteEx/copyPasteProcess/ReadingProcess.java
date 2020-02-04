package copyPaste.copyPaste.processesControl.copyPasteEx.copyPasteProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import copyPaste.copyPaste.ProcessesControl;
import copyPaste.copyPaste.data.Lock;
import copyPaste.copyPaste.processesControl.copyPasteEx.OrderCopyPasteProcess;
import copyPaste.copyPaste.processesControl.userRequestEx.FilesFunctions;

public class ReadingProcess implements Runnable {

	public static final int BUFFER_SIZE = 1024;

	private File inputFile;

	private FileInputStream is;

	private OrderCopyPasteProcess orderCopyPasteProcess;

	
	public ReadingProcess(OrderCopyPasteProcess orderCopyPasteProcess, File inputFile) {
		this.orderCopyPasteProcess = orderCopyPasteProcess;
		this.inputFile = inputFile;
	}

	@Override
	public void run() {
		orderCopyPasteProcess.getOrderCopyPaste().setReading(true);
		readingProcess();
		ProcessesControl.getInstance().updateOrderOnGui(orderCopyPasteProcess.getOrderCopyPaste());
		orderCopyPasteProcess.setLocked(false);
		orderCopyPasteProcess.notifyLock();
	}

	private void readingProcess() {
		StringBuilder output=new StringBuilder();
		while (!orderCopyPasteProcess.getOrderCopyPaste().getCancelled().get() && !orderCopyPasteProcess.getOrderCopyPaste().isReaded()) {
			try {
				is = new FileInputStream(inputFile);
				read();
				orderCopyPasteProcess.getOrderCopyPaste().setReaded(true);
			}  catch (Exception e) {
				ProcessesControl.getInstance().showChooseContinueOption(new Lock(), FilesFunctions.CONTINUE_OPTIONS_CONFLICT,output, "Input file with a trouble "+inputFile.getName());
				orderCopyPasteProcess.processErrorReading(output,is,inputFile);
			} finally {
				orderCopyPasteProcess.closeStreams(is);
			}
		}
	}

	private void read() throws IOException, SecurityException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int length=is.read(buffer);
		while (!orderCopyPasteProcess.getOrderCopyPaste().getCancelled().get() && length > 0) {
			orderCopyPasteProcess.addBytes(buffer, length);
			length = is.read(buffer);
		}
	}
}
