package com.example.androidcodes;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class HighScoresUtil {
	
	private HighScoresListener listener;
	
	private final int MAX_SCORES;
	private Context context;
	private long currentScore;
	
	private String name;
	private String[] scores;
	
	private SharedPreferences settings;

	public interface HighScoresListener {
		
		void onOk(HighScoresUtil highScoresUtil);
	}

	public HighScoresUtil(Context context, String preferences_filename) {
		
		this.context = context;
		
		scores = new String[10];
		MAX_SCORES = 10;
		
		settings = context.getSharedPreferences(preferences_filename, 0);
	}

	public HighScoresUtil(Context context, String preferences_filename, long currentScore, 
			HighScoresListener listener) {
		
		this.context = context;
		this.currentScore = currentScore;
		this.listener = listener;
		
		scores = new String[10];
		
		MAX_SCORES = 10;
		settings = context.getSharedPreferences(preferences_filename, 0);
		
		loadScores();
	}

	private void loadScores() {
		
		for (int x = 0; x < 10; x++) {
			scores[x] = settings.getString("score-" + x, "|0");
		}
	}

	public void show() {
		if (isScoreEligible()) {
			showEnterNameDialog();
		} else if (this.listener != null) {
			listener.onOk(this);
		}
	}

	@SuppressWarnings("deprecation")
	public void showEnterNameDialog() {
		
		View enterNameView = LayoutInflater.from(this.context).inflate(R.layout.enter_name_dialog, null);
		
		AlertDialog dialog = new Builder(context).create();
		dialog.setTitle(R.string.highScore);
		dialog.setMessage(this.context.getString(R.string.topScore).replace("[TOPSCORE]", 
				String.valueOf(this.currentScore)));
		dialog.setView(enterNameView);
		
		final EditText txt = (EditText) enterNameView.findViewById(R.id.userName);
		
		txt.setText(R.string.unknown);
		dialog.setButton(this.context.getString(R.string.ok), new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
					
				name = txt.getText().toString().trim();
				
				if (!name.equals("")) {
					saveScores();
				}
						
				if (listener != null) {
					listener.onOk(HighScoresUtil.this);
				}
			}
		});
		dialog.show();
	}

	private boolean isScoreEligible() {
		if (currentScore > extractScore(scores[9]).longValue()) {
			return true;
		}
		return false;
	}

	private String extractName(String str) {
		return str.substring(0, str.indexOf("|"));
	}

	private Long extractScore(String str) {
		return Long.valueOf(Long.parseLong(str.substring(str.indexOf("|") + 1)));
	}

	private void saveScores() {
		int x, index = 0;
		
		while (index < 10 && extractScore(this.scores[index]).longValue() > this.currentScore) {
			index++;
		}
		for (x = 9; x > index; x--) {
			scores[x] = scores[x - 1];
		}
		scores[index] = name + "|" + currentScore;
		
		Editor editor = settings.edit();
		for (x = 0; x < 10; x++) {
			editor.putString("score-" + x, scores[x]);
		}
		editor.commit();
	}

	@SuppressWarnings("deprecation")
	public void showHighScores() {
		
		TableLayout table = new TableLayout(context);
		table.setLayoutParams(new LayoutParams(-1, -1));
		table.setStretchAllColumns(true);
		
		for (int x = 0; x < 10; x++) {
			
			String str = settings.getString("score-" + x, "|0");
			
			if (extractScore(str).longValue() > 0) {
				
				TableRow row = new TableRow(context);
				
				TextView txt1 = new TextView(context);
				txt1.setText(extractName(str));
				txt1.setGravity(5);
				row.addView(txt1);
				
				TextView txt2 = new TextView(context);
				txt2.setText(":");
				txt2.setGravity(1);
				row.addView(txt2);
				
				TextView txt3 = new TextView(context);
				txt3.setText(extractScore(str).toString());
				txt3.setGravity(3);
				row.addView(txt3);
				
				table.addView(row);
			}
		}
		
		AlertDialog dialog = new Builder(context).create();
		dialog.setTitle(R.string.highScores);
		dialog.setView(table);
		dialog.setButton(this.context.getString(R.string.ok), new OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
			
			}
		});
		dialog.show();
	}
}
