package com.example.androidcodes;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.SensorListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class SnakesLaddersView extends SurfaceView implements SensorListener, Callback {
	
	private Context mContext;

	private TextView mStatusText;

	private SnakesLaddersThread thread;

	class SnakesLaddersThread extends Thread {
	
		private final Random RNG = new Random();

		private String displayText; 
		private String player1Name = "", player2Name = "";
		
		private final Integer[] DICE_IMAGES = new Integer[] {Integer.valueOf(R.drawable.dice1),
				Integer.valueOf(R.drawable.dice2), Integer.valueOf(R.drawable.dice3), 
				Integer.valueOf(R.drawable.dice4), Integer.valueOf(R.drawable.dice5),
				Integer.valueOf(R.drawable.dice6) };

		private final int[][] LADDERS = new int[][] { new int[] { 3, 24 }, new int[] { 27, 45 }, 
				new int[] { 32, 69 }, new int[] { 63, 97 } };
		
		private final int[][] SNAKES = new int[][] { new int[] { 36, 11 }, new int[] { 47, 29 }, 
				new int[] { 56, 40 }, new int[] { 85, 71 }, new int[] { 93, 88 }, new int[] { 99, 58 } };

		private int animDiceCounter = 0;
		private int boardHeight ,boardWidth, boardScreenPercent, boxWidth, boxHeight, bottom, coinRadius,
					diceValue, diceWidthHeight, freeSpaceHeight, right, whoseTurn, BORAD_MAX_NUMBER;
		
		private float dicePosX, dicePosY;
		private float deltaX = 0.0f,  deltaY = 0.0f, initialX = 0.0f, initialY = 0.0f;
		
		private double m_totalForcePrev = 0.0d;
		
		private boolean isDiceReady;
		private boolean isResume = false, mRun = false, singlePlayer = false;
		
		private Bitmap boardBitmap = null, currentDiceBitmap = null, greenCoin = null, redCoin = null;
	
		private Bitmap[] diceAnimBitmaps, diceBitmaps;
		
		private CoinHandler currentCoin;
		
		private Paint textPaint = new Paint();
		private Paint mPaint = new Paint();
		private RefreshHandler mRedrawHandler = new RefreshHandler();
		
		private DiceHandler dice;

		private SurfaceHolder mSurfaceHolder;
		
		private CoinHandler player1Coin, player2Coin;
		
		private Toast toast;	
		
		class CoinHandler {

			private final long COIN_MOVING_DELAY;

			private final int PIXEL_INCREMENT;

			private int animXPixel, animYPixel, animateTo, animationDirection, coinDirection, currentPos,
						diagonalDriver, diagonalIncrement, myColor, subTaskDirection, subTo, targetXPixel, 
						targetYPixel, x, y;
			
			private boolean animationInProgress = false;
			
			CoinHandler(int color, int position) {
				
				PIXEL_INCREMENT = Integer.parseInt(getResources().getString(R.string.pixelIncrement));
			
				COIN_MOVING_DELAY = Long.parseLong(getResources().getString(R.string.coinMovingDelay));
				
				myColor = color;
				
				currentPos = position;

			}

			public int getCurrentPosition() {
				
				return this.currentPos;
				
			}

			private int getX(int position) {
				
				int i = 10;
				
				if (position == 0) {
				
					return 0;
					
				}
				
				if (getY(position) % 2 != 0) {

					if (position % 10 != 0) {
						
						i = position % 10;
						
					}
				
					return (10 - i) + 1;
				} else if (position % 10 != 0) {
					
					return position % 10;
					
				} else {
					
					return 10;
					
				}
			}

			private int getY(int position) {
				
				return 10 - ((position - 1) / 10);
				
			}

			private int getXPixel(int x) {
				
				if (myColor == -65536) {
					
					return (boxWidth * x) - ((boxWidth * 3) / 4);
				}
				
				return (boxWidth * x) - ((boxWidth * 3) / 4);
			}

			private int getYPixel(int y) {
				
				if (myColor == -65536) {
					
					return (boxHeight * y) - ((boxHeight * 3) / 4);
				}
				
				return (boxHeight * y) - (boxHeight / 3);
			}

			public void draw(Canvas canvas) {
				
				mPaint.setColor(this.myColor);
				
				if (animationInProgress) {
					
					canvas.drawBitmap(myColor == -65536 ? redCoin : greenCoin, (float) animXPixel,
							(float) animYPixel, mPaint);
					
					return;

				}
				
				x = getX(currentPos);
				y = getY(currentPos);
				
				canvas.drawBitmap(myColor == -65536 ? redCoin : greenCoin, (float) getXPixel(x),
						(float) getYPixel(y), mPaint);
			}

			public void forward(int forwardVal, boolean isDiagonal) {
				
				if (currentPos + forwardVal > BORAD_MAX_NUMBER) {
					
					notifyCoinForwarded();
					
				} else if (isDiagonal) {
					
					startDiagonalAnimation(currentPos, currentPos + forwardVal);
					
				} else {
					
					startAnimation(currentPos, currentPos + forwardVal);
					
				}
			}

			private void startDiagonalAnimation(int animateFrom, int animateTo) {
				
				this.animateTo = animateTo;
				
				x = getX(animateFrom);
				y = getY(animateFrom);
				
				animXPixel = getXPixel(x);
				animYPixel = getYPixel(y);
				
				animationInProgress = true;
				animationDirection = 3;
				
				startSubTask(animateFrom, animateTo);
				mRedrawHandler.sleep(1, COIN_MOVING_DELAY);
			}

			private void startAnimation(int animateFrom, int animateTo) {
				
				this.animateTo = animateTo;
				
				x = getX(animateFrom);
				y = getY(animateFrom);
				
				animXPixel = getXPixel(x);
				animYPixel = getYPixel(y);
				
				animationInProgress = true;
				animationDirection = animateFrom < animateTo ? 1 : 2;
				startSubTask(animateFrom, animationDirection == 1 ? animateFrom + 1 : animateFrom - 1);
				
				mRedrawHandler.sleep(1, COIN_MOVING_DELAY);
			}

			private void notifyAnimationCompleted(int AnimatedTo) {
				
				currentPos = AnimatedTo;
				
				animationInProgress = false;
				
				if (currentPos == BORAD_MAX_NUMBER) {
					
					showToastMessage(4, -1);
					
					showAlertBox();
					
					return;
				}
				
				int i;
				for (i = 0; i < LADDERS.length; i++) {
					
					if (currentPos == LADDERS[i][0]) {
						
						showToastMessage(2, -1);
						
						forward(LADDERS[i][1] - currentPos, true);
					
						return;
					}
				}
				for (i = 0; i < SnakesLaddersThread.this.SNAKES.length; i++) {
					
					if (currentPos == SNAKES[i][0]) {
						
						showToastMessage(3, -1);
						
						forward(SNAKES[i][1] - currentPos, true);
						
						return;
					}
				}
				
				notifyCoinForwarded();
			}

			private void startSubTask(int subFrom, int subTo) {
				
				int i = 2;
				
				this.subTo = subTo;
				
				subTaskDirection = getY(subFrom) - getY(subTo) != 0 ? 2 : 1;
				
				x = getX(subTo);
				y = getY(subTo);
				
				targetXPixel = getXPixel(x);
				targetYPixel = getYPixel(y);
				
				if (animationDirection == 3) {
					
					if ((animXPixel <= targetXPixel) && (animYPixel <= targetYPixel)) {
						
						coinDirection = 6;
						
						if ((targetYPixel - animYPixel) > (targetXPixel - animXPixel)) {
							
							diagonalDriver = 2;
							
							diagonalIncrement = (targetXPixel - animXPixel) / ((targetYPixel - animYPixel) / 
									PIXEL_INCREMENT);
							
							return;
						}
						
						diagonalDriver = 1;
						
						diagonalIncrement = (targetYPixel - animYPixel) / ((targetXPixel - animXPixel) / 
								PIXEL_INCREMENT);
						
					} else if ((animXPixel >= targetXPixel) && (animYPixel <= targetYPixel)) {
						
						coinDirection = 5;
						
						if ((targetYPixel - animYPixel) > (animXPixel - targetXPixel)) {
							
							diagonalDriver = 2;

							diagonalIncrement = (animXPixel - targetXPixel) / ((targetYPixel - animYPixel) / 
									PIXEL_INCREMENT);
							
							return;
						}
						
						diagonalDriver = 1;
						
						diagonalIncrement = (targetYPixel - animYPixel) / ((animXPixel - targetXPixel) / 
								PIXEL_INCREMENT);
						
					} else if ((animXPixel <= targetXPixel) && (animYPixel >= targetYPixel)) {
						
						coinDirection = 8;
						
						if ((animYPixel - targetYPixel) > (targetXPixel - animXPixel)) {
							
							diagonalDriver = 2;
							
							diagonalIncrement = (targetXPixel - animXPixel) / ((animYPixel - targetYPixel) / 
									PIXEL_INCREMENT);
							
							return;
						}
						
						diagonalDriver = 1;
						
						diagonalIncrement = (animYPixel - targetYPixel) / ((targetXPixel - animXPixel) / 
								PIXEL_INCREMENT);
						
					} else if ((this.animXPixel >= this.targetXPixel) && (this.animYPixel >= this.targetYPixel)) {
						
						coinDirection = 7;
						 
						if ((animYPixel - targetYPixel) > (animXPixel - targetXPixel)) {
							
							diagonalDriver = 2;
							
							diagonalIncrement = (animXPixel - targetXPixel) / ((animYPixel - targetYPixel) 
									/ PIXEL_INCREMENT);
							
							return;
						}
					
						diagonalDriver = 1;
						
						diagonalIncrement = (animYPixel - targetYPixel) / ((animXPixel - targetXPixel) / 
								PIXEL_INCREMENT);
						
					}
				} else if (subTaskDirection == 2) {
					
					if (animYPixel >= targetYPixel) {
						i = 1;
					}
					
					coinDirection = i;
					
				} else if (subTaskDirection == 1) {
					
					coinDirection = animXPixel < targetXPixel ? 4 : 3;
				}
			}

			public void updateMe() {
				
				if (animationDirection == 3) {
					
					if (coinDirection == 6) {
						
						if (diagonalDriver == 1) {
							
							animXPixel += PIXEL_INCREMENT;							
							
							animYPixel += diagonalIncrement;
							
						} else if (diagonalDriver == 2) {
							
							animXPixel += diagonalIncrement;
							
							animYPixel += PIXEL_INCREMENT;
							
						}
						
						if (animYPixel >= targetYPixel) {
							
							animYPixel = targetYPixel;
							
						}
						
						if (animXPixel >= targetXPixel) {
							
							animXPixel = targetXPixel;
							
						}
						
						if ((animYPixel >= targetYPixel) && (animXPixel >= targetXPixel)) {
							
							notifySubTaskCompleted();
							
							return;
							
						}
					} else if (coinDirection == 5) {
						
						if (diagonalDriver == 1) {
							
							animXPixel -= PIXEL_INCREMENT;
							
							animYPixel += diagonalIncrement;
							
						} else if (diagonalDriver == 2) {
							
							animXPixel -= diagonalIncrement;
							
							animYPixel += PIXEL_INCREMENT;
							
						}
						
						if (animYPixel >= targetYPixel) {
							
							animYPixel = targetYPixel;
							
						}
						
						if (animXPixel <= targetXPixel) {
							
							animXPixel = targetXPixel;
							
						}
						
						if ((animYPixel >= targetYPixel) && (animXPixel <= targetXPixel)) {
							
							notifySubTaskCompleted();
							
							return;
							
						}
					} else if (coinDirection == 8) {
						
						if (diagonalDriver == 1) {
							
							animXPixel += PIXEL_INCREMENT;
							
							animYPixel -= diagonalIncrement;
							
						} else if (diagonalDriver == 2) {
							
							animXPixel += diagonalIncrement;
						
							animYPixel -= PIXEL_INCREMENT;
							
						}
						
						if (animYPixel <= targetYPixel) {
							
							animYPixel = targetYPixel;
							
						}
						
						if (animXPixel >= targetXPixel) {
							
							animXPixel = targetXPixel;
							
						}
						
						if ((animYPixel <= targetYPixel) && (animXPixel >= targetXPixel)) {
							
							notifySubTaskCompleted();
							
							return;
							
						}
					} else if (coinDirection == 7) {
						
						if (diagonalDriver == 1) {
							
							animXPixel -= PIXEL_INCREMENT;
						
							animYPixel -= diagonalIncrement;
							
						} else if (diagonalDriver == 2) {
							
							animXPixel -= diagonalIncrement;
							
							animYPixel -= PIXEL_INCREMENT;
							
						}
						
						if (animYPixel <= targetYPixel) {
						
							animYPixel = targetYPixel;
							
						}
						
						if (animXPixel <= targetXPixel) {
							
							animXPixel = targetXPixel;
							
						}
						
						if ((animYPixel <= targetYPixel) && (animXPixel <= targetXPixel)) {
							
							notifySubTaskCompleted();
							
							return;
							
						}
					}
				} else if (subTaskDirection == 2) {
					
					if (coinDirection == 2) {
						
						animYPixel += PIXEL_INCREMENT;
						
						if (animYPixel >= targetYPixel) {
							
							animYPixel = targetYPixel;
							
							notifySubTaskCompleted();
							
							return;
							
						}
					} else if (coinDirection == 1) {
						
						animYPixel -= PIXEL_INCREMENT;
						
						if (animYPixel <= targetYPixel) {
							
							animYPixel = targetYPixel;
							
							notifySubTaskCompleted();
							
							return;
						}
					}
				} else if (subTaskDirection == 1) {
				
					if (coinDirection == 4) {
						
						animXPixel += PIXEL_INCREMENT;
						
						if (animXPixel >= targetXPixel) {
							
							animXPixel = targetXPixel;
							
							notifySubTaskCompleted();
							
							return;
						}
					} else if (coinDirection == 3) {
						
						animXPixel -= PIXEL_INCREMENT;
						
						if (animXPixel <= targetXPixel) {
							
							animXPixel = targetXPixel;
							
							notifySubTaskCompleted();
							
							return;
							
						}
					}
				}
				
				mRedrawHandler.sleep(1, COIN_MOVING_DELAY);
			}

			private void notifySubTaskCompleted() {
				if (subTo == animateTo) {
				
					mRedrawHandler.removeMessages(1);
					
					notifyAnimationCompleted(animateTo);
					
					return;
					
				}
				
				startSubTask(subTo, animationDirection == 1 ? subTo + 1 : subTo - 1);
				mRedrawHandler.sleep(1, COIN_MOVING_DELAY);
			}
		}

		class DiceHandler {

			private int rollDiceCounter = 0;
			private float myDeltaX, myDeltaY;
			private final long DICE_ROLLING_DELAY;
			private boolean isDiceRolling = false;

			DiceHandler() {
				
				DICE_ROLLING_DELAY = Long.parseLong(getResources().getString(R.string.diceRollingDelay));
				
			}

			public void rollDice(float myDeltaX, float myDeltaY) {
				
				if (!isDiceRolling) {
					
					this.myDeltaX = myDeltaX;
					this.myDeltaY = myDeltaY;
					
					isDiceRolling = true;
					isDiceReady = false;
					
					mRedrawHandler.sleep(0, DICE_ROLLING_DELAY);
				}
			}

			private void updateMe() {
				
				if (isDiceRolling) {
					
					int i = rollDiceCounter + 1;
					
					rollDiceCounter = i;
					
					if (i <= 20) {
						
						prepareAnimatedDiceBitmap();
						
						SnakesLaddersThread snakesLaddersThread = SnakesLaddersThread.this;
						
						snakesLaddersThread.dicePosX = snakesLaddersThread.dicePosX + this.myDeltaX;
						snakesLaddersThread = SnakesLaddersThread.this;
						snakesLaddersThread.dicePosY = snakesLaddersThread.dicePosY + myDeltaY;
						
						if (dicePosX < 0.0f || dicePosX + ((float) diceWidthHeight) > ((float) boardWidth)) {
							
							myDeltaX *= -1.0f;
							snakesLaddersThread = SnakesLaddersThread.this;
							snakesLaddersThread.dicePosX = snakesLaddersThread.dicePosX + this.myDeltaX;
							
						}
						
						if (dicePosY < 0.0f || dicePosY + ((float)diceWidthHeight) > ((float) boardHeight)) {
							
							myDeltaY *= -1.0f;
							snakesLaddersThread = SnakesLaddersThread.this;
							snakesLaddersThread.dicePosY = snakesLaddersThread.dicePosY + myDeltaY;
							
						}
						
						mRedrawHandler.sleep(0, DICE_ROLLING_DELAY);
						
						return;
						
					}
					
					diceValue = prepareDiceBitmap();
					isDiceRolling = false;
					rollDiceCounter = 0;
					
					notifyDiceRolled();
				}
			}
		}

		class RefreshHandler extends Handler {

			public void handleMessage(Message msg) {
				if (msg.what == 0) {
					
					dice.updateMe();
					
				} else if (msg.what == 1) {
					
					currentCoin.updateMe();
					
				}
			}

			public void sleep(int what, long delayMillis) {
				
				removeMessages(what);

				sendMessageDelayed(obtainMessage(what), delayMillis);
				
			}
		}

		public SnakesLaddersThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
		
			// 2
			
			mSurfaceHolder = surfaceHolder;
			
			mContext = context;
			
			//r = context.getResources();
			
			BORAD_MAX_NUMBER = Integer.parseInt(mContext.getString(R.string.boardMaxNumber));
		}

		public void setGameMode(boolean flag) {
			
			// 5
			
			singlePlayer = flag;
			
		}

		public void setIsResume(boolean isResume) {
			
			this.isResume = isResume;
			
		}

		public void setPlayerNames(String player1Name, String player2Name) {
			
			// 4
			
			this.player1Name = player1Name;
			
			this.player2Name = player2Name;
			
		}

		private void initNewGame() {
			
			if (singlePlayer) {
				player2Name = getResources().getString(R.string.systemDeafultName);
			}
			
			whoseTurn = 1;
			isDiceReady = true;
			
			dice = new DiceHandler();
			player1Coin = new CoinHandler(-65536, 0);
			player2Coin = new CoinHandler(-16711936, 0);
		}

		public void setRunning(boolean b) {
			
			mRun = b;
					
		}

		public void setRight(int right) {
			
			this.right = right;
			
		}

		public void setBottom(int bottom) {
			
			this.bottom = bottom;
			
		}

		public void setSurfaceSize(int width, int height) {
			
			synchronized (mSurfaceHolder) {
				
				boardScreenPercent = Integer.parseInt(getResources().getString(R.string.boardScreenPercent));
				
				boardWidth = width;
				boardHeight = ((int) (((double) height) / 100.0d)) * boardScreenPercent;
				
				boxWidth = width / 10;
				boxHeight = boardHeight / 10;
				
				coinRadius = boxHeight / 3;
				freeSpaceHeight = height - boardHeight;
				
				diceWidthHeight = height / 7;
				
				dicePosX = (float) (RNG.nextInt(Integer.MAX_VALUE) % (boardWidth - diceWidthHeight));
				dicePosY = (float) (RNG.nextInt(Integer.MAX_VALUE) % (boardHeight - diceWidthHeight));
				
				mPaint.setAntiAlias(true);
				textPaint.setTextSize(Float.parseFloat(getResources().getString(R.string.textSize)));
				textPaint.setTypeface(Typeface.DEFAULT_BOLD);
				
				prepareBoardBitmap();
				
				loadDiceBitmaps();
				
				prepareDiceBitmap();
				
				prepareCoins();
				
				if (!isResume) {
					
					initNewGame();
					 
				}
			}
		}

		private void prepareBoardBitmap() {
			
			if (boardBitmap == null) {
				boardBitmap = Bitmap.createBitmap(boardWidth, boardHeight, Config.ARGB_8888);
				
				Canvas boardCanvas = new Canvas(boardBitmap);
				Drawable board = getResources().getDrawable(R.drawable.board);
				board.setBounds(0, 0, boardWidth, boardHeight);  // left, Top, Right, Bottom
				board.draw(boardCanvas);
			}
		}

		private void loadDiceBitmaps() {
			
			int x;
			
			diceBitmaps = new Bitmap[6];
			
			Canvas[] diceCanvases = new Canvas[6];
			
			Drawable[] diceDrawables = new Drawable[6];
			
			for (x = 0; x < DICE_IMAGES.length; x++) {
				
				if (diceBitmaps[x] == null) {
					
					diceBitmaps[x] = Bitmap.createBitmap(diceWidthHeight, diceWidthHeight, Config.ARGB_8888);
					
					diceCanvases[x] = new Canvas(diceBitmaps[x]);
					
					diceDrawables[x] = getResources().getDrawable(DICE_IMAGES[x].intValue());
					
					diceDrawables[x].setBounds(0, 0, diceWidthHeight, diceWidthHeight);
					
					diceDrawables[x].draw(diceCanvases[x]);

				}
			}
			
			diceAnimBitmaps = new Bitmap[31];
			
			x = 0;
			while (x < 31) {
				try {
					if (diceAnimBitmaps[x] == null) {
						
						diceAnimBitmaps[x] = Bitmap.createScaledBitmap(BitmapFactory.
								decodeStream(getContext().getAssets().open("image" + (x + 1) + ".png")),
								diceWidthHeight, diceWidthHeight, false);
					}
					
					x++;
					
				} catch (Exception e) {
					
					e.printStackTrace();
					
					return;
				}
			}
		}

		private int prepareDiceBitmap() {
			
			int random = RNG.nextInt(Integer.MAX_VALUE) % DICE_IMAGES.length;
			
			currentDiceBitmap = diceBitmaps[random];
			
			return random + 1;
		}

		private void prepareAnimatedDiceBitmap() {
			
			animDiceCounter++;
			
			if (animDiceCounter >= 31) {
				
				animDiceCounter = 0;
				
			}
			
			currentDiceBitmap = diceAnimBitmaps[animDiceCounter];
		}

		public void run() {
			
			while (mRun) {
				
				Canvas c = null;
				
				try {
					c = mSurfaceHolder.lockCanvas(null);
					
					synchronized (mSurfaceHolder) {
						
						doDraw(c);
						
					}
					
					if (c != null) {
						
						mSurfaceHolder.unlockCanvasAndPost(c);
						
					}
				} catch (Throwable th) {
					
					if (c != null) {
						
						mSurfaceHolder.unlockCanvasAndPost(c);
						
					}
				}
			}
		}

		public void doDraw(Canvas canvas) {
			
			if (canvas != null) {
				
				canvas.drawColor(Color.YELLOW);
				canvas.drawBitmap(boardBitmap, 0.0f, 0.0f, mPaint);
				
				player1Coin.draw(canvas);
				player2Coin.draw(canvas);
				
				canvas.drawBitmap(currentDiceBitmap, dicePosX, dicePosY, mPaint);
				displayText = whoseTurn == 1 ? player1Name : player2Name;
				
				if (whoseTurn == 1) {
					
					textPaint.setColor(Color.BLUE);
					
				} else {
					
					textPaint.setColor(Color.BLACK);
					
				}
				
				canvas.drawText(displayText, ((float) (boardWidth / 2)) - (textPaint.measureText(displayText) / 2.0f),
						(float) (boardHeight + (freeSpaceHeight / 2)), textPaint);
			}
		}

		private void prepareCoins() {
			
			redCoin = Bitmap.createBitmap(coinRadius, coinRadius, Config.ARGB_8888);
			Canvas boardCanvas = new Canvas(redCoin);
			Drawable board = getResources().getDrawable(R.drawable.red);
			board.setBounds(0, 0, coinRadius, coinRadius);
			board.draw(boardCanvas);
			
			greenCoin = Bitmap.createBitmap(coinRadius, coinRadius, Config.ARGB_8888);
			Canvas boardCanvas2 = new Canvas(greenCoin);
			Drawable board2 = getResources().getDrawable(R.drawable.green);
			board2.setBounds(0, 0, coinRadius, coinRadius);
			board2.draw(boardCanvas2);
		}

		public void notifyDiceRolled() {
			
			showToastMessage(1, this.diceValue);
			
			currentCoin = whoseTurn == 1 ? player1Coin : player2Coin;
			
			currentCoin.forward(diceValue, false);
		}

		public void notifyCoinForwarded() {
			if (diceValue != 6) {
				
				rotateStrike();
			}
			
			if (singlePlayer && whoseTurn == 2) {
				
				showToastMessage(5, -1);
				
				new PausingUtil(Long.parseLong(getResources().getString(R.string.pausingDelay)), 
						new PausingUtil.PausingListener() {
							
							public void notifyPausingCompleted() {
								
								dice.rollDice(deltaX / 2.0f, deltaY / 2.0f); 
								
								isDiceReady = true;
							}
						}).start();
				return;
			}
			
			isDiceReady = true;
		}

		private void showToastMessage(int result, int value) {
			
			String message = "";
			
			int image = -1;
			
			if (result == 6) {
				message = getResources().getString(R.string.anotherChanceText).replace("[PLAYERNAME]", 
						whoseTurn == 1 ? player1Name : player2Name);
			}
			
			if (result == 5) {
				
				message = getResources().getString(R.string.systemTurnMessage);
				
			} else if (result == 1) {
				
				message = new StringBuilder(String.valueOf(whoseTurn == 1 ? player1Name : player2Name)).append(", Dice :")
						.append(value).toString();
				
				image = R.drawable.dice6_toast;
				
			} else if (result == 2) {
				
				message = "Ladder!";
				
				image = R.drawable.ladder;
				
			} else if (result == 3) {
				
				message = "Snake!";
				
				image = R.drawable.snake;
				
			} else if (result == 4) {
				
				message = getResources().getString(R.string.gameWonText).replace("[PLAYERNAME]", whoseTurn == 1 ? 
						player1Name : player2Name);
				
				image = R.drawable.prize;
				
			}
			
			toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);

			toast.setGravity(80, 0, 0);
			
			if (result == 2 || result == 3 || result == 4 || (result == 1 && value == 6)) {
				
				LinearLayout toastView = (LinearLayout) toast.getView();
				
				ImageView imgView = new ImageView(getContext());
				imgView.setImageResource(image);
				imgView.setAdjustViewBounds(true);
				imgView.setMaxHeight(bottom / 7);
				toastView.addView(imgView, 0);
			}
			
			toast.show();
			
			if (result == 1 && value == 6) {
				
				showToastMessage(6, -1);
				
			}
		}

		public void rotateStrike() {
			if (whoseTurn == 1) {
				
				whoseTurn = 2;
				
			} else if (whoseTurn == 2) {
				
				whoseTurn = 1;
				
			}
		}

		public void saveState(Editor editor) {
			
			mRedrawHandler.removeMessages(0);
			mRedrawHandler.removeMessages(1);
			
			if (player1Coin != null) {
				
				editor.putInt("player1Position", player1Coin.getCurrentPosition());
				
			}
			
			if (player2Coin != null) {
				
				editor.putInt("player2Position", player2Coin.getCurrentPosition());
				
			}
			
			editor.putString("player1Name", player1Name);
			editor.putString("player2Name", player2Name);
			
			editor.putInt("whoseTurn", whoseTurn);
			editor.putBoolean("singlePlayer", singlePlayer);
		}
		
		public void restoreState(SharedPreferences settings) {
			
			whoseTurn = settings.getInt("whoseTurn", 1);
			
			singlePlayer = settings.getBoolean("singlePlayer", false);
			
			player1Name = settings.getString(player1Name, getResources().getString(R.string.player1DeafultName));
			
			player2Name = settings.getString(player2Name, getResources().getString(R.string.player2DeafultName));
			
			isDiceReady = true;
			
			dice = new DiceHandler();
			
			player1Coin = new CoinHandler(-65536, settings.getInt("player1Position", 0));
			
			player2Coin = new CoinHandler(-16711936, settings.getInt("player2Position", 0));
			
			if (singlePlayer) {
				
				player2Name = getResources().getString(R.string.systemDeafultName);
				if (whoseTurn == 2) {
					
					dice.rollDice(deltaX / 2.0f, deltaY / 2.0f);
					
				}
			}
		}

		public boolean doTouchEvent(MotionEvent event) {
			
			// 6
			
			synchronized (mSurfaceHolder) {
			
				synchronized (event) {
				
					try {
						event.wait(1);
						
						if (event.getAction() == 0) {
							
							deltaY = 0.0f;
							
							deltaX = 0.0f;
							
							initialX = event.getRawX();
							
							initialY = event.getRawY();
							
						}
						
						if (isDiceReady && event.getAction() == 1) {
							
							DisplayMetrics dm = getResources().getDisplayMetrics();						
							
							int x = (int) (initialX - ((float) (dm.widthPixels - right)));
							
							int y = (int) (initialY - ((float) (dm.heightPixels - bottom)));
							
							if (((float) x) >= dicePosX && ((float) x) <= dicePosX + ((float) diceWidthHeight)
									&& ((float) y) >= dicePosY && ((float) y) <= dicePosY + ((float) diceWidthHeight)) {
								
								deltaX = event.getRawX() - initialX;
								
								deltaY = event.getRawY() - initialY;
								
								dice.rollDice(deltaX / 2.0f, deltaY / 2.0f);
							}
						}
					} catch (InterruptedException e) {
					}
				}
			}
			
			return true;
		}

		public void doSensorChanged(int sensor, float[] values) {
			
			// 7
			
			if (sensor == 2) {
			
				double totalForce = Math.sqrt(((0.0d + Math.pow((double) (values[0] / 9.80665f), 2.0d)) + 
						Math.pow((double) (values[1] / 9.80665f), 2.0d)) + Math.pow((double) (values[2] / 9.80665f),
						2.0d));
				
				if (totalForce < 1.5d && m_totalForcePrev > 1.5d) {
					
					int tmpX = RNG.nextInt(100);
					
					int tmpY = RNG.nextInt(100);
					
					if (dice != null) {
						
						dice.rollDice((float) tmpX, (float) tmpY);
						
					}
				}
				
				m_totalForcePrev = totalForce;
			}
		}

		private void showAlertBox() {
			
			AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
			
			alertDialog.setTitle(R.string.game_over);
			
			alertDialog.setMessage(getResources().getString(R.string.startNewGame));
			
			alertDialog.setButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
							
					initNewGame();
				}
			});
			
			alertDialog.setButton2(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
						
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {

				}
			});
			
			alertDialog.show();
		}
	}

	@SuppressLint("HandlerLeak")
	public SnakesLaddersView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
		
		holder.addCallback(SnakesLaddersView.this);
		
		thread = new SnakesLaddersThread(holder, context, new Handler() {
			
			public void handleMessage(Message m) {
				
				mStatusText.setVisibility(VISIBLE);
				
				mStatusText.setText(m.getData().getString("text"));
			}
		});
		
		setFocusable(true);
		
		setFocusableInTouchMode(true);
	}

	public SnakesLaddersThread getThread() {
		
		// 3
		
		return thread;
		
	}

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		
		return thread.doTouchEvent(event);
	}

	public void onSensorChanged(int sensor, float[] values) {
		
		thread.doSensorChanged(sensor, values);
		
	}

	public void setTextView(TextView textView) {
		
		mStatusText = textView;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
		thread.setSurfaceSize(width, height);
		
		thread.setBottom(getBottom());
		
		thread.setRight(getRight());
	}

	public void surfaceCreated(SurfaceHolder holder) {
		
		thread.setRunning(true);
		
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;

		thread.setRunning(false);
		
		while (retry) {
			try {
				
				thread.join();
				
				retry = false;
				
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
