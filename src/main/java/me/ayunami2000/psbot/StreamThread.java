package me.ayunami2000.psbot;

import com.github.sarxos.webcam.Webcam;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StreamThread implements Runnable{
    public static String webcamName="";

    @Override
    public void run() {
        if(webcamName.equals("")){
            try {
                PsBot.chatMsg("Webcams:\n#~ screen\n/~ file");
                int ind=0;
                for(Webcam webcam : Webcam.getWebcams(5000)){
                    PsBot.chatMsg(ind+"~ "+webcam.getName());
                    ind++;
                }
                if(ind==0)PsBot.chatMsg("(there were no webcams...)");
            } catch (TimeoutException e) {
                PsBot.chatMsg("Error: No webcams found!");
            }
            PsBot.isPlaying=false;
        }else{
            Webcam theWebcam = null;
            if(!(webcamName.equalsIgnoreCase("screen")||webcamName.equalsIgnoreCase("#")||webcamName.equalsIgnoreCase("file")||webcamName.equalsIgnoreCase("/"))) {
                theWebcam = Webcam.getWebcamByName(webcamName);
                if (theWebcam == null) {
                    try {
                        List<Webcam> webcamList = Webcam.getWebcams();
                        theWebcam = webcamList.get(Integer.parseInt(webcamName));
                        if (theWebcam == null) {
                            PsBot.chatMsg("Error: That webcam could not be found!");
                            PsBot.isPlaying = false;
                            return;
                        }
                    } catch (NumberFormatException e) {
                        PsBot.chatMsg("Error: That webcam could not be found!");
                        PsBot.isPlaying = false;
                        return;
                    }
                }
            }
            try {
                if(PsBot.renderModeUsesCommandBlocks()) {
                    PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
                    PsBot.fixCommandBlocks();
                    Thread.sleep(500);
                }
                if(!ImageToChars.preRenderMode()){
                    PsBot.isPlaying=false;
                    return;
                }
                if(webcamName.equalsIgnoreCase("file")||webcamName.equalsIgnoreCase("/")){
                    File videoFile = PsBot.chooseFile(new String[]{"mp4","mkv","webm"}, "Video Files");
                    if(videoFile==null){
                        PsBot.chatMsg("Error: File not found!");
                        PsBot.isPlaying=false;
                        return;
                    }
                    try {
                        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));
                        DemuxerTrackMeta dtm = grab.getVideoTrack().getMeta();
                        int frameNum=dtm.getTotalFrames();
                        double fps=frameNum/dtm.getTotalDuration();
                        AtomicReference<Picture> picture = new AtomicReference<>();
                        long lastTime = Instant.now().toEpochMilli();
                        AtomicInteger frame= new AtomicInteger();
                        if(PsBot.autoSeek)PsBot.seek=0;
                        (new Thread(() -> {
                            long lastTimeThr = Instant.now().toEpochMilli();
                            try {
                                picture.set(grab.getNativeFrame());
                                while (PsBot.isPlaying && picture.get() != null) {
                                    frame.getAndIncrement();
                                    try {
                                        int seekFrame=(int)(PsBot.seek*(fps/1000.0));
                                        if(frame.get()!=seekFrame){
                                            grab.seekToFramePrecise(seekFrame);
                                            frame.set(seekFrame);
                                        }
                                    } catch (JCodecException e) {
                                        //stop!!
                                        PsBot.isPlaying=false;
                                        break;
                                    }
                                    if (PsBot.paused && PsBot.isPlaying) {
                                        lastTimeThr = Instant.now().toEpochMilli();
                                        Thread.sleep(100);
                                    }
                                    if (!PsBot.isPlaying) break;
                                    picture.set(grab.getNativeFrame());
                                    if (picture.get() == null) break;
                                    long nownow = Instant.now().toEpochMilli();
                                    long timeSpent = nownow - lastTimeThr;
                                    lastTimeThr = nownow;
                                    long timeToSleep = 1000L / (long) fps - timeSpent;
                                    if (timeToSleep > 0) Thread.sleep(timeToSleep);
                                }
                            } catch (IOException | InterruptedException e) {
                                PsBot.isPlaying = false;
                            }
                            PsBot.isPlaying=false;
                        })).start();
                        while(PsBot.isPlaying){
                            if(picture.get()!=null) {
                                BufferedImage bi = AWTUtil.toBufferedImage(picture.get());
                                String textNbt = ImageToChars.imageRender(bi);
                                for (String cmd : ImageToChars.textToCommands(textNbt)) PsBot.runCommand(cmd);
                                long nownow = Instant.now().toEpochMilli();
                                long timeSpent = nownow - lastTime;
                                lastTime = nownow;
                                long timeToSleep = 1000L / PsBot.fps - timeSpent;
                                if (timeToSleep > 0) Thread.sleep(timeToSleep);
                            }
                        }
                    } catch (IOException | JCodecException e) {
                        PsBot.isPlaying=false;
                    }
                }else {
                    Rectangle screenRect = null;
                    if (theWebcam != null) {
                        theWebcam.close();
                        theWebcam.open();
                    } else {
                        if (PsBot.screenSize[0] == 0 && PsBot.screenSize[1] == 0 && PsBot.screenSize[2] == 0 && PsBot.screenSize[3] == 0) {
                            screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        } else {
                            screenRect = new Rectangle(PsBot.screenSize[0], PsBot.screenSize[1], PsBot.screenSize[2], PsBot.screenSize[3]);
                        }
                    }
                    long lastTime = Instant.now().toEpochMilli();
                    while (PsBot.isPlaying) {
                        BufferedImage frame = theWebcam == null ? PsBot.robot.createScreenCapture(screenRect) : theWebcam.getImage();
                        String textNbt = ImageToChars.imageRender(frame);
                        for (String cmd : ImageToChars.textToCommands(textNbt)) PsBot.runCommand(cmd);
                        long nownow = Instant.now().toEpochMilli();
                        long timeSpent = nownow - lastTime;
                        lastTime = nownow;
                        long timeToSleep = 1000L / PsBot.fps - timeSpent;
                        if (timeToSleep > 0) Thread.sleep(timeToSleep);
                    }
                    if (theWebcam != null) theWebcam.close();
                }
            } catch (InterruptedException e) {
                if(theWebcam!=null)theWebcam.close();
                PsBot.isPlaying=false;
            }
        }
    }
}
