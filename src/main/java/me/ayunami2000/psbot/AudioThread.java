package me.ayunami2000.psbot;

import me.ayunami2000.psbot.AudioParser.Analysis.Analysis;
import net.minecraft.util.math.BlockPos;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.HashMap;

public class AudioThread implements Runnable{
    public static String deviceName="";
    public static int bpmTypeNumber=4096;
    private static String[] instruments=new String[]{"harp","basedrum","snare","hat","bass","flute","bell","guitar","chime","xylophone","iron_xylophone","cow_bell","didgeridoo","bit","banjo","pling"};
    private static HashMap<Integer, HashMap<Integer, BlockPos>> instrNoteToBlock=null;

    @Override
    public void run() {
        if(deviceName.equals("")){
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
        PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);

        if(PsBot.audioMode==0||PsBot.audioMode==1){
            PsBot.fixCommandBlocks();
        }else{
            //setup noteblock area (same every time because limited to midi piano (noteblock 3 instruments or smthn) ONLY
            instrNoteToBlock=new HashMap<>();
            String theSong="";
            for(int i=0;i<128;i++){
                int[] midiToNote = MidiConverter.noteConv(0,i);
                if(midiToNote[0]!=-1) {
                    int noteToGame = (midiToNote[1] - MidiConverter.instrument_offsets[midiToNote[0]]) + midiToNote[0] * 25;
                    int instr = (int) Math.floor(noteToGame / 25);
                    int note = noteToGame % 25;
                    theSong += i + ":" + note + ":" + instr + "\n";
                }
            }
            theSong=theSong.substring(0,theSong.length()-1);
            String[] songLines=theSong.split("\n");
            instrNoteToBlock=NoteblockPlayer.songLinesToBlocks(songLines);
            NoteblockPlayer.buildIt(instrNoteToBlock);
        }
        try {
            PsBot.pitchDetection.beginPitchDetection();
            while(PsBot.isPlaying){
                Thread.sleep(1);
            }
            PsBot.pitchDetection.endPitchDetection();
        } catch (Exception e) {
            e.printStackTrace();
            PsBot.isPlaying=false;
        }
    }

    public static void pitchDetect(Analysis analysis){
        if(analysis.maximum<1)return;
        double volumeToUsable = 12.0 * (Math.log(analysis.maximum / 440.0f) / Math.log(2)) + 69.0;
        for (Double f0 : analysis.klapuri.f0s) {
            //440 Hz as the pitch of A4
            double pitchToMidi = 12.0 * (Math.log(f0 / 440.0f) / Math.log(2)) + 69.0;
            int[] midiToNote = MidiConverter.noteConv(0,(int)pitchToMidi);
            int noteToGame = (midiToNote[1]-MidiConverter.instrument_offsets[midiToNote[0]]) + midiToNote[0]*25;
            if(PsBot.audioMode==0||PsBot.audioMode==1){
                PsBot.runCommand("execute at @a run playsound block.note_block."+instruments[(int)Math.floor(noteToGame / 25)]+" record @p ~ ~ ~ "+(volumeToUsable/127.0)+" "+(.5*(Math.pow(2,((double)(noteToGame%25))/12.0))));
            }else{
                NoteblockPlayer.playIt(instrNoteToBlock,(int)Math.floor(noteToGame / 25),noteToGame%25);
            }
        }
    }
}
