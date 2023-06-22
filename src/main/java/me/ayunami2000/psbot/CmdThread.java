package me.ayunami2000.psbot;

public class CmdThread implements Runnable{
    public static String theCmd="";

    @Override
    public void run() {
        try {
            if(PsBot.audioMode==0||PsBot.audioMode==1){
                PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
                PsBot.fixCommandBlocks();
            }
            PsBot.chatMsg("Running from file...");
            String[] cmdLines = theCmd.split("\n");
            int ticks=0;
            for (String cmd : cmdLines) {
                if(PsBot.isPlaying) {
                    String time = cmd.substring(0,cmd.indexOf(' '));
                    int tick = Integer.parseInt(time);
                    Thread.sleep((tick-ticks)/1000);
                    ticks=tick;
                    String cmdcmd=cmd.substring(cmd.indexOf(' ')+1);
                    if(PsBot.audioMode==0||PsBot.audioMode==1) {
                        PsBot.runCommand(cmdcmd);
                    }else{
						PsBot.sendChatOrCommand(cmdcmd);
                    }
                }
            }
            PsBot.chatMsg("Finished running!");
            PsBot.isPlaying=false;
        } catch (InterruptedException | NullPointerException e) {
            PsBot.isPlaying=false;
        }
    }
}