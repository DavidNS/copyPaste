package copyPaste.copyPaste.data;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrderCopyPaste {

	private AtomicBoolean cancelled=new AtomicBoolean(false);
	
	private boolean reading=false;
	
	private boolean writting=false;
	
	private boolean readed=false;
	
	private boolean writted=false;
	
	private File inputFile;
	
	private ArrayList<File> outputFiles;

	public OrderCopyPaste(File inputFile, ArrayList<File> outputFiles) {
		super();
		this.inputFile = inputFile;
		this.outputFiles = outputFiles;
	}

	public File getInputFile() {
		return inputFile;
	}

	public ArrayList<File> getOutputFiles() {
		return outputFiles;
	}

	public boolean isReading() {
		return reading;
	}

	public void setReading(boolean reading) {
		this.reading = reading;
	}

	public boolean isWritting() {
		return writting;
	}

	public void setWritting(boolean writing) {
		this.writting = writing;
	}

	public AtomicBoolean getCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled.set(cancelled);
	}

	public boolean isReaded() {
		return readed;
	}

	public void setReaded(boolean readed) {
		this.readed = readed;
	}

	public boolean isWritted() {
		return writted;
	}

	public void setWritted(boolean writted) {
		this.writted = writted;
	}

	
}
