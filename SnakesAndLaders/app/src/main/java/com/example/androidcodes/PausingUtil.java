package com.example.androidcodes;

public class PausingUtil extends Thread {
	final PausingListener listener;
	private long sleepTime;

	public interface PausingListener {
		void notifyPausingCompleted();
	}

	public PausingUtil(long sleepTime, PausingListener listener) {
		this.listener = listener;
		this.sleepTime = sleepTime;
	}

	public void run() {
		try {
			Thread.sleep(this.sleepTime);
		} catch (InterruptedException e) {
		}
		if (listener != null) {
			listener.notifyPausingCompleted();
		}
	}
}
