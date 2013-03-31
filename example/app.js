// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.

// open a single window
var window = Ti.UI.createWindow({
	backgroundColor : 'white'
});
var label = Ti.UI.createLabel({
	text : "TTS module!"
});
var speakbutton = Ti.UI.createButton({
	title : 'SPEAK',
	height : '7%',
	width : '35%',
	top : '50%',
	left : '10%'
});

var settingbutton = Ti.UI.createButton({
	title : 'setting',
	height : '7%',
	width : '35%',
	top : '50%',
	left : '55%'
});
var enginespicker = Ti.UI.createPicker({
	height : '10%',
	width : '90%',
	top : '5%',
	left : '5%'
});

var voicespicker = Ti.UI.createPicker({
	height : '10%',
	width : '90%',
	top : '25%',
	left : '5%'
});
speakbutton.enabled = false;

window.add(enginespicker);
window.add(voicespicker);
window.add(speakbutton);
window.add(settingbutton);

function clearPicker(picker) {
	for (var i = 0, l = picker.columns.length; i < l; ++i) {
		var _col = picker.columns[0];
		if(_col)　{
			var len = _col.rowCount;
			for( var x = len - 1; x >= 0; x-- ) {
			　　　var _row = _col.rows[x];
			　　　_col.removeRow(_row);
			}
			//picker.reloadColumn(_col);
		}
	}
}

var tts = require('jp.isisredirect.tts');
Ti.API.info("module is => " + tts);

// step 1 obtain list of TTS Engines installed in Android device 
var engines = tts.getEngines();
var pickerdata = [];
var lastselectedpackagename = null; 
for (var key in engines) {
	if (lastselectedpackagename == null) {
		lastselectedpackagename = engines[key];
	}
	pickerdata.push(Ti.UI.createPickerRow({title:key, packagename:engines[key]}));
}
enginespicker.add(pickerdata);
enginespicker.selectionIndicator = true;

// step 2 send checkTTS message to obtain voice list by package name of TTS Engine asynchronously
tts.checkTTS(lastselectedpackagename);

enginespicker.addEventListener('change', function(e) {
	Ti.API.debug("enginespicker change");
	speakbutton.enabled = false;
	clearPicker(voicespicker);
	lastselectedpackagename = e.row.packagename;
	tts.checkTTS(e.row.packagename);
});

// step 3 reveive TTS_CHKOK event with voice list and send message to create instance of TTS Engine
tts.addEventListener(tts.TTS_CHKOK, function(e) {
	Ti.API.debug(tts.TTS_CHKOK + "voices :" + e.voices);
	var pickerdata = [];
	for (var i = 0, l = e.voices.length; i < l; ++i) {
		pickerdata.push(Ti.UI.createPickerRow({title:e.voices[i], packagename:lastselectedpackagename}));
	}
	voicespicker.add(pickerdata);
	voicespicker.selectionIndicator = true;
	
	tts.initTTS(lastselectedpackagename);
});

// step 4 receive TTS_INITOK event that means TTS Engine is initialized ready to speak.
tts.addEventListener(tts.TTS_INITOK, function(e) {
	Ti.API.debug("tts engine is initialized");
	speakbutton.enabled = true;
	
	var lang = tts.getLanguage().toString();
	lang = lang.replace("/_/g", "-").toLowerCase();
	var rows =  voicespicker.getColumns()[0].rows;
	var f = -1;
	for (var i = 0, l = rows.length; i < l; ++i) {
		var checklang = rows[i]["tilte"];
		Ti.API.debug("TTS_INITOK columns[0].rows["+i+"].tilte: " + typeof checklang);
		if ((typeof checklang) === "string") {
			checklang = checklang.replace("/_/g", "-").toLowerCase();
			if (checklang == lang) {
				f = i;
				break;
			}
		}
	}
	Ti.API.debug("TTS_INITOK f: " + f);
	if (0 <= f) {
		voicespicker.setSelectedRow(0,f);
	}
});

// step 5 select language and speak
voicespicker.addEventListener('change', function(e) {
	Ti.API.debug("voicespicker change /tts.isInitialized:" + tts.isInitialized);
	if (tts.isInitialized) {
		Ti.API.debug("voicespicker change:" + e.row.title);
		tts.setLanguage(e.row.title);
	}else{
		
	}
});

// step 6 select language and speak
speakbutton.addEventListener('click', function(e) {
	tts.speak("こんにちは　Hello", "Hello");		
});

// step 7 receve TTS_UTTERANCE_COMPLETE to do something optionally
tts.addEventListener(tts.TTS_UTTERANCE_COMPLETE, function(e) {
	if (e.utteranceid == "Hello") {
		tts.speak("よの なか　world");
	}
});

// appendix: open TTS Setting Preferrence 
settingbutton.addEventListener('click', function(e) {
	tts.showTTSSettings();
});

// appendix: error handler
tts.addEventListener(tts.TTS_ERROR, function(e) {

});

// appendix: Android OS start to navigate user for installation of TTS Engine on Market place 
tts.addEventListener(tts.TTS_INSTALL_START, function(e) {

});


window.open();
