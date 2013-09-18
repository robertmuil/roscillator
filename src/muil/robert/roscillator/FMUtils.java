/**
 * 
 */
package muil.robert.roscillator;

/**
 * @author robert
 *
 * This is a set of helper functions for producing frequency-modulation.
 * 
 * 'Signal' is envelope of FM and is defined as 0-1, going from fLo to fHi.
 * 
 */
public class FMUtils {
	private static final float FullCircle = (float)(Math.PI*2d);
	
	private int sRate;
	private float fHi, fLo;
	private float mAngle, mInc;
	
	FMUtils (float freqLo, float freqHi, int sampleRate) {
		fHi = freqHi;
		fLo = freqLo;
		sRate = sampleRate;
		mAngle = mInc = 0;
	}

	public void setLo(float freq) {
		fLo = freq;
	}
	public void setHi(float freq) {
		fHi = freq;
	}
	public void setFreqRange(float freqLo, float freqHi) {
		fHi = freqHi;
		fLo = freqLo;
	}

	public void setFreqRange(int[] range) {
		fLo = range[0];
		fHi = range[1];
	}

	
	public static float calcInc(float freq, int sampleRate) {
		return FullCircle * freq / sampleRate;
	}
	
	/**
	 * Signal is from 0 to 1.0, and dictates the frequency between fHi and fLo.
	 */
	public void updateSignal(float signal) {
		mInc = calcInc(fLo + ((fHi - fLo) * signal), sRate);
	}
	
	/**
	 * 
	 * @return the updated angle for frequency modulation.
	 */
	public float updateAngle() {
		mAngle += mInc;
		if (mAngle >= FullCircle)
			mAngle -= FullCircle;
		
		return mAngle;
	}

}
