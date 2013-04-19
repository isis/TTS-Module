/*
Copyright (c) 2013 Katsumi ISHIDA. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to deal in the 
Software without restriction, including without limitation the rights to use, copy, 
modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
and to permit persons to whom the Software is furnished to do so, subject to the 
following conditions:

The above copyright notice and this permission notice shall be included in all copies
 or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 OR OTHER DEALINGS IN THE SOFTWARE.
 */
package jp.isisredirect.tts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.TextUtils;
import android.os.Build;


@Kroll.module(name="Tts", id="jp.isisredirect.tts")
public class TtsModule extends KrollModule implements OnInitListener, OnUtteranceCompletedListener, TiActivityResultHandler
{
	private static final String LCAT = "TtsModule";
	private static final boolean DBG = TiConfig.LOGD;
	
	@Kroll.constant
	public static final String TTS_ERROR = "error";
	@Kroll.constant
	public static final String TTS_CHKOK = "checkok";
	@Kroll.constant
	public static final String TTS_INITOK = "initok";
	@Kroll.constant
	public static final String TTS_INSTALL_START = "ttsinstallstart";
	@Kroll.constant
	public static final String TTS_UTTERANCE_ID = "utteranceid";
	@Kroll.constant
	public static final String TTS_UTTERANCE_COMPLETE = "utterancecomplete";
	@Kroll.constant
	public static final String TTS_MESSAGE = "message";
	@Kroll.constant
	public static final String TTS_VOICES = "voices";

	private boolean initialized = false;
	private TextToSpeech tts;
	private float pitch = 1.0f;
	private float rate = 1.0f;
	
	private static final int CHECK_TTS = 60007;
	
	public TtsModule() {
		super();
	}
	
	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		if (DBG) Log.d(LCAT, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	@Override
	public void onStart(Activity activity) {
		super.onStart(activity);
	}

	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
	}

	@Override
	public void onPause(Activity activity) {
		super.onPause(activity);
	}

	@Override
	public void onStop(Activity activity) {
		super.onStop(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		super.onDestroy(activity);
		shutdown();
	}

	
	@Override
	public void onInit(int status) {
		if (DBG) Log.d(LCAT, "onInit " + status);
		if(status == TextToSpeech.SUCCESS) {
			initialized = true;
			KrollDict data = new KrollDict();
			data.put(TiC.EVENT_PROPERTY_SOURCE, TtsModule.this);
			fireEvent(TTS_INITOK, data);
		}else{
			if (DBG) Log.e("TTS", "Init error.");
			KrollDict data = new KrollDict();
			data.put(TiC.EVENT_PROPERTY_SOURCE, TtsModule.this);
			fireEvent(TTS_ERROR, data);
		}
	}

	private static class MyEngineInfo {
		public String name;
		public String label;

		@Override
		public String toString() {
			return "MyEngineInfo{name=" + name + "}";
		}

	}

	private MyEngineInfo getEngineInfo(ResolveInfo resolve, PackageManager pm) {
		ServiceInfo service = resolve.serviceInfo;
		if (service != null) {
			MyEngineInfo engine = new MyEngineInfo();
			engine.name = service.packageName;
			CharSequence label = service.loadLabel(pm);
			engine.label = TextUtils.isEmpty(label) ? engine.name : label
					.toString();
			return engine;
		}
		return null;
	}

	@Kroll.method
	public KrollDict getEngines() {
		KrollDict data = new KrollDict();
		PackageManager pm = TiApplication.getInstance().getPackageManager();
		Intent intent = new Intent(Engine.INTENT_ACTION_TTS_SERVICE);
		List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (resolveInfos == null)
			return data;

		for (ResolveInfo resolveInfo : resolveInfos) {
			MyEngineInfo engine = getEngineInfo(resolveInfo, pm);
			if (engine != null) {
				data.put(engine.label, engine.name);
			}
		}
		return data;
	}

	private boolean isPackageInstalled(String packageName) {
		PackageManager pm = TiApplication.getInstance().getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(packageName, 0);

			return pi != null;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@Kroll.method
	public boolean initTTS(
			@Kroll.argument(optional = true) String enginepackangename) {
		shutdown();
		if (enginepackangename != null) {
			if (DBG) Log.d(LCAT, "initTTS engine:"+enginepackangename);
			if (isPackageInstalled(enginepackangename)) {
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
					tts = new TextToSpeech(TiApplication.getInstance(), this);
					tts.setEngineByPackageName(enginepackangename);
				} else {
					tts = new TextToSpeech(TiApplication.getInstance(), this, enginepackangename);
				}
				return true;
			} else {
				if (DBG) Log.d(LCAT, "initTTS engine not found");
				return false;
			}
		} else {
			tts = new TextToSpeech(TiApplication.getInstance(), this);
			return true;
		}
	}

	@Kroll.method
	public void checkTTS(
			@Kroll.argument(optional = true) String enginepackangename) {
		shutdown();
		TiBaseActivity activity = TiApplication.getInstance().getRootActivity();
		Intent intent;
		if (enginepackangename != null) {
	        intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	        intent.setPackage(enginepackangename);
		}else{
			intent = new Intent();				
		}
		intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		activity.launchActivityForResult(intent, CHECK_TTS, this);
	}

	// implements TiActivityResultHandler
	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		if (CHECK_TTS == requestCode) {
			KrollDict data = new KrollDict();
			data.put(TiC.EVENT_PROPERTY_SOURCE, TtsModule.this);
			data.put(TTS_MESSAGE, e.getLocalizedMessage());
			fireEvent(TTS_ERROR, data);
		}
	}
	
	//implements TiActivityResultHandler
	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (CHECK_TTS == requestCode) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				ArrayList<String> voices= data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
				if (voices != null) {
					if (DBG) Log.d(LCAT, "voices:" + voices.toString());
				}
				KrollDict resultdata = new KrollDict();
				resultdata.put(TiC.EVENT_PROPERTY_SOURCE, TtsModule.this);
				resultdata.put(TTS_VOICES, voices.toArray(new String[voices.size()]));
				fireEvent(TTS_CHKOK, resultdata);
			} else {
				Intent install = new Intent();
				install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				activity.startActivity(install);
				KrollDict resultdata = new KrollDict();
				resultdata.put(TiC.EVENT_PROPERTY_SOURCE, TtsModule.this);
				fireEvent(TTS_INSTALL_START, resultdata);
			}
		}
	}
	
	@Kroll.method
	public boolean isSupportedLang(String localeString)
	{
		if (localeString == null || localeString.isEmpty()) {
			return false;
		}
		if (!initialized) {
			return false;
		}
		Locale locale = makeLocaleByString(localeString);
		return (TextToSpeech.LANG_AVAILABLE <= tts.isLanguageAvailable(locale));			
	}
	
	@Kroll.method
	public boolean setLanguage(String localeString)
	{
		if (localeString == null || localeString.isEmpty()) {
			return false;
		}
		if (!initialized) {
			return false;
		}
		if (isSupportedLang(localeString)) {
			if (DBG) Log.d(LCAT, "setLanguage:" + localeString);
			Locale locale = makeLocaleByString(localeString);
			if (DBG) Log.d(LCAT, "setLanguage getVariant():"+ locale.getVariant());
			switch (tts.setLanguage(locale)) {
			case TextToSpeech.LANG_AVAILABLE:
				if (DBG) Log.d(LCAT, "setLanguage supported LANG_AVAILABLE:" + localeString);
				return true;
			case TextToSpeech.LANG_COUNTRY_AVAILABLE:
				if (DBG) Log.d(LCAT, "setLanguage supported LANG_COUNTRY_AVAILABLE:" + localeString);
				return true;
			case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
				if (DBG) Log.d(LCAT, "setLanguage supported LANG_COUNTRY_VAR_AVAILABLE:" + localeString);
				return true;
			case TextToSpeech.LANG_MISSING_DATA:
			case TextToSpeech.LANG_NOT_SUPPORTED:
			default:
				break;
			}
		}
		if (DBG) Log.d(LCAT, "setLanguage not supported:" + localeString);
		return false;	
	}

	private Locale makeLocaleByString(String localeString) {
		Locale locale;
		String[] elememts = localeString.replaceAll("-", "_").split("_", -1);
		switch (elememts.length) {
		case 3:
			locale = new Locale(elememts[0], elememts[1], elememts[2]);
			break;
		case 2:
			locale = new Locale(elememts[0], elememts[1]);
			break;
		case 1:
		default:
			locale = new Locale(localeString);
			break;
		}
		return locale;
	}
	
	@Kroll.method
	public String getLanguage()
	{
		if (!initialized) {
			return "";
		}
		Locale locale = tts.getLanguage();
		if (DBG) Log.d(LCAT, "getLanguage:" + locale.toString());
		return locale.toString();
	}
		
	@Kroll.method
	public boolean isSpeaking() {
		if (DBG) Log.d(LCAT, "isSpeaking" );
		if (initialized) {
			return tts.isSpeaking();
		}
		return false;
	}
	
	@Kroll.method
	public boolean speak(String text, @Kroll.argument(optional = true) String utteranceId) {
		if (DBG) Log.d(LCAT, "speak " + text);
		if (initialized) {
			if (0 < text.length()) {
				if (tts.setPitch(getPitch()) == TextToSpeech.ERROR) {
					if (DBG) Log.e("TTS", "Ptich(" + getPitch() + ") set error.");
				}
				if (tts.setSpeechRate(getRate()) == TextToSpeech.ERROR) {
					if (DBG) Log.e("TTS", "Speech rate(" + getRate() + ") set error.");
				}
				stopSpeaking();
				tts.setOnUtteranceCompletedListener(this);
				HashMap<String, String> options = new HashMap<String, String>();
				//myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				 //       String.valueOf(AudioManager.STREAM_ALARM));
				options.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
						utteranceId);
				tts.speak(text, TextToSpeech.QUEUE_FLUSH, options);
			}
		}
		return true;
	}
	// implements OnUtteranceCompletedListener
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		KrollDict data = new KrollDict();
		data.put(TiC.EVENT_PROPERTY_SOURCE, TtsModule.this);
		data.put(TTS_UTTERANCE_ID, utteranceId);
		fireEvent(TTS_UTTERANCE_COMPLETE, data);
	}

	@Kroll.method
	public void stopSpeaking() {
		if (DBG) Log.d(LCAT, "stopSpeaking ");
		if (initialized) {
			if (isSpeaking()) {
				tts.stop();
			}
		}
	}

//	@Kroll.method
	@Kroll.getProperty
	public boolean getIsInitialized() {
		return initialized;
	}

	@Kroll.method
	@Kroll.setProperty
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	@Kroll.method
	@Kroll.getProperty
	public float getPitch() {
		return pitch;
	}

	@Kroll.method
	@Kroll.setProperty
	public void setRate(float rate) {
		this.rate = rate;
	}

	@Kroll.method
	@Kroll.getProperty
	public float getRate() {
		return rate;
	}
	
	@Kroll.method
	public void shutdown() {
		if (tts != null) {
			tts.shutdown();
			tts = null;
			initialized = false;
		}
	}

	
	@Kroll.method
	public void showTTSSettings() {
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
			ComponentName componentToLaunch = new ComponentName(
					"com.android.settings",
					"com.android.settings.TextToSpeechSettings");

			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(componentToLaunch);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			intent = new Intent();
			intent.setAction("com.android.settings.TTS_SETTINGS");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		}
		try {
			if (DBG) Log.i(LCAT, "startActivity : TextToSpeechSettings");
			TiApplication.getInstance().getRootActivity().startActivity(intent);
		} catch (ActivityNotFoundException e) {
			if (DBG) Log.e(LCAT, "ActivityNotFoundException");
		}
	}
}

