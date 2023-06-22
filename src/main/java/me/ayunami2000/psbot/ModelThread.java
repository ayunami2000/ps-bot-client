package me.ayunami2000.psbot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ModelThread implements Runnable{
    public static String theModel="";
    public static boolean useCmdBlocks=false;
    public static boolean useBlocks=false;
    public static boolean moveMe=false;
    public static List<BlockPos> tickQueue=new ArrayList<>();

    @Override
    public void run() {
        if(useCmdBlocks){
            PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
            PsBot.fixCommandBlocks();
        }
        theModel=theModel.trim();
        String[] lines=theModel.split("\n");
        Vec3i facingVec=PsBot.mc.player.getHorizontalFacing().getVector();
        Vec3d plpos=PsBot.mc.player.getPos().add(Vec3d.of(facingVec).multiply(2));
        BlockPos plbpos=PsBot.mc.player.getBlockPos();
        List<Integer[]> points=new ArrayList<>();
        //sort
        for(int i=0;i<lines.length;i++){
            String[] lineParts=lines[i].split(", ");
            int x=0;
            int y=0;
            int z=0;
            int r=255;
            int g=255;
            int b=255;
            try{
                x=Integer.parseInt(lineParts[0]);
                y=Integer.parseInt(lineParts[1]);
                z=Integer.parseInt(lineParts[2]);
                if(lineParts.length>3) {
                    r = Integer.parseInt(lineParts[3]);
                    g = Integer.parseInt(lineParts[4]);
                    b = Integer.parseInt(lineParts[5]);
                }
            }catch(NumberFormatException e){
                i=lines.length;
                break;
            }
            points.add(new Integer[]{x,y,z,r,g,b});
        }
        points.sort(Comparator.comparing(p->{
            int d=0,i=0;
            while(i<3)d+=(d=p[i++])*d;
            return d;
        }));
        if(useBlocks&&!useCmdBlocks){
            PsBot.loadItem(new ItemStack(Items.POWDER_SNOW_BUCKET));
            HashMap<Integer[],Boolean> pointsBuilt=new HashMap<>();
            for (Integer[] point : points) {
                pointsBuilt.put(point,false);
            }
            while(pointsBuilt.containsValue(false)){
                Vec3d currPoss = PsBot.mc.player.getPos().add(-0.5, 0, -0.5);
                int[] B=new int[]{(int) currPoss.x, (int) currPoss.y, (int) currPoss.z};
                points.sort(Comparator.comparing(p->{
                    int d=0,i=0;
                    while(i<3)d+=(d=p[i]+B[i++])*d;
                    return d;
                }));
                boolean builtAnything=false;
                for (Integer[] point : points) {
                    if (!PsBot.isPlaying) break;
                    int x = point[0];
                    int y = point[1];
                    int z = point[2];
                    int r = point[3];
                    int g = point[4];
                    int b = point[5];
                    BlockPos pos = plbpos.add(x, y, z);
                    Vec3d currPos = PsBot.mc.player.getPos().add(-0.5, 0, -0.5);
                    Vec3d blockPosVec3d = Vec3d.of(pos);
                    double rangeBlockDist = currPos.add(0, PsBot.mc.player.getEyeHeight(PsBot.mc.player.getPose()), 0).distanceTo(blockPosVec3d);
                    double blockDist = currPos.add(0, 0.5, 0).distanceTo(blockPosVec3d);
                    PsBot.mc.player.sendMessage(Text.of(blockDist +" | "+pointsBuilt.values().stream().filter(lmao -> lmao).count()+"/"+pointsBuilt.values().size()), true);
                    if (rangeBlockDist <= PsBot.mc.interactionManager.getReachDistance() + 1.0F && blockDist >= 1.25/*1.1313709*/) {
                        tickQueue.add(pos);
                        pointsBuilt.put(point, true);
                        builtAnything=true;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {}
                    }
                }
                if(!PsBot.isPlaying)break;
                if(!builtAnything&&moveMe&&points.size()>0){
                    //sort again to ensure we move to the most optimal location
                    currPoss = PsBot.mc.player.getPos().add(-0.5, 0, -0.5);
                    points.sort(Comparator.comparing(p->{
                        int d=0,i=0;
                        while(i<3)d+=(d=p[i]+B[i++])*d;
                        return d;
                    }));
                    BlockPos pos = plbpos.add(points.get(0)[0], points.get(0)[1], points.get(0)[2]);
                    Vec3d fard = Vec3d.of(pos).add(1.5, 1.5, 1.5).subtract(currPoss);
                    PsBot.mc.player.setVelocity(0.05 * fard.x, 0.05 * fard.y, 0.05 * fard.z);
                }
                pointsBuilt.forEach((point, built) -> {
                    if(built)points.remove(point);
                });
                if (!PsBot.isPlaying) break;
            }
        }else {
            for (int i = 0; i < points.size(); i++) {
                int x = points.get(i)[0];
                int y = points.get(i)[1];
                int z = points.get(i)[2];
                int r = points.get(i)[3];
                int g = points.get(i)[4];
                int b = points.get(i)[5];
                if (useBlocks) {
                    BlockPos pos = plbpos.add(x, y, z);
                    if (useCmdBlocks) {
                        String theCmd = "setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " white_concrete";
                        PsBot.runCommand(theCmd);
                    } else {
                        //never true
                    }
                } else {
                    Vec3d pos = new Vec3d(x, y, z).multiply(0.2).add(plpos);
                    String theNbt = "Rotation:[0.0f,0.0f],Pose:{LeftLeg:[0f,0f,10f],RightLeg:[0f,0f,-10f]},Small:1b,Invisible:1b,NoGravity:1b,ArmorItems:[{id:\"leather_boots\",Count:1b,tag:{display:{color:" + (r * 256 * 256 + g * 256 + b) + "}}},{},{},{}],PersistenceRequired:1b";
                    if (useCmdBlocks) {
                        PsBot.runCommand("/summon armor_stand " + pos.x + " " + pos.y + " " + pos.z + " {" + theNbt + "}");
                    } else {
                        ItemStack theItem = new ItemStack(Items.ARMOR_STAND);
                        try {
                            theItem.setNbt(StringNbtReader.parse("{EntityTag:{Health:0f,DeathTime:99999,Pos:[" + pos.x + "," + pos.y + "," + pos.z + "]," + theNbt + "}}"));
                        } catch (CommandSyntaxException e) {
                        }
                        PsBot.loadItem(theItem);
                        PsBot.rightClickBlock(PsBot.mc.player.getBlockPos().add(facingVec), Direction.UP);
                    }
                }
                try {
                    Thread.sleep(useCmdBlocks ? 5 : 100);
                } catch (InterruptedException e) {
                }
                if (!PsBot.isPlaying) i = points.size();
            }
        }
        PsBot.chatMsg("Finished building model!");
    }
}