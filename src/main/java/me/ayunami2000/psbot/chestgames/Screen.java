package me.ayunami2000.psbot.chestgames;

import me.ayunami2000.psbot.ChestGamesThread;
import me.ayunami2000.psbot.ImageToChars;
import me.ayunami2000.psbot.PsBot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Screen {
    public Screen() throws InterruptedException {
        Rectangle screenRect;
        if (PsBot.screenSize[0] == 0 && PsBot.screenSize[1] == 0 && PsBot.screenSize[2] == 0 && PsBot.screenSize[3] == 0) {
            screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        } else {
            screenRect = new Rectangle(PsBot.screenSize[0], PsBot.screenSize[1], PsBot.screenSize[2], PsBot.screenSize[3]);
        }
        while (PsBot.isPlaying) {
            //game loop
            BufferedImage img = ImageToChars.resizeImage(PsBot.robot.createScreenCapture(screenRect), ChestGamesThread.getWidth(), ChestGamesThread.getHeight());
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int rgb = img.getRGB(x, y);/*,
                            r=(rgb>>16) & 0xff,
                            g=(rgb>>8) & 0xff,
                            b=rgb & 0xff;
                    rgb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));*/
                    ChestGamesThread.drawARGB(x, y, rgb);
                }
            }

            Thread.sleep(1000L / PsBot.fps);
        }
    }
}
