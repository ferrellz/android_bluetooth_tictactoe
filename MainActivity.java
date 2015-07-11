package com.example.bluetoothcheckers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import com.example.bluetoothcheckers.R;
/*
 * The main window for the game, allow user to host a game, join a game,
 * or discover new discoverable bluetooth devices. Throughout the project
 * I have avoided using private variables which requires getter/setter
 * methods simply because they add so much unneccessary code for a small 
 * project that very much likely won't be expanded in the future.
 */
public class MainActivity extends ActionBarActivity {
	public static final int REQUEST_ENABLE_BT = 1;
	public static final UUID MY_UUID = new UUID(123127837,1923812893);
	public BluetoothDevice mmDevice = null;
	public BluetoothSocket mmSocket = null;
	public ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();	
	private BluetoothServerSocket mmServerSocket = null;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket socket = null;
	private Thread hostThread;
	private boolean hostRunning = true;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				devices.add(device);
				Toast.makeText(getApplicationContext(),
						"Found: "+device.getName(), Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Process.killProcess(Process.myPid());
		}
		discoverPrompt();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

	}

	public void discoverPrompt(){
		Intent discoverableIntent = new
				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
		startActivityForResult(discoverableIntent, 1);
	}

	/*
	 * Called when the Host Game button is pressed.
	 * Cancel any discovery process and start a new Progress Dialog while
	 * firing up a thread that blocks at mmServerSocket.accept until a
	 * client connects to us. Allow the user to cancel the host process
	 * from the Progress Dialog.
	 */
	public void onHost(View view){
		mBluetoothAdapter.cancelDiscovery();
		ProgressDialog mDialog = new ProgressDialog(this);
		mDialog.setMessage("Waiting for client to connect...");
		mDialog.setCancelable(false);
		mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				hostRunning = false;
				try {
					mmServerSocket.close();
				} catch (IOException e){}
				try {
					hostThread.join();
				} catch (InterruptedException e) {}
				Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			}
		});
		mDialog.show();
		hostRunning = true;
		hostThread = new Thread(){
			@Override
			public void run() {
				while(hostRunning){	
					try {
						mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("PongPong", MY_UUID);
					} catch (IOException e) { }
					try {
						socket = mmServerSocket.accept();
						TictactoeActivity.socket = socket;
						TictactoeActivity.isServer = true;
						startActivity(new Intent(MainActivity.this, TictactoeActivity.class));
						hostRunning = false;
						mmServerSocket.close();
						join();
					} catch (IOException e) {} catch (InterruptedException e) {}
				}
			}
		};
		hostThread.start();
	}

	public void onDiscover(View view){
		devices.clear();
		discoverPrompt();
	}

	protected void onDestroy(){
		super.onDestroy();
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable(); 
		} 
	}

	public void onJoin(View view){
		mBluetoothAdapter.cancelDiscovery();
		JoinView.joinables = new String[devices.size()];
		int count = 0;
		for(BluetoothDevice d: devices){
			JoinView.joinables[count++] = d.getName();
		}
		JoinView.mainActivity = this;
		startActivity(new Intent(this, JoinView.class));
	}

	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case 1://public static final int REQUEST_ENABLE_BT = 1;
			mBluetoothAdapter.startDiscovery();
			break;
		}
	}
}
