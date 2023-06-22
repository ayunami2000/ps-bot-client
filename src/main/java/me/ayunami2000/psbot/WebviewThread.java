package me.ayunami2000.psbot;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import org.lwjgl.stb.STBImageWrite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public class WebviewThread implements Runnable{
    public static boolean isWebview=false;

    @Override
    public void run() {
        if(PsBot.httpServer==null) {
            PsBot.chatMsg("Error: HTTP server did not start successfully!");
            return;
        }
        isWebview=true;
        PsBot.chatMsg("Webview is now enabled! Access it at :8469/webview");
        try {
            while (PsBot.isPlaying){
                Thread.sleep(1);
            }
        }catch(InterruptedException e){}
        isWebview=false;
    }

    public static void handleHttp(HttpExchange t) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                try {
                    finishHttpStuff(t);
                } catch (IOException e) {}
            });
        } else {
            try {
                finishHttpStuff(t);
            } catch (IOException e) {}
        }
    }

    private static void finishHttpStuff(HttpExchange t) throws IOException{
        NativeImage gameScreen = ScreenshotRecorder.takeScreenshot(PsBot.mc.getFramebuffer());
        byte[] response = gameScreen.getBytes();
        t.sendResponseHeaders(200, response.length);
        OutputStream os = t.getResponseBody();
        os.write(response);
        os.close();
    }
}
