package copyPaste.copyPaste.processesControl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

import copyPaste.copyPaste.ProcessesControl;
import copyPaste.copyPaste.ProcessesControl.VoidConsumer;
import copyPaste.copyPaste.data.OrderCopyPaste;
import copyPaste.copyPaste.processesControl.copyPasteEx.OrderCopyPasteProcess;

public class CopyPasteExecutor implements Runnable {

	private boolean living = false;

	private HashMap<OrderCopyPasteProcess, OrderCopyPasteProcess> orderLockerVsOrderLocked = new HashMap<>();

	private SynchronousQueue<VoidConsumer> orderCopyPasteProcesses = new SynchronousQueue<>();

	private ArrayList<OrderCopyPasteProcess> runnigOrdersCopyPaste = new ArrayList<>();

	@Override
	public void run() {
		living = true;
		while (living) {
			try {
				executeBackCopyPasteProcesses();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void executeBackCopyPasteProcesses() throws InterruptedException {
		VoidConsumer orderCopyPasteProcess = orderCopyPasteProcesses.take();
		orderCopyPasteProcess.consume();
	}

	public boolean isOrderWriting(OrderCopyPaste orderInProgress) {
		synchronized (runnigOrdersCopyPaste) {
			return runnigOrdersCopyPaste.stream().anyMatch((s) -> {
				return s.getOrderCopyPaste().equals(orderInProgress) && orderInProgress.isWritting();
			});
		}
	}

	private void processOrderCopyPaste(OrderCopyPasteProcess orderCopyPasteProcess) {
		if (orderCopyPasteProcess != null) {
	
			startOrderCopyPasteThread(orderCopyPasteProcess);
		}
	}

	private void processOrderCopyPasteLocked(OrderCopyPasteProcess orderCopyPasteProcess) {
		if (orderCopyPasteProcess != null) {
			OrderCopyPasteProcess orderLocker = getOrderLocker(orderCopyPasteProcess);
			if (orderLocker == null) {
		
				startOrderCopyPasteThread(orderCopyPasteProcess);
			} else {
				orderLockerVsOrderLocked.put(orderLocker, orderCopyPasteProcess);
			}

		}
	}

	private void startOrderCopyPasteThread(OrderCopyPasteProcess orderCopyPasteProcess) {
		synchronized (runnigOrdersCopyPaste) {
			runnigOrdersCopyPaste.add(orderCopyPasteProcess);
		}
		new Thread(orderCopyPasteProcess,
				"OrderCopyPasteProcess " + orderCopyPasteProcess.getOrderCopyPaste().getInputFile().getName())
						.start();
	}

	private OrderCopyPasteProcess getOrderLocker(OrderCopyPasteProcess orderCopyPasteProcess) {
		OrderCopyPasteProcess lastOrder = null;
		synchronized (orderLockerVsOrderLocked) {
			lastOrder = mathOrder(orderCopyPasteProcess, lastOrder, orderLockerVsOrderLocked.values());
		}
		synchronized (runnigOrdersCopyPaste) {
			if (lastOrder == null) {
				lastOrder = mathOrder(orderCopyPasteProcess, lastOrder, runnigOrdersCopyPaste);
			}
		}
		return lastOrder;
	}

	private OrderCopyPasteProcess mathOrder(OrderCopyPasteProcess orderCopyPasteProcess, OrderCopyPasteProcess lastOrder,
			Collection<OrderCopyPasteProcess> orderProcesses) {
		for (OrderCopyPasteProcess orderProcess : orderProcesses) {
			OrderCopyPaste order = orderProcess.getOrderCopyPaste();
			for (File outFileLock : order.getOutputFiles()) {
				ArrayList<File> orderPorcessedFiles=orderCopyPasteProcess.getOrderCopyPaste().getOutputFiles();
				for (File outFile : orderPorcessedFiles) {
					if (outFile.getAbsolutePath().equals(outFileLock.getAbsolutePath())) {
						lastOrder = orderProcess;
					}
				}
			}
		}
		return lastOrder;
	}

	public void addOrderCopyPasteProcess(OrderCopyPaste orderCopyPaste) {
		try {
			orderCopyPasteProcesses.put(() -> {
				processOrderCopyPaste(new OrderCopyPasteProcess(this, orderCopyPaste));
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void addOrderCopyPasteProcessLocked(OrderCopyPaste orderCopyPaste) {
		try {
			orderCopyPasteProcesses.put(() -> {
				processOrderCopyPasteLocked(new OrderCopyPasteProcess(this, orderCopyPaste));
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void removeOrder(OrderCopyPasteProcess orderCopyPasteProcess) {
		synchronized (runnigOrdersCopyPaste) {
			runnigOrdersCopyPaste.remove(orderCopyPasteProcess);
		}
		ProcessesControl.getInstance().removeOrderFromGui(orderCopyPasteProcess.getOrderCopyPaste());
		unlockOverridingOrders(orderCopyPasteProcess);
	}

	private void unlockOverridingOrders(OrderCopyPasteProcess orderCopyPasteProcess) {
		OrderCopyPasteProcess orderLocked= orderLockerVsOrderLocked.remove(orderCopyPasteProcess);
		if(orderLocked!=null) {
			startOrderCopyPasteThread(orderLocked);
		}
	}

	public void setLiving(boolean b) {
		living = b;
		try {
			orderCopyPasteProcesses.put(() -> {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
