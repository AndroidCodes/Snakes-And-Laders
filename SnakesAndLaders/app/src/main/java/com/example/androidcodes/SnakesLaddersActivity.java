package com.example.androidcodes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.androidcodes.SnakesLaddersView.SnakesLaddersThread;

public class SnakesLaddersActivity extends Activity {
	
	private String PREFS_NAME;
	
	private SnakesLaddersThread mSnakesLaddersThread;
	
	private SnakesLaddersView mSnakesLaddersView;
	
	private SensorManager m_sensorManager;
	
	private SharedPreferences settings;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.snakesladders_layout);
		
		mSnakesLaddersView = (SnakesLaddersView) findViewById(R.id.snakesLaddersCustomView);
		mSnakesLaddersThread = mSnakesLaddersView.getThread();
		
		m_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		m_sensorManager.registerListener(mSnakesLaddersView, 2, 1);
	
		PREFS_NAME = getString(R.string.preferences_filename);
	
		settings = getSharedPreferences(PREFS_NAME, 0);
	
		mSnakesLaddersThread.setPlayerNames(settings.getString(getString(R.string.player1NameKey),
				getString(R.string.player1DeafultName)), settings.getString(getString(R.string.player2NameKey),
				getString(R.string.player2DeafultName)));
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			
			String singlePlayer = bundle.getString(getString(R.string.singlePlayerKey));
		
			if (singlePlayer != null) {
				
				SnakesLaddersThread snakesLaddersThread = mSnakesLaddersThread;
				
				if (singlePlayer.equals(getString(R.string.yes))) {
					
					snakesLaddersThread.setGameMode(true);		// if singlePlayer - true
					
				} else {
					
					snakesLaddersThread.setGameMode(false);		// if singlePlayer - false
					
				}
		
				return;
			}
			
			mSnakesLaddersThread.setIsResume(bundle.getBoolean(getString(R.string.isResumeKey)));
			
			mSnakesLaddersThread.restoreState(getSharedPreferences(PREFS_NAME, 0));
		}
	}

	@SuppressWarnings("deprecation")
	protected void onPause() {
		super.onPause();
		
		m_sensorManager.unregisterListener(mSnakesLaddersView);
	
		Editor editor = settings.edit();
		
		mSnakesLaddersThread.saveState(editor);
		
		editor.commit();
	}

	@SuppressWarnings("deprecation")
	protected void onResume() {
		super.onResume();
		
		// 6
		
		m_sensorManager.registerListener(this.mSnakesLaddersView, 2, 1);
	}
}

