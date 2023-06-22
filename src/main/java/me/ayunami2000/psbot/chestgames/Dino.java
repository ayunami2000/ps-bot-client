package me.ayunami2000.psbot.chestgames;

import me.ayunami2000.psbot.ChestGamesThread;
import me.ayunami2000.psbot.PsBot;
import net.minecraft.util.math.Vec3i;

import java.util.HashSet;
import java.util.Set;

public class Dino {
    private int dinoY = 0;
    private boolean dinoDuck = false;
    private Set<Vec3i> gameObjects = new HashSet<>(); // z = type

    // ChestGamesThread.getWidth()
    // ChestGamesThread.getHeight()
    // ChestGamesThread.drawARGB(int x, int y, int argb)
    // ChestGamesThread.draw(int x, int y, int r, int g, int b)
    // ChestGamesThread.draw(int x, int y, int rgb)
    // ChestGamesThread.clickAt(int x, int y)
    // ChestGamesThread.clearPixels(int rgb)

    public Dino() throws InterruptedException {
        while (PsBot.isPlaying) {
            //game loop
            ChestGamesThread.clearPixels(16777215); // clear set pixels with white

            int buttons = getButtonPresses();
            if (((buttons == 1 || buttons == 3) && dinoY == 0) || dinoY == 1) {
                dinoY++;
            } else if (dinoY == 2) {
                dinoY = -1;
            } else if (dinoY == -1) {
                dinoY = 0; // todo: cooldown? via setting to -2 then back to 0, but render -2 as 0 still?
            }
            dinoDuck = buttons == 2 || buttons == 3; // yes they will have to spam it to stay ducked lol, maybe they can use 1 key while mouse over the slot for easier spamming?

            drawDino();

            // move objects 1 pixel left
            gameObjects.forEach(gameObject -> {
                if (!PsBot.isPlaying) return;
                gameObject.west();
                if (gameObject.getX() >= 0) drawGameObject(gameObject.getX(), gameObject.getY(), gameObject.getZ());
            });
            if (!PsBot.isPlaying) return;
            gameObjects.removeIf(gameObject -> gameObject.getX() < 0);
            while (gameObjects.size() < 3) {
                //add some more
                gameObjects.add(genNewObj());
            }

            Thread.sleep(1000L / PsBot.fps); // fps is WAYYYY too high...
        }
    }

    // todo: use Random() object instead
    private Vec3i genNewObj() {
        int type = (int) Math.floor(Math.random() * 2); // 0 - 1
        int y = 0;
        if (type == 0) {
            y = (int) Math.floor(Math.random() * 3);
        } else {
            y = 3 + (int) Math.floor(Math.random() * 2);
        }
        return new Vec3i(ChestGamesThread.getWidth() - 1, y, type);
    }

    private boolean checkCollision(int x, int y) {
        return x == 1 && (y == pixelDinoY(false) || (!dinoDuck && y == pixelDinoY(true)));
    }

    private int pixelDinoY(boolean topHalf) {
        return pixelFromBottom(Math.abs(dinoY) + (topHalf ? 1 : 0));
    }

    private int pixelFromBottom(int y) {
        return (ChestGamesThread.getHeight() - 1) - y;
    }

    private boolean drawAndCheck(int x, int y, int rgb) {
        if (checkCollision(x, y)) {
            PsBot.chatMsg("Game over!");
            PsBot.isPlaying = false;
            return true;
        }
        ChestGamesThread.draw(x, y, rgb);
        return false;
    }

    private void drawGameObject(int x, int y, int type) {
        switch (type) {
            case 0:
                // cactus
                // y is for cactus HEIGHT, not y
                for (int i = 0; i < y; i++) {
                    if (drawAndCheck(x, pixelFromBottom(i), 65280)) return; // solid green
                }
                break;
            case 1:
                // bird
                if (drawAndCheck(x, pixelFromBottom(y), 255)) return; // solid blue
                break;
            default:
                //return;
        }
    }

    private void drawDino() {
        if (!dinoDuck) ChestGamesThread.draw(1, pixelDinoY(true), 0, 0, 0);
        ChestGamesThread.draw(1, pixelDinoY(false), 0);
    }

    //might be cramped on a single chest...
    private int getButtonPresses() {
        // 0 - none
        // 1 - jump
        // 2 - duck
        // 3 - both
        int buttonsPressed = 0;

        boolean[] clicks = ChestGamesThread.getClicks();

        for (int i = 0; i < clicks.length / 2; i++) {
            if (clicks[i]) {
                buttonsPressed = 1;
                break;
            }
        }

        for (int i = clicks.length / 2; i < clicks.length; i++) {
            if (clicks[i]) {
                buttonsPressed += 2;
                break;
            }
        }

        return buttonsPressed;
    }
}
