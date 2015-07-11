package com.example.bluetoothcheckers;

import java.io.IOException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
/*
 * A list of discovered devices which the user can attempt to
 * connect to. This window is shown when the user clicks Join Game
 * from the MainActivity.
 */
public class JoinView extends ListActivity{
	public static String[] joinables;
	public static MainActivity mainActivity;
	private Thread hostThread;
	private int position = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, joinables);
		setListAdapter(adapter);

	}

	/*
	 * As with the Host Game button we will fire up a Progress Dialog
	 * which makes the connection process cancelable.
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		this.position = position;
		ProgressDialog mDialog = new ProgressDialog(this);
		mDialog.setMessage("Setting up bluetooth connection...");
		mDialog.setCancelable(false);
		mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					hostThread.join();
				} catch (InterruptedException e) {}
			}
		});
		mDialog.show();
		hostThread = new Thread(){
			@Override
			public void run() {
				if(mainActivity.devices.get(JoinView.this.position) != null)
					mainActivity.mmDevice = mainActivity.devices.get(JoinView.this.position);
				try {
					mainActivity.mmSocket = mainActivity.mmDevice.createRfcommSocketToServiceRecord(MainActivity.MY_UUID);
				} catch (IOException e) {}
				try {
					mainActivity.mmSocket.connect();
					TictactoeActivity.socket = mainActivity.mmSocket;
					TictactoeActivity.isServer = false;
					startActivity(new Intent(JoinView.this, TictactoeActivity.class));
				} catch (IOException connectException) {
					try {
						mainActivity.mmSocket.close();
					} catch (IOException closeException) {}
					return;
				}
			}
		};
		hostThread.start();
	}
}
