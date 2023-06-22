package me.ayunami2000.psbot;

import com.sun.net.httpserver.HttpExchange;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.sound.sampled.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Stream;

public class ProximityThread implements Runnable{
    public static boolean isProx=false;
    public static ProxSockServer ws=null;
    public static String deviceName="";
    private static AudioFormat aFormat=null;
    private static ByteArrayOutputStream out=null;

    @Override
    public void run() {
        if(PsBot.httpServer==null){
            PsBot.chatMsg("Error: HTTP server did not start successfully!");
            return;
        }
        ///*
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
        isProx=true;
        PsBot.chatMsg("Proximity Audio is now enabled! Access it at :8469/prox");

        aFormat = new AudioFormat(11025,16,2,true,false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, aFormat);
        try{
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
                PsBot.isPlaying=false;
                return;
            }
            TargetDataLine line = (TargetDataLine) mixer.getLine(info);
            line.open(aFormat,line.getBufferSize());
            out=new ByteArrayOutputStream();
            AudioInputStream inputStream=new AudioInputStream(line);
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * aFormat.getFrameSize();
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;

            line.start();

            ws=new ProxSockServer(new InetSocketAddress("localhost",8468));
            new Thread(() -> ws.run()).start();

            try {
                while (PsBot.isPlaying/* && System.currentTimeMillis() - now < 500*/) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while (baos.size() < 11050.0 / 50.0) {
                        if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                            break;
                        }
                        baos.write(data);
                    }
                    baos.flush();
                    List<AbstractClientPlayerEntity> playerList = PsBot.mc.world.getPlayers().stream().filter(abstractClientPlayerEntity -> abstractClientPlayerEntity.distanceTo(PsBot.mc.player) <= 16).toList();

                    List<String> names = playerList.stream().map(player -> player.getName().getString()).toList();
                    Vec3d myPos = PsBot.mc.player.getPos();

                    if (ProximityThread.ws != null) ws.wsList.forEach((webSocket, s) -> {
                        if (names.contains(s)) {
                            AbstractClientPlayerEntity player = playerList.stream().filter(abstractClientPlayerEntity -> abstractClientPlayerEntity.getName().getString().equals(s)).findFirst().orElse(null);
                            Vec3d loc = player.getPos();
                            float yaw = player.getYaw();
                            float pitch = player.getPitch();
                            Vec3d pos = new Vec3d(loc.x - myPos.x, loc.y - myPos.y, loc.z - myPos.z);
                            pos = pos.rotateY((float)(yaw*Math.PI/180.0));
                            pos = pos.rotateX((float)(pitch*Math.PI/180.0));
                            //pos = new Vec3d(pos.x * Math.cos(yaw) + pos.z * Math.sin(yaw), pos.y - (pitch / 90), pos.z * Math.cos(yaw) - pos.x * Math.sin(yaw));
                            if (webSocket.isOpen()) {
                                webSocket.send(pos.x + "," + pos.y + "," + pos.z);
                                webSocket.send(baos.toByteArray());
                            }
                        } else if (PsBot.mc.player.getName().getString().equals(s)) {
                            if (webSocket.isOpen()) {
                                webSocket.send("0,0,0");
                                webSocket.send(baos.toByteArray());
                            }
                        }
                    });
                }
            }catch(Exception e){}
            line.stop();
            line.flush();
            line.close();
        } catch  (Exception err){	System.err.println("Error: " + err.getMessage());}

        //*/

        /*
        isProx=true;
        PsBot.chatMsg("Proximity Audio is now enabled! Access it at :8469/prox?yourUsernameOrUUID");
        try {
            while (PsBot.isPlaying){
                Thread.sleep(1);
            }
        }catch(InterruptedException e){}
        //*/
        if(ws!=null) {
            try {
                ws.stop(1);
            } catch (InterruptedException e) {
            }
        }
        ws=null;
        isProx=false;
        PsBot.isPlaying=false;
    }

    public static void handleHttp(HttpExchange t) throws IOException {
        /*
        List<AbstractClientPlayerEntity> playerList = PsBot.mc.world.getPlayers();
        try {
            String userOrUUID = t.getRequestURI().getQuery();
            if (userOrUUID.length() > 100) userOrUUID = userOrUUID.substring(0, 100);
            AbstractClientPlayerEntity thePlayer = null;
            for (AbstractClientPlayerEntity player : playerList) {
                if (player.getName().getString().equals(userOrUUID)) {
                    thePlayer = player;
                    break;
                }
                if (player.getUuidAsString().equals(userOrUUID)) {
                    thePlayer = player;
                    break;
                }
            }
            if (thePlayer == null) {
                String response = "Error: Player not found!";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            //try {
            //    Thread.sleep(10000);//lets go for 10s of audio
            //} catch (InterruptedException e) {}
            //bais.close();
            //audioInputStream.close();

                //AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, t.getResponseBody());
                return;
            }
            String response = "Player found. Distance: " + thePlayer.getPos().subtract(PsBot.mc.player.getPos()).toString();
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }catch(Exception e){
            String response = "Nearby Visible Players: \n";
            for (AbstractClientPlayerEntity player : playerList) {
                response+=player.getName().getString()+" ("+player.getUuidAsString()+")\n";
            }
            response=response.substring(0,response.length()-1);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            /*
            byte audioBytes[] = out.toByteArray();
            t.getResponseHeaders().set("Content-Type","audio/wav");
            t.sendResponseHeaders(200, audioBytes.length-44100*8);//length is dynamic!! please change.
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            bais.readNBytes(audioBytes.length-44100*8);
            AudioInputStream audioInputStream = new AudioInputStream(bais, aFormat, audioBytes.length / aFormat.getFrameSize());

            //long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / aFormat.getFrameRate());

            try {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, t.getResponseBody());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            * /
        }
        */
        String response = "Please host the index.html and forward using nginx port :8468 to the same port.";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
