package com.example.androidcodes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class PlayerNamesActivity extends Activity {
	
	private String PREFS_NAME;
	
	private EditText player1Text, player2Text;
	
	private SharedPreferences settings;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_names_layout);
		
		PREFS_NAME = getString(R.string.preferences_filename);
		settings = getSharedPreferences(this.PREFS_NAME, 0);
		
		player1Text = (EditText) findViewById(R.id.player1_edit);
		
		player1Text.setText(settings.getString(getString(R.string.player1NameKey), 
				getString(R.string.player1DeafultName)));
		player2Text = (EditText) findViewById(R.id.player2_edit);
		player2Text.setText(this.settings.getString(getString(R.string.player2NameKey),
				getString(R.string.player2DeafultName)));
	}

	public void playerNamesClickHandler(View view) {
		
		switch (view.getId()) {
		
		case R.id.okButton /* 2131165190 */:
			
			player1Text = (EditText) findViewById(R.id.player1_edit);
			player2Text = (EditText) findViewById(R.id.player2_edit);
			
			Editor editor = settings.edit();
			String player1TxtVal = player1Text.getText().toString();
			String player2TxtVal = player2Text.getText().toString();
			if (!player1TxtVal.trim().equals("")) {
				editor.putString(getString(R.string.player1NameKey), player1Text.getText().toString());
			}
			if (!player2TxtVal.trim().equals("")) {
				editor.putString(getString(R.string.player2NameKey), player2Text.getText().toString());
			}
			editor.commit();
			finish();
			
			return;
	
		case R.id.cancelButton /* 2131165191 */:
			
			finish();
			return;
		default:
			return;
		}
	}
}
