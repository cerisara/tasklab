package fr.xtof.tasklab;

import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;

public class TTS {
    private TextToSpeech tts = null;

    public TTS() {
        tts = new TextToSpeech(TaskLabAct.main, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                initok();
            }
        });
    }

    public void endTTS() {
        tts.shutdown();
    }

    private void initok() {
        tts.speak("je voudrais tester la synthèse de la parole, avec ou sans virgules !",
                TextToSpeech.QUEUE_ADD, null, "sentid1");
    }
}

