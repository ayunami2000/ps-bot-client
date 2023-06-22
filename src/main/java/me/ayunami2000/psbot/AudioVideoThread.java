package me.ayunami2000.psbot;

import com.github.sarxos.webcam.Webcam;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.concurrent.TimeoutException;

public class AudioVideoThread implements Runnable{
    public static String devices="";

    @Override
    public void run() {
        if(devices.equals("")){
            try {
                PsBot.chatMsg("Webcams:\n#~ screen");
                int ind=0;
                for(Webcam webcam : Webcam.getWebcams(5000)){
                    PsBot.chatMsg(ind+"~ "+webcam.getName());
                    ind++;
                }
                if(ind==0)PsBot.chatMsg("(there were no webcams...)");
            } catch (TimeoutException e) {
                PsBot.chatMsg("Error: No webcams found!");
            }
            PsBot.chatMsg("Audio Devices:");
            int ind=0;
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
                PsBot.chatMsg(ind+"~ "+mixerInfo.getName());
                ind++;
            }
            if(ind==0)PsBot.chatMsg("(there were no audio devices...)");
            PsBot.isPlaying=false;
            return;
        }
        if(!devices.contains("|")){
            PsBot.chatMsg("Error: Please specify video and audio device as: videodevice | audiodevice (double pipe symbols to use them)");
            PsBot.isPlaying=false;
            return;
        }
        devices=devices.replaceAll("\\|\\|","\n");
        String[] deviceSpl=devices.split("\\|",2);
        deviceSpl[0]=deviceSpl[0].replaceAll("\n","|").trim();
        deviceSpl[1]=deviceSpl[1].replaceAll("\n","|").trim();
        PsBot.streamThread.webcamName=deviceSpl[0];
        PsBot.audioThread.deviceName=deviceSpl[1];
        (new Thread(PsBot.streamThread)).start();
        (new Thread(PsBot.audioThread)).start();
    }
}
