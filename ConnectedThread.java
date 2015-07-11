package com.example.bluetoothcheckers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
/*
 * The thread that takes care of our connection to the
 * other player.
 */
public class ConnectedThread extends Thread {
	public boolean running = true;
	public DrawView dw;
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;

	public ConnectedThread(BluetoothSocket socket) {
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) { }
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	public void run() {
		int bytes;
		while (running) {
			try {
				bytes = mmInStream.read();
				dw.handleMessage(bytes, false);
			} catch (IOException e) {
				break;
			}
		}
	}
	
	public void write(int bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) { }
	}

	public void cancel() {
		try {
			running = false;
			mmSocket.close();
		} catch (IOException e) { }
	}
}
