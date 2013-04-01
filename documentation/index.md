# *TextToSpeech Module*

## Description

*TextToSpeech Module*

## Requrement

Android min-sdk: Android 2.2 (API Level 8)
Titanium 2.1.0.GA

## Accessing the tts Module

To access this module from JavaScript, you would do the following:

	var tts = require("jp.isisredirect.tts");

The tts variable is a reference to the Module object.	

## Reference

### module methods
#### TTSMODULE.getEngines()
gets list of TTS engines that are intalled in Android device.

##### Parameters

+ void

##### Returns

+ Dict<String, String>


#### TTSMODULE.initTTS(String enginepackangename) : Boolean
initialize TTS engine specified by *enginepackangename*.

You should check in *initok* or *error* event whether the specified TTS engine is actually available.

##### Parameters

+ enginepackangename : String

(optional) the package name of TTS engine to use.

###### default :
use TTS engine set by the device setting.

##### Returns

+ Boolean

true if the package of TTS engine specified by enginepackangename is found, even though TTS engine is not actually available.

allways true if enginepackangename is not specified, even though TTS engine is not actually available. 


#### TTSMODULE.checkTTS(String enginepackangename) : void
requires  the supported languages information of TTS engine specified by *enginepackangename*.

##### Parameters

+ enginepackangename : String

(optional) the package name of TTS engine to use.

###### default :
Android OS shows *TTS engine selection dialog* for user.

checkTTS result (in *checkok* event) is the information of user's selected TTS engine.

##### Returns

+ void

#### TTSMODULE.isSupportedLang(String localeString) : Boolean
check whether the specified language is supported by the current TTS engine.

##### Parameters

+ localeString : String

locale string to check for.

##### Returns

+ Boolean

#### TTSMODULE.setLanguage(String localeString) : Boolean
set language for TTS engine to use.

##### Parameters

+ localeString : String
locale string is one of languages ("en-usa" etc.) that *checkok* event provided.

##### Returns

+ Boolean

#### TTSMODULE.getLanguage() : String
get current language that TTS engine uses.

##### Parameters

+ void : 

##### Returns

+ String

locale string to use, like "en-usa".

#### TTSMODULE.isSpeaking() : Boolean
get whether TTS engine is speeking.

##### Parameters

+ void : 

##### Returns

+ Boolean

true if TTS engine is speaking.

#### TTSMODULE.speak(String text, String utteranceId) : Boolean
get whether TTS engine is speeking.

##### Parameters

+ text : String
text to speak.

+ utteranceId : String

(optional) make TTS engine to fire *utterancecomplete* event when speech is finished.

##### Returns

+ Boolean

#### TTSMODULE.shutdown() : void
make TTS engine instance free.

##### Parameters

+ void 

### Returns

+ void

#### TTSMODULE.showTTSSettings() : void
open TTS Setting of device.

##### Parameters

+ void 

### Returns

+ void



### module properties
#### TTSMODULE.initialized
(read only)

#### TTSMODULE.pitch

#### TTSMODULE.rate



### module events
#### checkok

#### ttsinstallstart


#### error

#### initok

#### utterancecomplete


## Usage
The most simply example of usage of TextToSpeech module is below:

	var tts = require("jp.isisredirect.tts");
	tts.addEventListener(tts.TTS_INITOK, function(e) {
		tts.speak("Hello", "spoken Hello");
	});
	tts.addEventListener(tts.TTS_UTTERANCE_COMPLETE, function(e) {
		if (e.utteranceid == "spoken Hello") {
			tts.speak("world");
		}
	});
	tts.initTTS();


For more deep usage, you see /example/app.js

## Author

Kastumi ISHIDA (isis re-direct) in k.i.office.

isis.redirect4@gmail.com

## License

Copyright (c) 2013 Katsumi ISHIDA. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.