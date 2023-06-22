package me.ayunami2000.psbot;

import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;
import java.util.regex.Pattern;

public class SpamThread implements Runnable{
    public static String cmd="";
    public static Random rand=new Random();
    private static Pattern patternRand=Pattern.compile("%rand%");
    private static Pattern patternRandBoth=Pattern.compile("%randboth%");
    private static Pattern patternRandNum=Pattern.compile("%rand(\\d+)%");
    private static Pattern patternRandNumBoth=Pattern.compile("%rand(\\d+)both%");

    @Override
    public void run() {
        if(cmd.equals("")){
            PsBot.chatMsg("Error: Please specify the command!");
            PsBot.isPlaying=false;
        }else{
            try {
                int delay=1;
                if(cmd.contains(" ")){
                    String[] cmdp=cmd.split(" ",2);
                    try{
                        delay=Integer.parseInt(cmdp[0]);
                        cmd=cmdp[1];
                    }catch(NumberFormatException e){}
                }
				boolean isPaddle = cmd.equalsIgnoreCase("paddle");
				boolean isSwing = cmd.equalsIgnoreCase("swing");
				if (!isPaddle && !isSwing) {
					PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
					PsBot.fixCommandBlocks();
				}

				boolean swingHand = false;

                while(PsBot.isPlaying){
					if (isPaddle) {
						if (PsBot.mc.world != null) {
							PsBot.mc.world.sendPacket(new BoatPaddleStateC2SPacket(rand.nextBoolean(), rand.nextBoolean()));
						}
					} else if (isSwing) {
						PsBot.mc.player.swingHand(swingHand ? Hand.MAIN_HAND : Hand.OFF_HAND);
						swingHand = !swingHand;
					} else {
						String finalCmd = cmd;
						finalCmd = patternRand.matcher(finalCmd).replaceAll(mr -> "" + rand.nextFloat(1));
						finalCmd = patternRandBoth.matcher(finalCmd).replaceAll(mr -> "" + rand.nextFloat(-1, 1));
						finalCmd = patternRandNum.matcher(finalCmd).replaceAll(mr -> "" + rand.nextInt(Integer.parseInt(mr.group(1)) + 1));
						finalCmd = patternRandNumBoth.matcher(finalCmd).replaceAll(mr -> "" + rand.nextInt(-Integer.parseInt(mr.group(1)), Integer.parseInt(mr.group(1)) + 1));
						PsBot.runCommand(finalCmd);
					}
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                PsBot.isPlaying=false;
            }
        }
    }
}
