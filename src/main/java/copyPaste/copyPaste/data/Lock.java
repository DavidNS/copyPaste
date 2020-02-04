package copyPaste.copyPaste.data;

import java.util.concurrent.atomic.AtomicBoolean;

public class Lock {

	private AtomicBoolean locked = new AtomicBoolean(true);

	public void waitLock() {
		synchronized (locked) {
			try {
				if (locked.get()) {
					locked.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setLocked(boolean newValue) {
		synchronized (locked) {
			locked.set(newValue);
		}
	}

	public void notifyLock() {
		synchronized (locked) {
			locked.notify();
		}
	}
		
	
	
}
