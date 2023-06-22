package me.ayunami2000.psbot;

import java.util.ArrayList;

public class SubtitleThread implements Runnable{
    public static String subtitle="";
    public static boolean alreadySubbing=false;
    public static String itemSub="\\n";

    @Override
    public void run() {
        if(subtitle.equals("")){
            PsBot.chatMsg("Error: Please specify the subtitle file!");
            PsBot.isPlaying=false;
        }else{
            try {
                if (PsBot.renderMode == 3 || PsBot.renderMode == 4 || PsBot.renderMode == 5 || PsBot.renderMode == 12) {
                    itemSub=",'[\"\"]','[\"\"]'";
                } else {
                    itemSub="\\n";
                }
                String[] parsedSubtitle=parseSubtitle();
                if((PsBot.audioMode==0||PsBot.audioMode==1)&&PsBot.renderModeUsesCommandBlocks()){
                    PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
                    PsBot.fixCommandBlocks();
                }
                //wait 3 seconds
                //PsBot.chatMsg("Subtitles will start in 3 seconds!");
                //Thread.sleep(3000);
                PsBot.chatMsg("Subtitles starting!");
                if(PsBot.autoSeek)PsBot.seek=0;
                String justDidThis="";
                int justDidTimer=0;
                int loopRate=(int)(1000/PsBot.fps);//adjust as needed for speed
                //long startTime=Instant.now().toEpochMilli();
                //todo: get length of subtitle file and stop accordingly
                while(alreadySubbing){
                    //Instant.now().toEpochMilli()-startTime
                    /*long nowTime=Instant.now().toEpochMilli();
                    long origStartTime=startTime;
                    while(PsBot.paused&&alreadySubbing){
                        //push start time to compensate for pause
                        startTime=origStartTime+(Instant.now().toEpochMilli()-nowTime);
                        Thread.sleep(100);
                    }*/
                    while(PsBot.paused&&alreadySubbing){
                        Thread.sleep(100);
                    }
                    if(!alreadySubbing)break;
                    String[] subsNow=subsAtThisTime(parsedSubtitle,(long)PsBot.seek);
                    String allSub=String.join("\n\n",subsNow);
                    if(allSub.equals(justDidThis)&&justDidTimer<1000/loopRate){
                        if(PsBot.audioMode==0||PsBot.audioMode==1&&(PsBot.renderMode==2||PsBot.renderMode==6||PsBot.renderMode==7||PsBot.renderMode==8||!PsBot.isPlaying))justDidTimer++;
                    }else{
                        if(PsBot.subtitleMode==1){
                            String msmsmsmsmsg=allSub.trim().replace("\n", " ");
                            if(!msmsmsmsmsg.equals(""))PsBot.sendChatOrCommand(msmsmsmsmsg);
                        }else if(PsBot.audioMode!=0&&PsBot.audioMode!=1){
                            PsBot.sendChatOrCommand("/d chicken setGlowing setNoGravity "+(allSub.trim().equals("")?"":("setCustomNameVisible setCustomName \""+allSub.replace("\n"," ")+"\"")));
                        }else if(PsBot.renderMode==2||PsBot.renderMode==6||PsBot.renderMode==7||PsBot.renderMode==8||!PsBot.isPlaying){
                            PsBot.runCommand("/title @a actionbar [\"" + allSub.replace("\n"," ") + "\"]");
                        }else {
                            if (PsBot.renderMode == 3 || PsBot.renderMode == 4 || PsBot.renderMode == 5 || PsBot.renderMode == 12) {
                                itemSub = itemSubtitle(allSub);//todo: preproccess?? maybe??
                            } else {
                                String finalSub = "";
                                for (String s : subsNow) {
                                    int lind = s.lastIndexOf("§");
                                    String appendme = lind != -1 ? s.substring(lind, lind + 2) : "";
                                    finalSub += s + "\n" + (appendme.equals("§r") ? "" : appendme);
                                }
                                if (finalSub.trim().equals("")) finalSub = "\n";
                                if (!finalSub.contains("\n")) finalSub += "\n\n";
                                if(finalSub.indexOf("\n")==finalSub.lastIndexOf("\n"))finalSub+="\n";
                                finalSub = finalSub.replace("\n", "\\n");
                                itemSub = finalSub;
                            }
                        }
                        justDidTimer=0;
                    }
                    justDidThis=allSub;
                    Thread.sleep(loopRate);
                }
            } catch (InterruptedException e) {
                alreadySubbing=false;
            }
        }
    }

    private String itemSubtitle(String currentSub){
        if(currentSub.trim().equals(""))return ",'[\"\"]','[\"\"]'";
        if(!currentSub.contains("\n"))currentSub+="\n";
        currentSub=currentSub.replace("'","\\'");
        currentSub=currentSub.replace("\\\"","\\\\\"");
        String outSub=",'[{\"italic\":\"false\",\"text\":\"";
        currentSub=currentSub.replaceAll("<i>","\"},{\"text\":\"");
        currentSub=currentSub.replaceAll("<b>","\"},{\"italic\":\"false\",\"bold\":\"true\",\"text\":\"");
        currentSub=currentSub.replaceAll("<u>","\"},{\"italic\":\"false\",\"underline\":\"true\",\"text\":\"");
        //not perfect bc no layered shit but ehh dont care didnt ask
        currentSub=currentSub.replaceAll("</[biu]>","\"},{\"italic\":\"false\",\"text\":\"");
        outSub=outSub.replaceAll(",\\{\"text\":\"\"}","").replaceAll("\\{\"text\":\"\"},","").replaceAll(",\\{\"italic\":\"false\",\"text\":\"\"}","").replaceAll("\\{\"italic\":\"false\",\"text\":\"\"},","").replaceAll(",\\{\"italic\":\"false\",\"bold\":\"true\",\"text\":\"\"}","").replaceAll("\\{\"italic\":\"false\",\"bold\":\"true\",\"text\":\"\"},","").replaceAll(",\\{\"italic\":\"false\",\"underline\":\"true\",\"text\":\"\"}","").replaceAll("\\{\"italic\":\"false\",\"underline\":\"true\",\"text\":\"\"},","");
        outSub=outSub.replaceAll("\\{\"text\":\"(.+?)\"}","\"$1\"");
        //0=normal
        //1=italic
        //2=bold
        //3=underline
        int[] colorings=new int[]{currentSub.lastIndexOf("{\"italic\":\"false\",\"text\":\""),currentSub.lastIndexOf("{\"text\":\""),currentSub.lastIndexOf("{\"italic\":\"false\",\"bold\":\"true\",\"text\":\""),currentSub.lastIndexOf("{\"italic\":\"false\",\"underline\":\"true\",\"text\":\"")};
        int lastColoringValue=-1;
        int lastColoring=0;
        for (int i = 0; i < colorings.length; i++) {
            if(colorings[i]!=-1)lastColoringValue=Math.max(lastColoringValue,colorings[i]);
            if(lastColoringValue==colorings[i])lastColoring=i;
        }
        String[] coloringStrings=new String[]{"\"italic\":\"false\",","","\"italic\":\"false\",\"bold\":\"true\",","\"italic\":\"false\",\"underline\":\"true\","};
        outSub+=currentSub.replaceAll("\n","\"}]','[{"+coloringStrings[lastColoring]+"\"text\":\"");
        outSub+="\"}]'";
        outSub=outSub.replaceAll(",'\\[\\{(\"italic\":\"false\",)?(\"underline\":\"true\",)?(\"bold\":\"true\",)?\"text\":\"\"}]'",",'[\"\"]'");
        return outSub;
    }

    private String[] parseSubtitle(){
        if(PsBot.audioToTextThread.enabled)return new String[]{};
        String[] subbysub=subtitle.trim().split("\n\n");
        for(int i=0;i<subbysub.length;i++){
            subbysub[i]=subbysub[i].substring(subbysub[i].indexOf('\n')+1).replaceFirst("\n","~");
            String[] times=subbysub[i].substring(0,subbysub[i].indexOf('~')).split(" --> ");
            subbysub[i]=timeToMs(times[0])+" "+timeToMs(times[1])+subbysub[i].substring(subbysub[i].indexOf('~')).replaceAll("§","&").replaceAll("(?i)<(b|strong)>","<b>").replaceAll("(?i)<(i|em)>","<i>").replaceAll("(?i)<(u|ins)>","<u>").replaceAll("(?i)</(b|strong)>","</b>").replaceAll("(?i)</(i|em)>","</i>").replaceAll("(?i)</(u|ins)>","</u>").replace("\\","\\\\").replace("\"","\\\"");
            //currently only creative mode packet based item lore is supported other than tellraw
            if(PsBot.renderMode!=3&&PsBot.renderMode!=4&&PsBot.renderMode!=5&&PsBot.renderMode!=12)subbysub[i]=subbysub[i].replaceAll("<b>","§l").replaceAll("<i>","§o").replaceAll("<u>","§n").replaceAll("</.+>","§r");
        }
        return subbysub;
    }

    private long timeToMs(String in) {
        String[] split1 = in.split(":");
        long hours = Long.parseLong(split1[0].trim());
        long minutes = Long.parseLong(split1[1].trim());
        long seconds = 0;
        long millies = 0;
        if (split1.length > 2) {
            String[] split = split1[2].split(",");//SRT format
            seconds = Long.parseLong(split[0].trim());
            if (split.length > 1) {
                millies = Long.parseLong(split[1].trim());
            }
        }
        return hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000 + millies;
    }

    private String[] subsAtThisTime(String[] subs,long ms){
        if(PsBot.audioToTextThread.enabled)return new String[]{PsBot.audioToTextThread.currentText};
        ArrayList<String> ar = new ArrayList<String>();
        for (String sub:subs){
            String[] pieces=sub.split("~");
            String[] timesString=pieces[0].split(" ");
            if(Long.parseLong(timesString[0])<=ms&&ms<=Long.parseLong(timesString[1]))ar.add(pieces[1]);
        }
        return ar.toArray(new String[0]);
    }
}