package com.example.bluetoothcheckers;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
/*
 * Our game loop thread. We setup a touchListener here which will send messages to our DrawView
 * instead of using performClick because I wanted to try this approach. We can assign the
 * FPS rate the game should update at with the variable FPS, 10 should be more than
 * plenty for a simple game like this.
 */
public class GameThread extends Thread{
	private static final long FPS = 10;
	public Rect[][] tiles = new Rect[4][4];
	public int[][] lowerTiles = new int[4][4];
	private DrawView view;
	private boolean running, sleepToSync = true;

	public GameThread(DrawView v){
		view = v;
		view.setOnTouchListener(new OnTouchListener()  
		{  
			public boolean onTouch(View v, MotionEvent event)   
			{  
				if(view.myTurn){
					for(int i = 0; i < 4; i++)
						for(int i2 = 0; i2 < 4; i2++){
							if(tiles[i][i2].contains((int)event.getX(), (int)event.getY()) && lowerTiles[i][i2] == 0){
								if(view.isServer)
									lowerTiles[i][i2] = 1;
								else 
									lowerTiles[i][i2] = 2;
								int squareId = (i*4)+(i2);
								view.connection.write(squareId);
								view.handleMessage(squareId, true);
								view.myTurn = false;
							}
						}
				}
				return true;  
			}  
		});  
	}  

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		long ticksPS = 1000 / FPS;
		long startTime;
		long sleepTime;
		while (running) {
			if(sleepToSync){
				try {
					sleep(2000);
				} catch (InterruptedException e) { e.printStackTrace();}
				sleepToSync = false;
			}
			startTime = System.currentTimeMillis();
			Canvas c = null;
			try {
				c = view.getHolder().lockCanvas();
				synchronized (view.getHolder()) {
					view.postInvalidate();
				}
			} finally {
				if (c != null) {
					view.getHolder().unlockCanvasAndPost(c);
				}
			}
			sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
			try {
				if (sleepTime > 0)
					sleep(sleepTime);
				else
					sleep(10);
			} catch (Exception e) {}
		}
	}
}
