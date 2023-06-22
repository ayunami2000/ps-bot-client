package me.ayunami2000.psbot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.time.Instant;

public class ImageToChars {
    private static final Integer[][] rgbColors=new Integer[][]{
            new Integer[]{190, 0, 0},
            new Integer[]{254, 63, 63},
            new Integer[]{217, 163, 52},
            new Integer[]{254, 254, 63},
            new Integer[]{0, 190, 0},
            new Integer[]{63, 254, 63},
            new Integer[]{63, 254, 254},
            new Integer[]{0, 190, 190},
            new Integer[]{0, 0, 190},
            new Integer[]{63, 63, 254},
            new Integer[]{254, 63, 254},
            new Integer[]{190, 0, 190},
            new Integer[]{255, 255, 255},
            new Integer[]{190, 190, 190},
            new Integer[]{63, 63, 63},
            new Integer[]{0, 0, 0}
    };
    private static final String[] colorCodes=new String[]{
            "4",
            "c",
            "6",
            "e",
            "2",
            "a",
            "b",
            "3",
            "1",
            "9",
            "d",
            "5",
            "f",
            "7",
            "8",
            "0"
    };
    public static BufferedImage scaleImage(BufferedImage img,int maxSize){
        return scaleImage(img,maxSize,1,1);
    }
    public static BufferedImage scaleImage(BufferedImage img,int maxSize, int stretchx, int stretchy){
        return scaleImage(img,maxSize,stretchx,stretchy, false);
    }
    public static BufferedImage scaleImage(BufferedImage img,int maxSize,int stretchx, int stretchy, boolean forceStretch){
        int imgWidth=img.getWidth(),
                imgHeight=img.getHeight();
        float aspectRatio=(float)imgWidth/(float)imgHeight;
        if(stretchx==1&&stretchy==1){
            if(Math.max(imgWidth,imgHeight)==maxSize)return img;
            //if(imgWidth>maxSize||imgHeight>maxSize) {
            return resizeImage(img,aspectRatio<1?-1:maxSize, aspectRatio<1?maxSize:-1);
            //}
        }else{
            //if(imgWidth>maxSize*stretchx||imgHeight>maxSize*stretchy) {
            if (forceStretch) {
                return resizeImage(img,imgWidth*stretchx, imgHeight*stretchy);
            } else {
                return resizeImage(img,(int)(aspectRatio<1?(maxSize*aspectRatio*stretchx):(maxSize*stretchx)), (int)(aspectRatio<1?(maxSize*stretchy):(stretchy*maxSize/aspectRatio)));
            }
            //}
        }
        //return img;
    }
    public static BufferedImage resizeImage(BufferedImage img,int width,int height){
        return convertImageToBuffered(img.getScaledInstance(width, height, Image.SCALE_SMOOTH)); // consider: Image.SCALE_FAST
    }
    public static BufferedImage scaleForConv(BufferedImage img) {
        return scaleForConv(img, PsBot.renderMode);
    }
    public static BufferedImage scaleForConv(BufferedImage img, int rm) {
        switch(rm){
            case 0:
            case 11:
            case 6:
            case 7:
            case 8:
            case 9:
            case 3:
            case 4:
            case 5:
            case 12:
                return PsBot.pixelMode==1?ImageToChars.scaleImage(img,PsBot.imgSize,3,1):ImageToChars.scaleImage(img,PsBot.imgSize);
            case 1:
                return ImageToChars.scaleImage(img,PsBot.imgSize,4,1);
            case 2:
                return ImageToChars.scaleImage(img,PsBot.imgSize);
            case 10:
                return ImageToChars.scaleImage(img,14);
            default:
        }
        return img;
    }
    public static String toHexBlocks(BufferedImage img){
        String[] brailleChars=null;
        if (PsBot.pixelMode==1) brailleChars = toBraille(img).split("");

        img = scaleForConv(img);

        String restxt=PsBot.renderMode==11 ? "" : "[";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                String pixelChar = PsBot.pixelMode == 1 ? brailleChars[y*img.getWidth() + x] : "⬛";
                if (PsBot.renderMode == 11) {
                    //restxt += "&" + String.format("#%02x%02x%02x", imgR, imgG, imgB) + "⬛";
                    restxt += "&x" + String.format("%02x%02x%02x", imgR, imgG, imgB).replaceAll(".", "&$0") + pixelChar;
                } else {
                    restxt += "{\"text\":\"" + pixelChar + "\",\"color\":\"" + String.format("#%02x%02x%02x", imgR, imgG, imgB) + "\"},";
                }
            }
            restxt=restxt.substring(0, restxt.length() - 1) + (PsBot.renderMode==11 ? "\n" : ",\"\\n\",");
        }
        restxt=restxt.substring(0, restxt.length() - 6)+(SubtitleThread.alreadySubbing?(PsBot.renderMode==11 ? SubtitleThread.itemSub : (",\"§r\\n"+SubtitleThread.itemSub+"\"")):"") + (PsBot.renderMode==11 ? "" : "]");
        return restxt;
    }
    public static String toLineHexBlocks(BufferedImage img){
        String[] brailleChars=null;
        if (PsBot.pixelMode==1) brailleChars = toBraille(img).split("");

        img = scaleForConv(img);

        String restxt="[";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                String pixelChar = PsBot.pixelMode == 1 ? brailleChars[y*img.getWidth() + x] : "⬛";
                restxt+="{\"text\":\"" + pixelChar + "\",\"color\":\""+String.format("#%02x%02x%02x", imgR, imgG, imgB)+"\"},";
            }
            restxt=restxt.substring(0, restxt.length() - 1)+"]\n[";
        }
        restxt=restxt.substring(0, restxt.length() - 2);
        return restxt;
    }
    public static String toVncHexBlocks(BufferedImage img,int oWidth,int oHeight){
        String[] brailleChars=null;
        if (PsBot.pixelMode==1) brailleChars = toBraille(img).split("");

        img = scaleForConv(img, 0);

        String restxt="[";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                String pixelChar = PsBot.pixelMode == 1 ? brailleChars[y*img.getWidth() + x] : "⬛";
                restxt+="{\"text\":\"" + pixelChar + "\",\"color\":\""+String.format("#%02x%02x%02x", imgR, imgG, imgB)+"\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{vnc m "+((int)(oWidth*((double)x/(double)img.getWidth())))+" "+((int)(oHeight*((double)y/(double)img.getHeight())))+"\"}},";
            }
            restxt=restxt.substring(0, restxt.length() - 1)+",\"\\n\",";
        }
        restxt+="{\"text\":\"§9§nDrag Mode\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{vnc d\"}},\"§r§9: "+(PsBot.vncThread.dragging?"§aEn":"§cDis")+"abled§9."+(PsBot.vncThread.dragging?" Mouse Down: "+(PsBot.vncThread.mouseClicking?"§aYes":"§cNo")+"§9.":"")+"\"]";
        return restxt;
    }
    public static String[] toVncHexBlocksParts(BufferedImage img,int oWidth,int oHeight){
        String[] brailleChars=null;
        if (PsBot.pixelMode==1) brailleChars = toBraille(img).split("");

        img = scaleForConv(img);

        String restxt1="[",
                restxt2="[";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                String pixelChar = PsBot.pixelMode == 1 ? brailleChars[y*img.getWidth() + x] : "⬛";
                if (y > img.getHeight() / 2.0) {
                    restxt2 += "{\"text\":\"" + pixelChar + "\",\"color\":\"" + String.format("#%02x%02x%02x", imgR, imgG, imgB) + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{vnc mouse " + ((int) (oWidth * ((double) x / (double) img.getWidth()))) + " " + ((int) (oHeight * ((double) y / (double) img.getHeight()))) + "\"}},";
                } else {
                    restxt1 += "{\"text\":\"" + pixelChar + "\",\"color\":\"" + String.format("#%02x%02x%02x", imgR, imgG, imgB) + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{vnc mouse " + ((int) (oWidth * ((double) x / (double) img.getWidth()))) + " " + ((int) (oHeight * ((double) y / (double) img.getHeight()))) + "\"}},";
                }
            }
            if(y>img.getHeight()/2.0){
                restxt2=restxt2.substring(0, restxt2.length() - 1)+",\"\\n\",";
            }else {
                restxt1 = restxt1.substring(0, restxt1.length() - 1) + ",\"\\n\",";
            }
        }
        restxt1=restxt1.substring(0, restxt1.length() - 6)+"]";
        restxt2=restxt2.substring(0, restxt2.length() - 6)+"]";
        return new String[]{restxt1,restxt2};
    }
    public static String toLoreHexBlocks(BufferedImage img){
        String[] brailleChars=null;
        if (PsBot.pixelMode==1) brailleChars = toBraille(img).split("");

        img = scaleForConv(img);

        String restxt="['[";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                String pixelChar = PsBot.pixelMode == 1 ? brailleChars[y*img.getWidth() + x] : "⬛";
                restxt+="{\"text\":\"" + pixelChar + "\",\"italic\":\"false\",\"color\":\""+String.format("#%02x%02x%02x", imgR, imgG, imgB)+"\"},";
            }
            restxt=restxt.substring(0, restxt.length() - 1)+"]','[";
        }
        restxt=restxt.substring(0, restxt.length() - 3)+(PsBot.subtitleThread.alreadySubbing?PsBot.subtitleThread.itemSub:"")+"]";
        return restxt;
    }
    public static String toWritableBookHexBlocks(BufferedImage img){
        return toColorLines(img,"⬛");
    }
    public static String toColorLines(BufferedImage img){
        return toColorLines(img,"|");
    }
    public static String toColorLines(BufferedImage img,String quoteOnQuoteLine){
        img = scaleForConv(img);

        String restxt="[\"";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                restxt+="§"+rgbaToCodes(imgR,imgG,imgB,imgA)+quoteOnQuoteLine;
            }
            restxt=restxt+"\\n";
        }
        restxt=restxt.substring(0, restxt.length() - 2)+"\"]";
        return restxt;
    }
    public static String toHexParticles(BufferedImage img){
        img = scaleForConv(img);

        String restxt="";
        for(int y=0;y<img.getHeight();y++){
            for(int x=0;x<img.getWidth();x++){
                int ip=img.getRGB(x,y),
                        imgA=(ip>>24) & 0xff,
                        imgR=(ip>>16) & 0xff,
                        imgG=(ip>>8) & 0xff,
                        imgB=ip & 0xff;
                restxt+="particle dust "+(imgR/255.0)+" "+(imgG/255.0)+" "+(imgB/255.0)+" 1 "+(PsBot.originPos.getX()-(x/4.0))+" "+(100-(y/4.0))+" "+PsBot.originPos.getZ()+" 0 0 0 0 1 force|";
            }
        }
        restxt=restxt.substring(0, restxt.length() - 1);
        return restxt;
    }
    public static String toBraille(BufferedImage imgg){
        BufferedImage img = ImageToChars.scaleImage(imgg, PsBot.imgSize, 6, 4);

        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

        int[] splitPixels = new int[pixels.length * /*4*/3];

        for (int i = 0; i < pixels.length; i++) {
            int ip = pixels[i],
                //imgA=(ip >> 24) & 0xff,
                imgR = (ip >> 16) & 0xff,
                imgG = (ip >> 8) & 0xff,
                imgB = ip & 0xff;
            splitPixels[/*4*/3 * i] = imgR;
            splitPixels[/*4*/3 * i + 1] = imgG;
            splitPixels[/*4*/3 * i + 2] = imgB;
            //splitPixels[4 * i + 3] = imgA;
        }

        String output = "";

        for(int y = 0; y < img.getHeight(); y += 4) {
            for(int x = 0; x < img.getWidth(); x += 2) {
                int index = x + img.getWidth() * y;
                int brailleCharData = brailleCodeAt(splitPixels, index * /*4*/3, img.getWidth());
                output += brailleChars.charAt(brailleCharData);
            }
        }

        return output;
    }
    public static String imageRender(BufferedImage img){
        String textNbt="";
        switch(PsBot.renderMode){
            case 0:
            case 11:
                textNbt=ImageToChars.toHexBlocks(img);
                break;
            case 1:
                //fard (use of this is discouraged)
                textNbt=ImageToChars.toColorLines(img);
                break;
            case 2:
                textNbt=ImageToChars.toHexParticles(img);
                break;
            case 3:
            case 4:
            case 5:
            case 12:
                textNbt=ImageToChars.toLoreHexBlocks(img);
                break;
            case 6:
            case 7:
            case 8:
            case 9:
                textNbt=ImageToChars.toLineHexBlocks(img);
                break;
            case 10:
                //todo: add braille support
                textNbt=ImageToChars.toWritableBookHexBlocks(img);
                break;
            default:
        }
        return textNbt;
    }
    private static int alternator=0;
    private static int alternating(){
        alternator=(alternator+1)%2;
        return alternator;
    }
    private static long bowWowNow = 0;
    private static BlockPos lecternPos = null;
    public static String[] textToCommands(String s){
        String[] result=new String[]{};
        switch(PsBot.renderMode){
            case 0:
            case 1:
                if(PsBot.lineMode==1){
                    String[] trPieces = s.substring(1, s.length() - 1).split(",\"\\\\n\",");
                    result=new String[trPieces.length];
                    for (int i = 0; i < trPieces.length; i++) result[i]="tellraw @a [" + trPieces[i] + "]";
                }else{
                    result=new String[]{"tellraw @a "+s};
                }
                break;
            case 2:
                result= s.split("\\|");
                //armor stand: move by 0.5
                //summon armor_stand -68.0 69.5 -55.0 {NoGravity:1b,Small:1b,Invisible:1b,ArmorItems:[{},{},{},{id:"leather_helmet",Count:1b,tag:{display:{color:16711680}}}]}
                break;
            case 3:
                result = new String[]{"replaceitem entity @a weapon.offhand paper{display:{Name:'{\"text\":\"hover over me -ayunami2000\",\"color\":\"green\",\"bold\":\"true\",\"italic\":\"false\"}',Lore:"+s+"}} 1"};
                break;
            case 4:
                result = new String[]{"item replace entity @a weapon.offhand paper{display:{Name:'{\"text\":\"hover over me -ayunami2000\",\"color\":\"green\",\"bold\":\"true\",\"italic\":\"false\"}',Lore:"+s+"}} 1"};
                break;
            case 5:
            case 12:
                ItemStack displayItem = new ItemStack(Items.PAPER);
                try {
                    displayItem.setNbt(StringNbtReader.parse("{display:{Name:'{\"text\":\"hover over me -ayunami2000\",\"color\":\"green\",\"bold\":\"true\",\"italic\":\"false\"}',Lore:"+s+"}}"));
                    PsBot.mc.interactionManager.clickCreativeStack(displayItem,4+36);
                    if (PsBot.renderMode==12){
                        if (PsBot.mc.currentScreen instanceof GenericContainerScreen containerScreen){
                            PsBot.mc.interactionManager.clickSlot(containerScreen.getScreenHandler().syncId, 13, 4, SlotActionType.SWAP, PsBot.mc.player);
                            PsBot.mc.player.getInventory().updateItems();
                        }
                    }
                }catch(CommandSyntaxException e){}
                break;
            case 6:
                String[] splstr=s.split("\n");
                for (int i = 0; i < splstr.length; i++) {
                    splstr[i]="data modify entity @e[tag=ps-bot-"+i+",limit=1] {} merge value {CustomName:'"+splstr[i]+"'}";
                }
                result=splstr;
                break;
            case 7:
                String[] splstrr=s.split("\n");
                for (int i = 0; i < splstrr.length; i++) {
                    splstrr[i]="kill @e[tag=ps-bot-"+((i-1)%splstrr.length)+"]\nsummon armor_stand "+PsBot.originPos.getX()+" "+(100-(i/4.0))+" "+PsBot.originPos.getZ()+" {CustomNameVisible:1b,Marker:1b,Invisible:1b,Tags:[\"ps-bot\",\"ps-bot-"+i+"\"],CustomName:'"+splstrr[i]+"'}";
                }
                result=String.join("\n",splstrr).split("\n");
                break;
            case 8:
                String[] splstrrr=s.split("\n");
                int alt=alternating();
                for (int i = 0; i < splstrrr.length; i++) {
                    if(i%2==alt) {
                        splstrrr[i] = "summon armor_stand " + PsBot.originPos.getX() + " " + (100 - (i / 4.0)) + " " + PsBot.originPos.getZ() + " {CustomNameVisible:1b,Marker:1b,Invisible:1b,Tags:[\"ps-bot\",\"ps-bot-" + i + "\"],CustomName:'" + splstrrr[i] + "'}";
                    }else{
                        splstrrr[i] = "kill @e[tag=ps-bot-" + i + "]";
                    }
                }
                result=String.join("\n",splstrrr).split("\n");
                break;
            case 9:
                String[] splstrrrr=s.split("\n");
                for (int i = 0; i < splstrrrr.length; i++) {
                    //lets try to calculate the amount of time this command will take based on how long the last one took
                    //splstrrrr[i]="summon area_effect_cloud "+PsBot.originPos.getX()+" "+(100-(i/4.0))+" "+PsBot.originPos.getZ()+" {Particle:\"block air\",Radius:0f,Duration:0,WaitTime:"+((splstrrrr.length - 1) * Math.max(1.0,1.0/PsBot.cmdPerTick))+",CustomNameVisible:1b,CustomName:'"+splstrrrr[i]+"'}";
                    //lets use PsBot.fps
                    //splstrrrr[i]="summon area_effect_cloud "+PsBot.originPos.getX()+" "+(100-(i/4.0))+" "+PsBot.originPos.getZ()+" {Particle:\"block air\",Radius:0f,Duration:0,WaitTime:"+(20.0 / ((double)PsBot.fps))+",CustomNameVisible:1b,CustomName:'"+splstrrrr[i]+"'}";
                    //lets try something else
                    splstrrrr[i]="summon area_effect_cloud "+PsBot.originPos.getX()+" "+(100-(i/4.0))+" "+PsBot.originPos.getZ()+" {Particle:\"block air\",Radius:0f,Duration:0,WaitTime:"+(Math.max(0,(1000L / ((float)PsBot.fps) - (Instant.now().toEpochMilli()-bowWowNow)) / 50.0))+",CustomNameVisible:1b,CustomName:'"+splstrrrr[i]+"'}";
                }
                bowWowNow=Instant.now().toEpochMilli();
                result=splstrrrr;
                break;
            case 10:
                if(PsBot.mc.currentScreen instanceof LecternScreen){
                    PsBot.mc.interactionManager.clickButton(1,3);
                    ItemStack displayItemm = new ItemStack(Items.WRITABLE_BOOK);
                    try {
                        displayItemm.setNbt(StringNbtReader.parse("{pages:"+s+"}"));
                        PsBot.loadItem(displayItemm);
                    }catch(CommandSyntaxException e){}
                }else{
                    if(PsBot.mc.world.getBlockState(lecternPos).getBlock() instanceof LecternBlock){
                        //needs to be called twice, WITH 50ms DELAY!!
                        PsBot.rightClickBlock(lecternPos, Direction.UP);
                        PsBot.rightClickBlock(lecternPos, Direction.UP);
                    }
                }
                break;
            case 11:
                String[] chatLines = s.split("\n");
                for (String chatLine : chatLines) {
                    PsBot.sendChatOrCommand(chatLine);
                    try {
                        Thread.sleep(Math.max(20, (1000L / PsBot.fps) / chatLines.length));
                    } catch (InterruptedException e) { }
                }
                break;
            default:
        }
        return result;
    }
    public static boolean preRenderMode(){
        switch(PsBot.renderMode){
            case 6:
                PsBot.sendChatOrCommand("/kill @e[tag=ps-bot]");
                try{Thread.sleep(500);}catch(InterruptedException e){}
                for(int i=0;i<PsBot.imgSize;i++)PsBot.runCommand("summon armor_stand "+PsBot.originPos.getX()+" "+(100-(i/4.0))+" "+PsBot.originPos.getZ()+" {CustomNameVisible:1b,Marker:1b,Invisible:1b,Tags:[\"ps-bot\",\"ps-bot-"+i+"\"],CustomName:'{\"text\":\"\"}'}");
                break;
            case 9:
                bowWowNow=Instant.now().toEpochMilli();
                break;
            case 10:
                HitResult rayTraceBlock=PsBot.mc.getCameraEntity().raycast(PsBot.mc.interactionManager.getReachDistance(),PsBot.mc.getTickDelta(),false);
                if(rayTraceBlock.getType()==HitResult.Type.BLOCK){
                    BlockPos blockPos=new BlockPos(rayTraceBlock.getPos());
                    BlockState blockState=PsBot.mc.world.getBlockState(blockPos);
                    if(blockState.getBlock() instanceof LecternBlock){
                        BlockEntity blockEntity=PsBot.mc.world.getBlockEntity(blockPos);
                        if(blockEntity instanceof LecternBlockEntity&&!((LecternBlockEntity)blockEntity).hasBook()){
                            try {
                                PsBot.loadItem(Items.WRITABLE_BOOK.getDefaultStack());
                                Thread.sleep(50);
                                PsBot.rightClickBlock(blockPos, Direction.UP);
                                Thread.sleep(50);
                            } catch (InterruptedException e) {}
                        }
                        lecternPos=blockPos;
                        if(!(PsBot.mc.currentScreen instanceof LecternScreen)){
                            PsBot.rightClickBlock(blockPos, Direction.UP);
                        }
                        return true;
                    }
                }
                return false;
                //break;
            default:
        }
        return true;
    }
    public static BufferedImage convertImageToBuffered(Image image){
        BufferedImage newImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=newImage.createGraphics();
        g.drawImage(image,0,0,null);
        g.dispose();
        return newImage;
    }
    public static String rgbaToCodes(int r,int g,int b,int a){
        //if(a<85)return -1;
        int id=0;
        int shortestDistance=1000;
        for (int i = 0; i < rgbColors.length; i++) {
            int dist = colorDistance(rgbColors[i][0], rgbColors[i][1], rgbColors[i][2], r, g, b);
            if (dist < shortestDistance) {
                shortestDistance = dist;
                id = i;
            }
        }

        return colorCodes[id];
    }

    // as a lazy fuck, i have skidded the braille core code, HOWEVER i am a RESPECTFUL lazy fuck so here is the source link:
    // https://raw.githubusercontent.com/505e06b2/Image-to-Braille/master/braille.js
    // https://github.com/mohayonao/seurat/blob/master/src/seurat.js

    private static final String brailleChars = "⠀⠁⠂⠃⠄⠅⠆⠇⡀⡁⡂⡃⡄⡅⡆⡇⠈⠉⠊⠋⠌⠍⠎⠏⡈⡉⡊⡋⡌⡍⡎⡏⠐⠑⠒⠓⠔⠕⠖⠗⡐⡑⡒⡓⡔⡕⡖⡗⠘⠙⠚⠛⠜⠝⠞⠟⡘⡙⡚⡛⡜⡝⡞⡟⠠⠡⠢⠣⠤⠥⠦⠧⡠⡡⡢⡣⡤⡥⡦⡧⠨⠩⠪⠫⠬⠭⠮⠯⡨⡩⡪⡫⡬⡭⡮⡯⠰⠱⠲⠳⠴⠵⠶⠷⡰⡱⡲⡳⡴⡵⡶⡷⠸⠹⠺⠻⠼⠽⠾⠿⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⣀⣁⣂⣃⣄⣅⣆⣇⢈⢉⢊⢋⢌⢍⢎⢏⣈⣉⣊⣋⣌⣍⣎⣏⢐⢑⢒⢓⢔⢕⢖⢗⣐⣑⣒⣓⣔⣕⣖⣗⢘⢙⢚⢛⢜⢝⢞⢟⣘⣙⣚⣛⣜⣝⣞⣟⢠⢡⢢⢣⢤⢥⢦⢧⣠⣡⣢⣣⣤⣥⣦⣧⢨⢩⢪⢫⢬⢭⢮⢯⣨⣩⣪⣫⣬⣭⣮⣯⢰⢱⢲⢳⢴⢵⢶⢷⣰⣱⣲⣳⣴⣵⣶⣷⢸⢹⢺⢻⢼⢽⢾⢿⣸⣹⣺⣻⣼⣽⣾⣿";

    private static int brailleCodeAt(int[] rgb, int offset, int width) {
        int num = 0;

        for (int i = 0; i < 8; i++) {
            int off = offset + (i % 4) * (width * /*4*/3) + Math.floorDiv(i, 4) * /*4*/3;
            if (off >= rgb.length || !isEdge(rgb, off, width)) { // todo: subarray? or no?
                num += Math.pow(2, i);
            }
        }

        return num;
    }

    public static float colorDistance(double r1,double g1,double b1,double r2,double g2,double b2) {
        //return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        return (float)Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }
    public static int colorDistance(int r1,int g1,int b1,int r2,int g2,int b2) {
        return (int)colorDistance((double)r1,(double)g1,(double)b1,(double)r2,(double)g2,(double)b2);
    }

    private static float toGreyscale(float r, float g, float b) {
        return (0.22f * r) + (0.72f * g) + (0.06f * b);
    }

    private static float makeItGray(int[] rgb, int offset) {
        return /*(rgb[offset + 3] / 255f) * */toGreyscale(rgb[offset], rgb[offset + 1], rgb[offset + 2]);
    }

    private static final float edgeThresh = 30f;

    // https://codepen.io/taylorcoffelt/pen/eYNZvZ

    private static boolean isEdge(int[] rgb, int offset, int width) {
        float pixel = makeItGray(rgb, offset); // todo: preprocess gray?!?!?!??!? nahhHHHhhhh

        float left = (offset % (width * /*4*/3) == 0) ? pixel : makeItGray(rgb, offset - /*4*/3);
        float right = ((offset + /*4*/3) % (width * /*4*/3) == 0) ? pixel : makeItGray(rgb, offset + /*4*/3);
        float top = (offset < width * /*4*/3) ? pixel : makeItGray(rgb, offset - width * /*4*/3);
        float bottom = (offset + width * /*4*/3 >= rgb.length) ? pixel : makeItGray(rgb, offset + width * /*4*/3);

        if (pixel > left + edgeThresh) return true;
        if (pixel < left - edgeThresh) return true;
        if (pixel > right + edgeThresh) return true;
        if (pixel < right - edgeThresh) return true;
        if (pixel > top + edgeThresh) return true;
        if (pixel < top - edgeThresh) return true;
        if (pixel > bottom + edgeThresh) return true;
        if (pixel < bottom - edgeThresh) return true;

        return false;
    }

}
