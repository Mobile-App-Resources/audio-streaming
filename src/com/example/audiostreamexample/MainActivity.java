package com.example.audiostreamexample;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener,
		OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {
	private Button btn_play, btn_pause, btn_stop;
	private SeekBar seekBar;
	private MediaPlayer mediaPlayer;
	private int lengthOfAudio;
	private final String URL = "http://songspkcompilations.com/indian/soulfularijit/%5BSongs.PK%5D%2012%20-%20Mickey%20Virus%20-%20Tose%20Naina.mp3";
	private static final int MINUTES_IN_AN_HOUR = 60;
	private static final int SECONDS_IN_A_MINUTE = 60;
	private final Handler handler = new Handler();
	private boolean is_loading = true;
	private boolean is_stopped = false;
	private final Runnable r = new Runnable() {
		@Override
		public void run() {
			updateSeekProgress();
		}
	};
	private TextView txtTime;
	private ProgressBar musicProgress;
	private ImageView artistImg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize_Controls();
	}

	private void initialize_Controls() {
		btn_play = (Button) findViewById(R.id.btn_play);
		btn_play.setOnClickListener(this);
		btn_pause = (Button) findViewById(R.id.btn_pause);
		btn_pause.setOnClickListener(this);
		btn_pause.setEnabled(false);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(this);
		btn_stop.setEnabled(false);

		musicProgress = (ProgressBar) findViewById(R.id.progress);
		artistImg = (ImageView) findViewById(R.id.artistImg);

		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnTouchListener(this);

		txtTime = (TextView) findViewById(R.id.time);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
		seekBar.setSecondaryProgress(percent);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		btn_play.setEnabled(true);
		btn_pause.setEnabled(false);
		btn_stop.setEnabled(false);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mediaPlayer.isPlaying()) {
			SeekBar tmpSeekBar = (SeekBar) v;
			mediaPlayer
					.seekTo((lengthOfAudio / 100) * tmpSeekBar.getProgress());
		}
		return false;
	}

	@Override
	public void onClick(View view) {

		try {
			mediaPlayer.setDataSource(URL);
			mediaPlayer.prepare();
			lengthOfAudio = mediaPlayer.getDuration();

		} catch (Exception e) {
			// Log.e("Error", e.getMessage());
		}

		switch (view.getId()) {
		case R.id.btn_play:
			if (is_stopped) {
				is_stopped = false;
				mediaPlayer.seekTo(0);
			}
			playAudio();
			break;
		case R.id.btn_pause:
			pauseAudio();
			break;
		case R.id.btn_stop:
			stopAudio();
			break;
		default:
			break;
		}

		updateSeekProgress();
	}

	private void updateSeekProgress() {
		if (mediaPlayer.isPlaying()) {
			if (is_loading) {
				is_loading = false;
				musicProgress.setVisibility(View.GONE);
				artistImg.setVisibility(View.VISIBLE);
			}
			int progress = (int) (((float) mediaPlayer.getCurrentPosition() / lengthOfAudio) * 100);

			int remainSec = (lengthOfAudio - mediaPlayer.getCurrentPosition()) / 1000;

			seekBar.setProgress(progress);
			txtTime.setText("" + timeConversion(remainSec));

			handler.postDelayed(r, 1000);
		}
	}

	private void stopAudio() {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
			is_stopped = true;
		}
		seekBar.setProgress(0);
		seekBar.setSecondaryProgress(0);
		txtTime.setText("" + timeConversion(lengthOfAudio / 1000));
		btn_play.setEnabled(true);
		btn_pause.setEnabled(false);
		btn_stop.setEnabled(false);
	}

	private void pauseAudio() {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
		}
		btn_play.setEnabled(true);
		btn_pause.setEnabled(false);
	}

	private void playAudio() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
		btn_play.setEnabled(false);
		btn_pause.setEnabled(true);
		btn_stop.setEnabled(true);
	}

	private static String timeConversion(int totalSeconds) {
		int hours = totalSeconds / MINUTES_IN_AN_HOUR / SECONDS_IN_A_MINUTE;
		int minutes = (totalSeconds - (hoursToSeconds(hours)))
				/ SECONDS_IN_A_MINUTE;
		int seconds = totalSeconds
				- ((hoursToSeconds(hours)) + (minutesToSeconds(minutes)));

		return hours + ":" + minutes + ":" + seconds;
	}

	private static int hoursToSeconds(int hours) {
		return hours * MINUTES_IN_AN_HOUR * SECONDS_IN_A_MINUTE;
	}

	private static int minutesToSeconds(int minutes) {
		return minutes * SECONDS_IN_A_MINUTE;
	}

	class PlayTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				mediaPlayer.setDataSource(URL);
				mediaPlayer.prepare();
				lengthOfAudio = mediaPlayer.getDuration();

			} catch (Exception e) {
				// Log.e("Error", e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			playAudio();
			updateSeekProgress();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		new PlayTask().execute();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		pauseAudio();
		finish();
	}
}