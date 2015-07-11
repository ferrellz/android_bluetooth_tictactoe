package com.example.bluetoothcheckers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/*
 * The class that contains most of our game. Here we handle rendering and several
 * game logic operations.
 */
public class DrawView extends SurfaceView implements SurfaceHolder.Callback{
	public GameThread thread;
	public Context con;
	public ConnectedThread connection;
	public boolean isServer, myTurn, initialized = false;
	private float tokenRadius = 0;
	private Handler activityHandler;
	private Paint paint = new Paint();
	
	//Coordinates which will be used for several things in our game, assigned correctly in onSizeChanged
	private int outerLeftPoint = 0, outerRightPoint = 0, outerTopPoint = 0, outerBottomPoint = 0,
			innerLeftPoint = 0, innerRightPointX =0, innerTopPoint = 0, innerBottomPoint = 0,
			centerPoint = 0;

	public DrawView(Context context, ConnectedThread connec, boolean isServ, Handler handler) {
		super(context);
		isServer = isServ;
		con = context;
		connection = connec;
		activityHandler = handler;
		if(isServ)
			myTurn = true;
		else myTurn = false;
		getHolder().addCallback(this);
		thread = new GameThread(this);
		setFocusable(true);
	}
	
	/*
	 * Populate our two tile arrays with Rectangles and information
	 * about what token is on top of the tile(where 0 means empty,
	 * 1 means server and 2 means client).
	 */
	private void initTiles(){
		for(int i = 0; i < 4; i++){
			for(int i2 = 0; i2 < 4; i2++){
				int left = 0, top = 0, right = 0, bottom = 0;
				switch(i){
				case 0: left = outerLeftPoint; right = innerLeftPoint; break;
				case 1: left = innerLeftPoint; right = centerPoint; break;
				case 2: left = centerPoint; right = innerRightPointX; break;
				case 3: left = innerRightPointX; right = outerRightPoint; break;
				}
				switch(i2){
				case 0: top = outerTopPoint; bottom = innerTopPoint; break;
				case 1: top = innerTopPoint; bottom = centerPoint; break;
				case 2: top = centerPoint; bottom = innerBottomPoint; break;
				case 3: top = innerBottomPoint; bottom = outerBottomPoint; break;
				}
				thread.tiles[i][i2] = new Rect(left, top, right, bottom);
				thread.lowerTiles[i][i2] = 0;
			}
		}
	}
	
	//Assign our coordinates by percentage relative to our screen width
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		outerLeftPoint = w/6; outerRightPoint = w-w/6; outerTopPoint = w/6; outerBottomPoint = w-w/6;
		innerLeftPoint = w/3; innerRightPointX = w-w/3; innerTopPoint = (w/3); innerBottomPoint = w-w/3;
		centerPoint = w/2;
		tokenRadius = w/18;
		initTiles();
	}
	
	/*
	 * Make a check too see if the game is over. If it is we end
	 * everything that needs to be ended and tell our TictactoeActivity
	 * to display a toast about the game result and after three seconds
	 * switch us back to our MainActivity.
	 */
	public void checkVictory(int i, int i2){
		boolean clientWon = false, serverWon = false;
		//Check straight lines
		if(thread.lowerTiles[i][0] == 1 && thread.lowerTiles[i][1] == 1 && thread.lowerTiles[i][2] == 1 && thread.lowerTiles[i][3] == 1)
			serverWon = true;
		if(thread.lowerTiles[0][i2] == 1 && thread.lowerTiles[1][i2] == 1 && thread.lowerTiles[2][i2] == 1 && thread.lowerTiles[3][i2] == 1)
			serverWon = true;
		if(thread.lowerTiles[i][0] == 2 && thread.lowerTiles[i][1] == 2 && thread.lowerTiles[i][2] == 2 && thread.lowerTiles[i][3] == 2)
			clientWon = true;
		if(thread.lowerTiles[0][i2] == 2 && thread.lowerTiles[1][i2] == 2 && thread.lowerTiles[2][i2] == 2 && thread.lowerTiles[3][i2] == 2)
			clientWon = true;
		//Check diagonal lines
		if(thread.lowerTiles[0][0] == 1 && thread.lowerTiles[1][1] == 1 && thread.lowerTiles[2][2] == 1 && thread.lowerTiles[3][3] == 1)
			serverWon = true;
		if(thread.lowerTiles[3][0] == 1 && thread.lowerTiles[2][1] == 1 && thread.lowerTiles[1][2] == 1 && thread.lowerTiles[0][3] == 1)
			serverWon = true;
		if(thread.lowerTiles[0][0] == 2 && thread.lowerTiles[1][1] == 2 && thread.lowerTiles[2][2] == 2 && thread.lowerTiles[3][3] == 2)
			clientWon = true;
		if(thread.lowerTiles[3][0] == 2 && thread.lowerTiles[2][1] == 2 && thread.lowerTiles[1][2] == 2 && thread.lowerTiles[0][3] == 2)
			clientWon = true;

		if(serverWon || clientWon){
			postInvalidate();
			thread.setRunning(false);
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			connection.cancel();
			Message msg = new Message();
			String textToChange = "Server";
			if (clientWon) textToChange = "Client";
			msg.obj = textToChange;
			activityHandler.sendMessage(msg);
		}
		boolean draw = true;
		for(int x = 0; x < 4; x++)
			for (int x1 = 0; x1 < 4; x1++)
				if(thread.lowerTiles[x][x1] == 0)
					draw = false;
		if(!serverWon && !clientWon && draw){
			postInvalidate();
			thread.setRunning(false);
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			connection.cancel();
			Message msg = new Message();
			String textToChange = "draw";
			msg.obj = textToChange;
			activityHandler.sendMessage(msg);
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		setBackgroundColor(Color.WHITE);
		paint.setColor(Color.BLACK);
		canvas.drawLine(outerLeftPoint, outerTopPoint, outerRightPoint, outerTopPoint, paint);
		canvas.drawLine(outerLeftPoint, outerBottomPoint, outerRightPoint, outerBottomPoint, paint);
		canvas.drawLine(outerLeftPoint, outerTopPoint, outerLeftPoint, outerBottomPoint, paint);
		canvas.drawLine(outerRightPoint, outerTopPoint, outerRightPoint, outerBottomPoint, paint);
		canvas.drawLine(innerLeftPoint, outerTopPoint, innerLeftPoint, outerBottomPoint, paint);
		canvas.drawLine(innerRightPointX, outerTopPoint, innerRightPointX, outerBottomPoint, paint);
		canvas.drawLine(outerLeftPoint, innerTopPoint, outerRightPoint, innerTopPoint, paint);
		canvas.drawLine(outerLeftPoint, innerBottomPoint, outerRightPoint, innerBottomPoint, paint);
		canvas.drawLine(outerLeftPoint, centerPoint, outerRightPoint, centerPoint, paint);
		canvas.drawLine(centerPoint, outerTopPoint, centerPoint, outerBottomPoint, paint);
		for(int i = 0; i < 4; i++)
			for(int i2 = 0; i2 < 4; i2++){
				if(thread.lowerTiles[i][i2] == 1){
					paint.setColor(Color.RED);
					canvas.drawCircle(getTilePosX(i), getTilePosY(i2), tokenRadius, paint);
				} else if(thread.lowerTiles[i][i2] == 2){
					paint.setColor(Color.BLACK);
					canvas.drawCircle(getTilePosX(i), getTilePosY(i2), tokenRadius, paint);
				}
			}
		if(!initialized)
			initialized = true;
	}

	private float getTilePosX(int i){
		switch(i){
		default:
		case 0: return (float)(outerLeftPoint+(getWidth()/12));
		case 1: return (float)(innerLeftPoint+(getWidth()/12));
		case 2: return (float)(centerPoint+(getWidth()/12));
		case 3: return (float)(innerRightPointX+(getWidth()/12));
		}
	}

	private float getTilePosY(int i){
		switch(i){
		default:
		case 0: return (float)(outerTopPoint+(getWidth()/12));
		case 1: return (float)(innerTopPoint+(getWidth()/12));
		case 2: return (float)(centerPoint+(getWidth()/12));
		case 3: return (float)(innerBottomPoint+(getWidth()/12));
		}
	}
	
	/*
	 * Our method that takes care of someone placing a token on the field.
	 * Will be called either from our touchListener or ConnectedThread upon
	 * receiving a message from the other player.
	 */
	public void handleMessage(int bytes, boolean sentToSelf){
		int i = 0, i2 = 0, player = 0;
		if(sentToSelf){
			if(isServer)
				player=1;
			else player=2;
		} else {
			if(isServer)
				player=2;
			else player=1;
			myTurn = true;
		}
		switch(bytes){
		case 0: i = 0; i2 = 0; break;
		case 1: i = 0; i2 = 1; break;
		case 2: i = 0; i2 = 2; break;
		case 3: i = 0; i2 = 3; break;
		case 4: i = 1; i2 = 0; break;
		case 5:  i = 1; i2 = 1; break;
		case 6:  i = 1; i2 = 2; break;
		case 7:  i = 1; i2 = 3; break;
		case 8:  i = 2; i2 = 0; break;
		case 9:  i = 2; i2 = 1; break;
		case 10: i = 2; i2 = 2; break;
		case 11: i = 2; i2 = 3; break;
		case 12: i = 3; i2 = 0; break;
		case 13: i = 3; i2 = 1; break;
		case 14: i = 3; i2 = 2; break;
		case 15:  i = 3; i2 = 3; break;
		}
		thread.lowerTiles[i][i2] = player; 
		checkVictory(i,i2); 
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		thread.setRunning(true);
		thread.start();
		setWillNotDraw(false);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {}
		}
	}

}
