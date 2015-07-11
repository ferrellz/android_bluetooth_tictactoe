package com.example.bluetoothcheckers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
/*
 * The activity for the actual game, which is started by a Host and Client
 * connection(MainActivity->onHost and JoinView->onListItemClick).
 * From here we setup our DrawView which will contain most of our
 * game logic and handle our rendering(Which if this was a larger game
 * would preferably be seperated)
 * Nothing fancy except maybe the Handler which will switch back to our
 * MainActivity when the game is over.
 */
public class TictactoeActivity extends Activity{
	public static BluetoothSocket socket = null;
	public static boolean isServer = false;
	private DrawView dw;
	private ConnectedThread connection;

	//No long delayed messages stacked up, so no need to worry about serious memory leakage
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.obj.toString().equals("draw")){
				Toast.makeText(getApplicationContext(),
						"Draw!", Toast.LENGTH_SHORT).show();
				postDelayed(closeActivity, 3000);
			} else {
				Toast.makeText(getApplicationContext(),
						msg.obj.toString() + " won!", Toast.LENGTH_SHORT).show();
				postDelayed(closeActivity, 3000);
			}
		}
	};

	private Runnable closeActivity = new Runnable() {
		public void run() {
			startActivity(new Intent(TictactoeActivity.this, MainActivity.class));
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		connection = new ConnectedThread(socket);
		dw = new DrawView(this, connection, isServer, mHandler);
		connection.dw = dw;
		connection.start();
		setContentView(dw);
	}

	protected void onDestroy(){
		super.onDestroy();
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable(); 
		} 
	}
}

