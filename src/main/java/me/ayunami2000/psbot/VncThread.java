package me.ayunami2000.psbot;

import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import com.shinyhut.vernacular.utils.KeySyms;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VncThread implements Runnable{
    public static String ipAndPort="";
    private static Pattern ipPortPattern = Pattern.compile("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|localhost):?([0-9]{1,5})?");
    public static boolean isVnc=false;
    private static VernacularConfig config = new VernacularConfig();
    private static VernacularClient client = new VernacularClient(config);
    public static boolean mouseClicking=false;
    public static boolean dragging=false;
    private static int rendering=0;
    //private static Image vncimg=null;
    static{
        config.setColorDepth(ColorDepth.BPP_24_TRUE);
        config.setErrorListener(e -> {
            PsBot.isPlaying=false;
            isVnc=false;
        });

        config.setScreenUpdateListener(image -> {
            if(rendering>5)return;
            rendering++;
            //vncimg=image;
            int width = image.getWidth(null);
            int height = image.getHeight(null);

            BufferedImage img=ImageToChars.convertImageToBuffered(image);

            String textNbt=ImageToChars.toVncHexBlocks(img,width,height);

            if(PsBot.lineMode==1) {
                String[] trPieces = textNbt.substring(1, textNbt.length() - 1).split(",\"\\\\n\",");
                for (String trPiece : trPieces) PsBot.runCommand("tellraw @a [" + trPiece + "]");
            }else{
                PsBot.runCommand("tellraw @a " + textNbt);
            }
            try {
                Thread.sleep(200);//frame delay
            } catch (InterruptedException e) {}
            rendering--;
        });
    }

    @Override
    public void run() {
        Matcher m = ipPortPattern.matcher(ipAndPort);
        if(!m.find()){
            PsBot.chatMsg("Error: Expected IP:PORT");
            PsBot.isPlaying=false;
            return;
        }
        PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
        PsBot.fixCommandBlocks();
        try {Thread.sleep(500);} catch (InterruptedException e) {}
        mouseClicking=false;
        dragging=false;
        isVnc=true;
        String ip=m.group(1),
                port=m.group(2);
        client.stop();
        dragging=false;
        mouseClicking=false;
        client.start(ip, Integer.parseInt(port));
        try {
            while (PsBot.isPlaying) {
                Thread.sleep(1);
                //if(vncimg!=null) {
                //    int width = vncimg.getWidth(null);
                //    int height = vncimg.getHeight(null);
                //    String textNbt = ImageToChars.toVncHexBlocks(ImageToChars.scaleImage(ImageToChars.convertImageToBuffered(vncimg), PsBot.imgSize), width, height);
                //    PsBot.runCommand("tellraw @a " + textNbt);
                //    /*
                //    String[] textNbt = ImageToChars.toVncHexBlocksParts(ImageToChars.scaleImage(ImageToChars.convertImageToBuffered(vncimg), PsBot.imgSize), width, height);
                //    PsBot.runCommand("tellraw @a " + textNbt[0]);
                //    PsBot.runCommand("tellraw @a " + textNbt[1]);
                //    */
                //}
            }
        }catch(InterruptedException e){}
        isVnc=false;
        client.stop();
    }

    private static int[] numpadKeys=new int[]{65438,65436,65433,65435,65430,65437,65432,65429,65431,65434,65451,65439,65455,65421,65450,65453};

    public static void commandEvent(String s){
        if(s.matches("^m \\d+ \\d+$")){
            String[] xy=s.substring(2).split(" ");
            int x=Integer.parseInt(xy[0]),
                    y=Integer.parseInt(xy[1]);
            client.moveMouse(x,y);
            if(dragging){
                mouseClicking=!mouseClicking;
                client.updateMouseButton(1,mouseClicking);
            }else {
                // Click a mouse button. Buttons are numbered 1 - 3
                client.click(1);
            }
        }else if(s.matches("^t .*$")){
            //client.type(s.substring(2));//scared!!
        }else if(s.matches("^c .*$")){
            /*
            try{
                int key=Integer.parseInt(s.substring(2));
                if(key>=0&&key<numpadKeys.length){
                    client.updateKey(numpadKeys[key],true);
                    client.updateKey(numpadKeys[key],false);
                }
            }catch(NumberFormatException e){}
            */
        }else if(s.matches("^d$")){
            dragging=!dragging;
            mouseClicking=false;
        }
    }
}
