package me.ayunami2000.psbot;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class FixCoreThread implements Runnable{
    public static boolean auto=false;

    @Override
    public void run() {
        try {
            while(auto) {
                if(PsBot.originPos!=null){
                    for(int x=0;x<PsBot.coreSize;x++){
                        for(int y=0;y<PsBot.coreSize;y++){
                            BlockPos cmdBlockPos = PsBot.originPos.add(x - Math.floor(PsBot.coreSize/2.0), 0, y - Math.floor(PsBot.coreSize/2.0));
                            BlockState cmdBlockState = PsBot.mc.world.getBlockState(cmdBlockPos);
                            if(cmdBlockState==null||!cmdBlockState.isOf(PsBot.isKaboom?Blocks.REPEATING_COMMAND_BLOCK:Blocks.COMMAND_BLOCK)){
                                PsBot.fixCommandBlocks();
                            }
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException | NullPointerException e) {
            auto=false;
            PsBot.chatMsg("Auto mode disabled due to error.");
        }
    }
}
