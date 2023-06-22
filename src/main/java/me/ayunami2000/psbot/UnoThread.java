package me.ayunami2000.psbot;

import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.game.UnoWinner;
import com.github.markozajc.juno.players.UnoPlayer;
import me.ayunami2000.psbot.uno.UnoChatGame;
import me.ayunami2000.psbot.uno.UnoStrings;

public class UnoThread implements Runnable{
    public static boolean isPlayingUno=false;
    public static String command="";
    private static UnoGame game = null;
    public static boolean basicMode=false;
    public static String[] messages=UnoStrings.dynamic;

    @Override
    public void run() {
        PsBot.unoThread.isPlayingUno=true;
        basicMode=command.equalsIgnoreCase("basic");
        command="";
        messages=basicMode?UnoStrings.basic:UnoStrings.dynamic;
        unoMessage(getMessage(0));
        try{Thread.sleep(1000);}catch(InterruptedException e){}
        command="";
        game = new UnoChatGame();

        UnoWinner winner = game.play();
        UnoPlayer winnerPlayer = winner.getWinner();
        if (winnerPlayer == null) {
            unoMessage(getMessage(1));

        } else {
            unoMessage(getMessage(2,winnerPlayer.getName()));
        }
        unoMessage(getMessage(3));
        switch (winner.getEndReason()) {
            case REQUESTED:
                unoMessage(getMessage(4));
                break;
            case FALLBACK:
                unoMessage(getMessage(5));
                break;
            case VICTORY:
                unoMessage(getMessage(6));
                break;
            case UNKNOWN:
                //unoMessage("this shouldn't have happened! Please send a log of the game to https://github.com/markozajc/JUNO/issues.");
                break;
        }
        PsBot.unoThread.isPlayingUno=false;
        PsBot.isPlaying=false;
    }

    public static String readCommand(){
        String cmd=command;
        command="";
        return cmd;
    }

    public static void unoMessage(String s){
        PsBot.sendChatOrCommand(getMessage(7)+s.replaceAll("[^a-zA-Z0-9.,'\"\\[\\]:;{}!@#$%^&*()=+|\\\\<>/?~` _-]",""));
        try{Thread.sleep(200);}catch(InterruptedException e){}
    }

    public static void endGame(){
        if(game!=null)game.endGame();
        PsBot.unoThread.isPlayingUno=false;
        PsBot.isPlaying=false;
    }

    public static String getMessage(int index,Object... objects){
        return String.format(messages[index],objects);
    }

    public static String getMessage(int index){
        return messages[index];
    }
}
