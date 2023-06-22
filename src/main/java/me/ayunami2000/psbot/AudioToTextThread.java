package me.ayunami2000.psbot;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.IOException;
import javax.sound.sampled.*;

public class AudioToTextThread implements Runnable{
    public static String deviceName="";
    public static boolean showPartial=false;
    private static Model model=null;
    private static Recognizer recognizer=null;
    public static boolean enabled=false;
    public static String currentText="";

    static {
        try {
            model = new Model(PsBot.mc.runDirectory.toPath().normalize().resolve("vosk/vosk-model-en-us-0.22/").toString());
            recognizer = new Recognizer(model, 120000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 44100, false);
    private static DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

    @Override
    public void run() {
        if(model==null||recognizer==null){
            PsBot.chatMsg("Error: The model or recognizer is not initialized!");
            enabled=false;
            return;
        }
        if(deviceName.equals("")){
            PsBot.chatMsg("Audio Devices:");
            int ind=0;
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
                PsBot.chatMsg(ind+"~ "+mixerInfo.getName());
                ind++;
            }
            if(ind==0)PsBot.chatMsg("(there were no audio devices...)");
            enabled=false;
            return;
        }

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        Mixer mixer = null;
        for (int i = 0; i < mixerInfos.length; i++) {
            if (mixerInfos[i].getName().trim().equals(deviceName)) {
                mixer = AudioSystem.getMixer(mixerInfos[i]);
                break;
            }
        }
        if(mixer==null){
            for (int i = 0; i < mixerInfos.length; i++) {
                try {
                    int index = Integer.parseInt(deviceName);
                    if (index==i) {
                        mixer = AudioSystem.getMixer(mixerInfos[i]);
                        break;
                    }
                }catch(NumberFormatException e){
                    break;
                }
            }
        }
        if(mixer==null){
            PsBot.chatMsg("Error: Device not found!");
            enabled=false;
            return;
        }

        currentText="";
        if(!PsBot.subtitleThread.alreadySubbing) {
            PsBot.subtitleThread.alreadySubbing = true;
            PsBot.subtitleThread.subtitle = " ";//trol
            (new Thread(PsBot.subtitleThread)).start();
        }

        TargetDataLine line;

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            int numBytesRead;
            int CHUNK_SIZE = 1024;

            byte[] b = new byte[4096];

            String lastMsg="";

            while (enabled) {
                numBytesRead = line.read(b, 0, CHUNK_SIZE);

                if (recognizer.acceptWaveForm(b, numBytesRead)) {
                    if(!showPartial) {
                        String currMsg = recognizer.getResult().substring(14);
                        currMsg = currMsg.substring(0, currMsg.length() - 3);
                        showText(currMsg);
                    }else{
                        recognizer.getResult();
                    }
                } else {
                    if(showPartial) {
                        String currMsg = recognizer.getPartialResult().substring(17);
                        currMsg = currMsg.substring(0, currMsg.length() - 3);
                        if(!lastMsg.equals(currMsg))showText(currMsg);
                        lastMsg=currMsg;
                    }else{
                        recognizer.getPartialResult();
                    }
                }
            }
            line.close();
            enabled=false;
        } catch (Exception e) {
            e.printStackTrace();
            enabled=false;
        }
    }

    private static void showText(String msg){
        //if you turn on subtitle thread itll use it's handler instead (defaults turning it on)
        if(PsBot.subtitleThread.alreadySubbing){
            currentText=msg;
        }else{
            if(!msg.trim().equals(""))PsBot.sendChatOrCommand(msg);
        }
    }
}