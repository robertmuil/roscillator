package muil.robert.acceleratorsqueak;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import muil.robert.acceleratorsqueak.FMOut;

public class MainActivity extends Activity implements SensorEventListener, OnCheckedChangeListener, OnSeekBarChangeListener {

	public static final int FMax = 7000;
	public static final int FMin = 100;
	
	/* TODO: get and store in persistent storage */
	public static final int [][] DefFRanges = {{100,700},{3000,4000},{6000,7000}};
	
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private FMOut mFM;
	private TextView mTxtStatus;
	private TextView mTxtAcc1, mTxtAcc2, mTxtAcc3, 
	mTxtFreq1Lo, mTxtFreq1Hi, mTxtFreq2Lo, mTxtFreq2Hi,
	mTxtFreq3Lo, mTxtFreq3Hi;
	
	private SeekBar mSeek1Lo, mSeek1Hi;
	private SeekBar mSeek2Lo, mSeek2Hi;
	private SeekBar mSeek3Lo, mSeek3Hi;
	private ProgressBar mPrg1, mPrg2, mPrg3;
	private ToggleButton mTglPlay;
	private TextView mCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTxtStatus = (TextView) findViewById(R.id.txtStatus);
		mTxtAcc1 = (TextView) findViewById(R.id.txtAcc1);
		mTxtAcc2 = (TextView) findViewById(R.id.txtAcc2);
		mTxtAcc3 = (TextView) findViewById(R.id.txtAcc3);
		
		mTxtFreq1Lo = (TextView) findViewById(R.id.txtFreq1Lo);
		mTxtFreq1Hi = (TextView) findViewById(R.id.txtFreq1Hi);
		mTxtFreq2Lo = (TextView) findViewById(R.id.txtFreq2Lo);
		mTxtFreq2Hi = (TextView) findViewById(R.id.txtFreq2Hi);
		mTxtFreq3Lo = (TextView) findViewById(R.id.txtFreq3Lo);
		mTxtFreq3Hi = (TextView) findViewById(R.id.txtFreq3Hi);

		mSeek1Lo = (SeekBar) findViewById(R.id.seek1Lo);
		mSeek1Hi = (SeekBar) findViewById(R.id.seek1Hi);
		mSeek2Lo = (SeekBar) findViewById(R.id.seek2Lo);
		mSeek2Hi = (SeekBar) findViewById(R.id.seek2Hi);
		mSeek3Lo = (SeekBar) findViewById(R.id.seek3Lo);
		mSeek3Hi = (SeekBar) findViewById(R.id.seek3Hi);
		mTglPlay = (ToggleButton) findViewById(R.id.tglPlay);
		mPrg1 = (ProgressBar) findViewById(R.id.prg1);
		mPrg2 = (ProgressBar) findViewById(R.id.prg2);
		mPrg3 = (ProgressBar) findViewById(R.id.prg3);
		
		mCircle = (TextView) findViewById(R.id.txtCircle);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		mFM = new FMOut(3);
		mFM.setRanges(DefFRanges);
		
		mSeek1Lo.setMax(FMax - FMin);
		mSeek2Lo.setMax(FMax - FMin);
		mSeek3Lo.setMax(FMax - FMin);
		mSeek1Hi.setMax(FMax - FMin);
		mSeek2Hi.setMax(FMax - FMin);
		mSeek3Hi.setMax(FMax - FMin);
		
		/* TODO: extract from persistent storage */
		/* mSeek1Lo.setProgress(); ...*/
		mSeek1Lo.setProgress(DefFRanges[0][0]);
		mSeek1Hi.setProgress(DefFRanges[0][1]);
		mSeek2Lo.setProgress(DefFRanges[1][0]);
		mSeek2Hi.setProgress(DefFRanges[1][1]);
		mSeek3Lo.setProgress(DefFRanges[2][0]);
		mSeek3Hi.setProgress(DefFRanges[2][1]);
		mTxtFreq1Lo.setText(String.format("%d", mSeek1Lo.getProgress()));
		mTxtFreq1Hi.setText(String.format("%d", mSeek1Hi.getProgress()));
		mTxtFreq2Lo.setText(String.format("%d", mSeek2Lo.getProgress()));
		mTxtFreq2Hi.setText(String.format("%d", mSeek2Hi.getProgress()));
		mTxtFreq3Lo.setText(String.format("%d", mSeek3Lo.getProgress()));
		mTxtFreq3Hi.setText(String.format("%d", mSeek3Hi.getProgress()));

		
		mTglPlay.setOnCheckedChangeListener(this);
		
		mSeek1Lo.setOnSeekBarChangeListener(this);
		mSeek1Hi.setOnSeekBarChangeListener(this);
		mSeek2Lo.setOnSeekBarChangeListener(this);
		mSeek2Hi.setOnSeekBarChangeListener(this);
		mSeek3Lo.setOnSeekBarChangeListener(this);
		mSeek3Hi.setOnSeekBarChangeListener(this);
		
		if (mAcc != null){
			mTxtStatus.setText(R.string.all_good);
			Log.v("Shyit", "Everyfing goood y'aaall");
		}
		else{
			mTxtStatus.setText(R.string.missing_accel_sensor);
			
			//Context context = getApplicationContext();
			Toast.makeText(this, "No Accelerometer!", Toast.LENGTH_LONG).show();
			Log.e("Shyit", "No bleedin' Accelerometer!");
		}

		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.d("MainActivity", "onResume()");
		super.onResume();
		mSensorManager.registerListener(this, mAcc,
				SensorManager.SENSOR_DELAY_FASTEST);
		
		/*
		 * NB: as soon as an application launches another thread,
		 * as will be done here with the start(), the Android
		 * system will not kill the application by default 
		 * when the user presses the back key.
		 */
		if (!mFM.isAlive())
			mFM.start();
		
		if (mTglPlay.isChecked()) {
			mFM.startPlaying();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("MainActivity", "onBackPressed()");
		finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		Log.d("MainActivity", "onStop()");
		mSensorManager.unregisterListener(this);
		if (mFM.isAlive()) {
			mFM.stopPlaying();
			mFM.end();
		}
		
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		Log.d("Accelerometer", "Sensor '"+arg0+"' changed accuracy to " + arg1);
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		double [] acc_norm = new double [3];
		
		/* normed, accels go from 0 (-ve gravity) to 1 (+ve gravity). silly */
		for (int ii = 0; ii < 3; ii++) {
			acc_norm[ii] = (arg0.values[ii]+SensorManager.GRAVITY_EARTH)/(2*SensorManager.GRAVITY_EARTH);
			
			mFM.updateSignal(ii, (float) acc_norm[ii]);
		}
		

		GradientDrawable ss = (GradientDrawable) mCircle.getBackground();
		ss.setColor(Color.argb(255, (int)(acc_norm[0]*255),
				(int)(acc_norm[1]*255),
				(int)(acc_norm[2]*255)));
		//ss.setSize((int)(20+100*acc_norm[0]), 50);
		
		mTxtAcc1.setText(String.format("%02.1f", arg0.values[0]));
		mTxtAcc2.setText(String.format("%02.1f", arg0.values[1]));
		mTxtAcc3.setText(String.format("%02.1f", arg0.values[2]));
		
		mPrg1.setProgress((int) (acc_norm[0]*mPrg1.getMax()));
		mPrg2.setProgress((int) (acc_norm[1]*mPrg2.getMax()));
		mPrg3.setProgress((int) (acc_norm[2]*mPrg3.getMax()));

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked)
			mFM.startPlaying();
		else
			mFM.stopPlaying();
		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int f = seekBar.getProgress() + FMin;
		Log.i("SeekBar", "f="+f);
		if (seekBar.equals(mSeek1Lo)) {
			mTxtFreq1Lo.setText(String.format("%d", f));
			mFM.setLo(0, f);
		} else if (seekBar.equals(mSeek1Hi)) {
			mTxtFreq1Hi.setText(String.format("%d", f));
			mFM.setHi(0, f);
		} else if (seekBar.equals(mSeek2Lo)) {
			mTxtFreq2Lo.setText(String.format("%d", f));
			mFM.setLo(1, f);
		} else if (seekBar.equals(mSeek2Hi)) {
			mTxtFreq2Hi.setText(String.format("%d", f));
			mFM.setHi(1, f);
		} else if (seekBar.equals(mSeek3Lo)) {
			mTxtFreq3Lo.setText(String.format("%d", f));
			mFM.setLo(2, f);
		} else if (seekBar.equals(mSeek3Hi)) {
			mTxtFreq3Hi.setText(String.format("%d", f));
			mFM.setHi(2, f);
				
		} else {
			Toast.makeText(this, "unknown seekbar generated touch", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}
