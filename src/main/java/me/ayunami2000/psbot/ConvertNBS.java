package me.ayunami2000.psbot;

import me.ayunami2000.psbot.nbsapi.Layer;
import me.ayunami2000.psbot.nbsapi.Note;
import me.ayunami2000.psbot.nbsapi.Song;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ConvertNBS {
    private static final int[] nb2in=new int[]{0,4,1,2,3,7,5,6,8,9,10,11,12,13,14,15};
    private static final int[] in2old=new int[]{0,1,2,3,4,0,0,4,0,0,0,0,4,0,4,0};
    public static void doConvert(){
        File nbsFile=PsBot.chooseFile(new String[]{"nbs","mid","midi"},"Note Block Studio Files OR Midi Files");
        if(nbsFile!=null&&nbsFile.exists()&&!nbsFile.isDirectory()) {
            String resStr=nbsFile.getName().toLowerCase(Locale.ROOT).endsWith(".nbs")?doLiveConvert(nbsFile):MidiConverter.midiToTxt(nbsFile);
            if (resStr==null||resStr.equals("")) {
                PsBot.chatMsg("There was an error while converting your file.");
            } else {
                PsBot.chatMsg("File converted successfully!");
                File saveFile=PsBot.saveFile("txt","WWE/Inertia/Bleachhack/FabricNotebot Notebot Format");
                if(saveFile!=null){
                    try {
                        saveFile.createNewFile();
                        FileWriter myWriter = new FileWriter(saveFile);
                        myWriter.write(resStr);
                        myWriter.close();
                        PsBot.chatMsg("File saved successfully!");
                    }catch(Exception e){
                        PsBot.chatMsg("There was an error while saving your file.");
                    }
                };
            }
        }else{
            PsBot.chatMsg("There was an error while finding your file.");
        }
    }
    public static String doLiveConvert(File nbsFile){
        try{
            String resSongFile="";
            Map<Integer,ArrayList<String>> songLines=new HashMap<>();
            Song nbsSong = new Song(nbsFile);
            List<Layer> nbsSongBoard = nbsSong.getSongBoard();
            for (int i = 0; i < nbsSongBoard.size(); i++) {
                Layer layer=nbsSongBoard.get(i);
                HashMap<Integer, Note> noteList = layer.getNoteList();
                for (Map.Entry note : noteList.entrySet()) {
                    Note noteInfo = (Note) note.getValue();
                    Integer noteKey=(int)((double)(int)note.getKey()/(5.0*((double)nbsSong.getTempo()/10000.0)));
                    if(!songLines.containsKey(noteKey))songLines.put(noteKey,new ArrayList<>());
                    ArrayList<String> tickLines=songLines.get(noteKey);
                    //keep notes within 2-octave range
                    Integer notePitch=Math.max(33,Math.min(57,noteInfo.getPitch()))-33;
                    int instrId=noteInfo.getInstrument().getID();
                    if(instrId!=-1){
                        instrId=nb2in[instrId];
                        if (PsBot.audioMode == 1) instrId = in2old[instrId];
                    }
                    tickLines.add(noteKey + ":" + notePitch + ":" + instrId + ":" + ((int)(127.0*(layer.getVolume()*noteInfo.getVelocity())/10000.0)) + ":" + noteInfo.getPanning() + ":" + noteInfo.getPrecisePitch() + "\n");
                    songLines.put(noteKey,tickLines);
                }
            }
            SortedSet<Integer> ticks = new TreeSet<>(songLines.keySet());
            for (Integer tick : ticks) {
                ArrayList<String> tickLines = songLines.get(tick);
                for(int i=0;i<tickLines.size();i++){
                    resSongFile+=tickLines.get(i);
                }
            }
            if(resSongFile.endsWith("\n"))resSongFile=resSongFile.substring(0,resSongFile.length()-1);
            return resSongFile;
        }catch(Exception e){
            e.printStackTrace();
            PsBot.chatMsg("There was an error while converting your NBS.");
            return null;
        }
    }
}
