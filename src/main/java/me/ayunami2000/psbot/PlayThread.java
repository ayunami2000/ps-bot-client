package me.ayunami2000.psbot;

import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class PlayThread implements Runnable{
    public static String theSong="";

    private static String[] instruments=new String[]{"harp","basedrum","snare","hat","bass","flute","bell","guitar","chime","xylophone","iron_xylophone","cow_bell","didgeridoo","bit","banjo","pling"};
    public static int ticks=0;

    @Override
    public void run() {
        try {
            PsBot.chatMsg("Preparing to play...");
            String[] songLines = theSong.split("\n");
            HashMap<Integer,HashMap<Integer,BlockPos>> instrNoteToBlock=null;
            if(PsBot.audioMode==0||PsBot.audioMode==1){
                PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
                PsBot.fixCommandBlocks();
            }else{
                instrNoteToBlock=NoteblockPlayer.songLinesToBlocks(songLines);
                NoteblockPlayer.buildIt(instrNoteToBlock);
            }
            Thread.sleep(500);
            PsBot.chatMsg("Playing!");
            if(PsBot.autoSeek)PsBot.seek=0;
            while(playSong(songLines,instrNoteToBlock)){}
            PsBot.chatMsg("Finished playing!");
            PsBot.isPlaying=false;
        } catch (InterruptedException | NullPointerException e) {
            PsBot.isPlaying=false;
        }
    }
    private static boolean playSong(String[] songLines,HashMap<Integer,HashMap<Integer,BlockPos>> instrNoteToBlock) throws InterruptedException {
        while(PsBot.paused&&PsBot.isPlaying)Thread.sleep(100);
        ticks=(int)(PsBot.seek/50);
        int lastTick=ticks;
        for (String songLine : songLines) {
            if(PsBot.isPlaying) {
                //if autoseek then control song duration itself so nothing gets skipped
                if(!PsBot.autoSeek&&lastTick>ticks){
                    //restart song if changed but keep new value
                    return true;
                }
                if(PsBot.autoSeek||ticks>=(int)(PsBot.seek/50)) {//catch up if needed
                    while (PsBot.paused && PsBot.isPlaying) Thread.sleep(100);
                    String[] songInfo = songLine.split(":");
                    int tick = Integer.parseInt(songInfo[0]);
                    if(tick>=ticks) {
                        int note = Integer.parseInt(songInfo[1]);
                        int instr = Integer.parseInt(songInfo[2]);
                        if(instr!=-1) {
                            int vol = songInfo.length >= 4 ? Integer.parseInt(songInfo[3]) : 127;
                            int panning = songInfo.length >= 5 ? Integer.parseInt(songInfo[4]) : 100;
                            int precisePitch = songInfo.length >= 6 ? Integer.parseInt(songInfo[5]) : 0;
                            if(PsBot.autoSeek) {
                                Thread.sleep((tick - ticks) * 50L);
                                lastTick = tick;
                                PsBot.seek = ticks = tick;
                            }else {
                                int msWaited=0;
                                long msToWait=(tick - ticks) * 50L;
                                while(PsBot.isPlaying&&tick>PsBot.seek/50&&msWaited<msToWait){
                                    Thread.sleep(1);
                                    msWaited++;
                                }
                                lastTick = tick;
                                ticks = (int) (PsBot.seek / 50);
                            }
                            if (PsBot.audioMode == 0||PsBot.audioMode==1) {
                                double[] panpos=new double[]{panning==100?0:((100 - panning) / 50.0),0,0};
                                if(PsBot.filter8d){
                                    double trigval=(instr/(double)instruments.length)+PsBot.seek/8.0;
                                    if(panning==100)panpos[0]=2;
                                    double[] oldpanpos=panpos;
                                    panpos[0]=oldpanpos[0]*Math.cos(trigval);
                                    panpos[1]=oldpanpos[0]*Math.sin(trigval);
                                    panpos[2]=panpos[1];
                                }
                                PsBot.runCommand("execute at @a" + ((PsBot.filter8d||panning!=100)?" positioned ^"+panpos[0]+" ^"+panpos[1]+" ^"+panpos[2]:"") + " run playsound block.note_block." + instruments[instr] + " record @p ~ ~ ~ " + (((double) vol) / 127.0) + " " + Math.max(0.5,Math.min(2.0,(.5 * (Math.pow(2, (note+precisePitch / 100.0) / 12.0))))));
                            } else {
                                NoteblockPlayer.playIt(instrNoteToBlock, instr, note);
                            }
                        }
                    }else{
                        ticks = (int) (PsBot.seek / 50);
                    }
                }else{
                    return true;
                }
            }
        }
        if(PsBot.loop&&PsBot.isPlaying){
            PsBot.seek=0;
            return true;
        }
        return false;
    }
}
