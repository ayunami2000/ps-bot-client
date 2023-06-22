package me.ayunami2000.psbot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.ayunami2000.psbot.chestgames.*;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;

public class ChestGamesThread implements Runnable {
    @Override
    public void run() {
        changedPixels = new int[0];
        try {
            switch (PsBot.chestGameMode) {
                case 0:
                    //screen
                    new Screen();
                    break;
                case 1:
                    //dino
                    new Dino();
                    break;
                default:
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    private static Block[] stainedGlassBlocks=new Block[]{
            Blocks.WHITE_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS,
            Blocks.GRAY_STAINED_GLASS,
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS
    };

    private static Integer[][] rgbColors=new Integer[][]{
            new Integer[]{249, 255, 255},//white
            new Integer[]{249, 128, 29},//orange
            new Integer[]{198, 79, 189},//magenta
            new Integer[]{58, 179, 218},//light blue
            new Integer[]{255, 216, 61},//yellow
            new Integer[]{128, 199, 31},//lime
            new Integer[]{243, 140, 170},//pink
            new Integer[]{71, 79, 82},//gray
            new Integer[]{156, 157, 151},//light gray
            new Integer[]{22, 156, 157},//cyan
            new Integer[]{137, 50, 183},//purple
            new Integer[]{60, 68, 169},//blue
            new Integer[]{130, 84, 50},//brown
            new Integer[]{93, 124, 21},//green
            new Integer[]{176, 46, 38},//red
            new Integer[]{29, 28, 33}//black
    };

    private static Integer rgbToNumbers(int r,int g,int b){
        int id=0;
        int shortestDistance=1000;
        for (int i = 0; i < rgbColors.length; i++) {
            int dist = colorDistance(rgbColors[i][0], rgbColors[i][1], rgbColors[i][2], r, g, b);
            if (dist < shortestDistance) {
                shortestDistance = dist;
                id = i;
            }
        }

        return id;
    }
    private static Block rgbToBlocks(int r,int g,int b){
        Integer rgbaNums=rgbToNumbers(r,g,b);
        if(rgbaNums==-1)return Blocks.BARRIER;
        return stainedGlassBlocks[rgbaNums];
    }
    private static float colorDistance(double r1,double g1,double b1,double r2,double g2,double b2) {
        //return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        return (float)Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }
    private static int colorDistance(int r1,int g1,int b1,int r2,int g2,int b2) {
        return (int)colorDistance((double)r1,(double)g1,(double)b1,(double)r2,(double)g2,(double)b2);
    }
    */

    private static int[] changedPixels = new int[0]; // initialize at zero in case it doesn't use clearPixels

    public static void clearPixels(int rgb) {
        if (PsBot.mc.currentScreen instanceof GenericContainerScreen) {
            int maxI = Math.min(changedPixels.length, getWidth() * getHeight());
            for (int i = 0; i < maxI; i++) {
                if (changedPixels[i] != rgb) draw(i % getWidth(), Math.floorDiv(i, getHeight()), rgb);
            }
        }
        if (changedPixels.length != getWidth() * getHeight()) {
            changedPixels = new int[getWidth() * getHeight()];
            Arrays.fill(changedPixels, rgb == 16777215 ? 0 : rgb + 1);
        }
    }

    public static void draw(int x, int y, int r, int g, int b) {
        draw(x, y, ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public static void draw(int x, int y, int rgb) {
        //ItemStack displayItem = new ItemStack(rgbToBlocks(r, g, b).asItem());
        ItemStack displayItem = new ItemStack(Items.LEATHER_CHESTPLATE);
        try {
            displayItem.setNbt(StringNbtReader.parse("{HideFlags:66,display:{Name:'\"\"',color:" + rgb + "}}"));
            PsBot.mc.interactionManager.clickCreativeStack(displayItem,36 + x);
            if (PsBot.mc.currentScreen instanceof GenericContainerScreen containerScreen) {
                int i = (y * getWidth()) + x;
                PsBot.mc.interactionManager.clickSlot(containerScreen.getScreenHandler().syncId, i, x, SlotActionType.SWAP, PsBot.mc.player);
                if (changedPixels.length > i) changedPixels[i] = rgb;
                PsBot.mc.player.getInventory().updateItems();
            }
        }catch(CommandSyntaxException e){}
    }

    public static void drawARGB(int x, int y, int argb) {
        draw(x, y, convertARGBtoRGB(argb));
    }

    // https://gist.github.com/TobleMiner/7b53cfc5aae7082260fb
    private static int convertARGBtoRGB(int argbValue) {
        int rgb = 0;
        // Extract bit 24 - 31 and discard sign (& 0xFF)
        double alpha = ((argbValue >> 24) & 0xFF) / 255d;

        for(int i = 0; i <= 16; i += 8) {
            // Extract color channel
            int channel = argbValue >> i & 0xFF;
            // Blend channel
            channel = (int) (channel * alpha + 255 * (1 - alpha));
            // Store result
            rgb |= channel << i;
        }
        return rgb;
    }

    private static int chestWidth = 9;
    private static int chestHeight = 3;

    public static int getWidth() {
        return chestWidth;
    }

    public static int getHeight() {
        if (PsBot.mc.currentScreen instanceof GenericContainerScreen containerScreen) {
            chestHeight = containerScreen.getScreenHandler().getInventory().size() / getWidth();
        }
        return chestHeight;
    }

    private static boolean[] clicks = new boolean[9 * 3];

    public static boolean[] getClicks() {
        if (PsBot.mc.currentScreen instanceof GenericContainerScreen containerScreen) {
            clicks = new boolean[getWidth() * getHeight()];
            for (int i = 0; i < getWidth() * getHeight(); i++) {
                clicks[i] = containerScreen.getScreenHandler().getSlot(i).hasStack();
            }
        } else {
            Arrays.fill(clicks, false);
        }
        return clicks;
    }

    public static boolean clickAt(int x, int y) {
        if (PsBot.mc.currentScreen instanceof GenericContainerScreen containerScreen) {
            return containerScreen.getScreenHandler().getSlot((y * getWidth()) + x).hasStack();
        } else {
            return false;
        }
    }
}
