/**
 * 
 */
package muil.robert.roscillator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;


/**
 * @author Robert
 *
 */
public class FMOut extends Thread {
	private static String TAG = "FMOut";
	private static final short MaxAudioTrackSampleValue = Short.MAX_VALUE;	
	
	private static final int DesiredOutputSampleRate = 22050;
	private static final int LocalBufferTime = 250; /* milliseconds */

	public static final int DefFHi = 12000; /* hz */
	public static final int DefFLo = 10; /* hz */
	
	private final int numTracks;

	private boolean mPlaying = false;
	private boolean mAlive = true;
	private AudioTrack [] mTracks;
	private short [][] mLocalBuffers;
	private float [] mSigs;
	private FMUtils [] mFM;
	

	/**
	 * 
	 * @param ranges - an array of ranges, lo and hi frequencies for each track:
	 * 		should be mx2 where m is numTracks.
	 */
	public void setRanges(int [][] ranges) {
		Log.v(TAG, String.format("setRanges(): %dx%d", 
				ranges.length, ranges[0].length));
		for (int ii = 0; ii < numTracks; ii++) {
			mFM[ii].setFreqRange(ranges[ii]);
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public FMOut(int numberOfTracks) {
		super();
		
		numTracks = numberOfTracks;
		
		mTracks = new AudioTrack[numTracks];
		mLocalBuffers = new short [numTracks][];
		mFM = new FMUtils[numTracks];
		mSigs = new float [numTracks];
		mAmplitudes = 
		
		Log.i(TAG, 
				"nativeOutputSampleRate="+AudioTrack.getNativeOutputSampleRate(
						AudioManager.STREAM_MUSIC));
		Log.i(TAG,
				"desiredOutputSampleRate="+DesiredOutputSampleRate);
		
		int minTrackBufSz = AudioTrack.getMinBufferSize(
				DesiredOutputSampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		
		Log.i(TAG,
				"minBufferSize: "+minTrackBufSz);
		/* 
		 * Create all AudioTracks and fill their buffers with zeros.
		 */
		for (int ii = 0; ii < numTracks; ii++) {
			mSigs[ii] = 0;
			mTracks[ii] = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					DesiredOutputSampleRate, 
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 
					minTrackBufSz,
					AudioTrack.MODE_STREAM);

			mFM[ii] = new FMUtils(DefFLo, DefFHi, mTracks[ii].getSampleRate());
			
			Log.i(TAG, "track "+ii+": "+
					"playbackRate="+mTracks[ii].getPlaybackRate()+
					"; bufferDelay="+(minTrackBufSz*1000/mTracks[ii].getSampleRate())+"msecs");
	
			mLocalBuffers[ii] = new short [(int) Math.ceil(
					(float)mTracks[ii].getSampleRate()/LocalBufferTime)];
			Log.i(TAG, " -> samples in local buffer="+mLocalBuffers[ii].length);
			
			/* 
			 * fill the AudioTrack buffer with 0s:
			 * presumably to avoid playing random noise at the beginning.
			 * */
			int bytesInBuffer = 0;
			do {
				int samp;
				for (samp = 0; samp < (Math.min(mLocalBuffers[ii].length,
						(minTrackBufSz-bytesInBuffer)/2)); samp++) {
					mLocalBuffers[ii][samp] = 0;
				}
				mTracks[ii].write(mLocalBuffers[ii], 0, samp);
				bytesInBuffer += (samp*2);
			} while (bytesInBuffer < minTrackBufSz);
		}
	}

	@Override
	public void run() {
		super.run();
		setName(TAG);

		Log.i(TAG, "run()");
		Log.i(TAG, "ThreadPriority="+this.getPriority());
		
		//setPriority(MAX_PRIORITY);
		//Log.w(TAG, "Updated ThreadPriority to "+this.getPriority());
		
		mPlaying = false;
		
		while(mAlive) {
			if (mPlaying) {
				for (int ii = 0; ii < numTracks; ii++) {
					mFM[ii].updateSignal(mSigs[ii]);
				}
					
				for(int sample = 0; sample < mLocalBuffers[0].length; sample++ ) {
					short ss = 0;
					for (int ii = 0; ii < numTracks; ii++) {
						if (mSigs[ii] > 0) {
							ss += (short)(Math.sin(mFM[ii].updateAngle())
									* fmAmplitude * MaxAudioTrackSampleValue);
						}
					}
					mLocalBuffers[0][sample] = ss;
				}
				mTracks[0].write(mLocalBuffers[0], 0, mLocalBuffers[0].length);
			} else { // not playing
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				Log.v(TAG, "paused...");
			}
		}

	}
	
	/******************************************************************
	 * Below, called external to this class, and so not in this thread.
	 * These need to be, therefore, threadsafe.
	 * 
	 * Just setting a variable (like mPlaying) should be fine.
	 */

	public void startPlaying() {
		mPlaying = true;
		for (int ii = 0; ii < 1/*numTracks*/; ii++) {
			if (mTracks[ii] != null) {
				mTracks[ii].play();
				Log.d(TAG, "track"+ii+" play() called");
			}
		}
		
	}
	public void stopPlaying() {
		mPlaying = false;
		for (int ii = 0; ii < 1/*numTracks*/; ii++) {
			if (mTracks[ii] != null) {
				mTracks[ii].pause();
				Log.d(TAG, "track"+ii+" pause() called");
			}
		}
	}
	
	public void updateSignal(int which, float newSig) {
		if (which > numTracks) {
			throw new IllegalArgumentException(
					"attempted to update signal "+which +
					" but numTracks="+numTracks+".");
		}
		if (newSig > 1.0f)
			mSigs[which] = 1.0f;
		else if (newSig < 0f)
			mSigs[which] = 0f;
		else
			mSigs[which] = newSig;
		
		//Log.v(TAG, "new mSig1="+mSig1);
	}
	
	public void updateAmplitude(int which, float newAmp) {
		if (which > numTracks) {
			throw new IllegalArgumentException(
					"attempted to update signal "+which +
					" but numTracks="+numTracks+".");
		}
		if (newAmp > 1.0f)
			mAmplitude[which] = 1.0f;
		else if (newAmp < 0f)
			mAmplitude[which] = 0f;
		else
			mAmplitude[which] = newAmp;
		
		//Log.v(TAG, "new mSig1="+mSig1);
	}
	
	public void setLo(int which, int freq) {
		if (which > numTracks) {
			throw new IllegalArgumentException(
					"attempted to set freq for track "+which +
					" but numTracks="+numTracks+".");
		}
		
		mFM[which].setLo(freq);
	}
	public void setHi(int which, int freq) {
		if (which > numTracks) {
			throw new IllegalArgumentException(
					"attempted to set freq for track "+which +
					" but numTracks="+numTracks+".");
		}
		
		mFM[which].setHi(freq);
	}
	
	public void end() {
		mAlive = false;
	}
}
