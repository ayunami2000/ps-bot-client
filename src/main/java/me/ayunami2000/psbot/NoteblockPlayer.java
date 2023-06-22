package me.ayunami2000.psbot;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NoteblockPlayer {
    private static String[] instruments=new String[]{"harp","basedrum","snare","hat","bass","flute","bell","guitar","chime","xylophone","iron_xylophone","cow_bell","didgeridoo","bit","banjo","pling"};
    private static BlockPos startingPos = null;
    public static HashMap<Integer, HashMap<Integer, BlockPos>> songLinesToBlocks(String[] songLines){
        HashMap<Integer,HashMap<Integer,BlockPos>> instrNoteToBlock=new HashMap<>();
        int uniqueNotes=0;
        startingPos=PsBot.mc.player.getBlockPos();
        BlockPos centerPos=startingPos.down();
        for (String songLine : songLines) {
            String[] songInfo = songLine.split(":");
            int tick = Integer.parseInt(songInfo[0]);
            int note = Integer.parseInt(songInfo[1]);
            int instr = Integer.parseInt(songInfo[2]);
            int vol = songInfo.length==4?Integer.parseInt(songInfo[3]):127;
            if(!(instrNoteToBlock.containsKey(instr)&&instrNoteToBlock.get(instr).containsKey(note))){
                HashMap<Integer,BlockPos> theVal=instrNoteToBlock.containsKey(instr)?instrNoteToBlock.get(instr):new HashMap<Integer,BlockPos>();
                theVal.put(note,centerPos);
                instrNoteToBlock.put(instr,theVal);
                uniqueNotes++;
            }
        }
		if (PsBot.noteStageMode == 0) {
			AtomicInteger counter = new AtomicInteger();
			//fuck you, add 1 to spiral size. excess is already disposed of, so does it really matter if it only runs once?
			List<int[]> spiralCoords = spiral(1 + Math.min(9, (int) Math.ceil(Math.sqrt(uniqueNotes))));
			List<int[]> spiralCoordsTwo = uniqueNotes > 81 ? spiral(1 + (int) Math.ceil(Math.sqrt(uniqueNotes - 81))) : new ArrayList<int[]>();
			instrNoteToBlock.replaceAll((instr, noteBlockPos) -> {
				noteBlockPos.replaceAll((note, blockPos) -> {
					int currNum = counter.getAndIncrement();
					BlockPos theBlock = blockPos;
					if (currNum >= 81) {
						theBlock = theBlock.add(spiralCoordsTwo.get(currNum - 81)[0] - spiralCoordsTwo.get(0)[0], 3, spiralCoordsTwo.get(currNum - 81)[1] - spiralCoordsTwo.get(0)[1]);
					} else {
						//todo: fix corners
						theBlock = theBlock.add(spiralCoords.get(currNum)[0] - spiralCoords.get(0)[0], 0, spiralCoords.get(currNum)[1] - spiralCoords.get(0)[1]);
						BlockPos offset = theBlock.subtract(blockPos);
						if (Math.abs(offset.getX()) == 4 && Math.abs(offset.getZ()) == 4) {
							theBlock = theBlock.up();
						}
					}
					return theBlock;
				});
				return noteBlockPos;
			});
		} else {
			// Based on https://github.com/Sk8kman/SongPlayer/blob/1651867014873523320cd3c604429040d7a8a609/src/main/java/com/github/hhhzzzsss/songplayer/playing/Stage.java#L87

			int currentInstrIndex = 0;
			List<Integer> instrs = new ArrayList<>(instrNoteToBlock.keySet());
			int currentNoteIndex = 0;
			List<Integer> notes = new ArrayList<>(instrNoteToBlock.get(instrs.get(currentInstrIndex)).keySet());

			int[] yLayers = {-4, -2, -1, 0, 1, 2, 3, 4, 5, 6};
			//UGH

			for (int dx = -5; dx <= 5; dx++) {
				for (int dz = -5; dz <= 5; dz++) {
					for (int dy : yLayers) {
						int adx = Math.abs(dx);
						int adz = Math.abs(dz);
						switch(dy) {
							case -4: {
								if (adx < 3 && adz < 3) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if ((adx == 3 ^ adz == 3) && (adx == 0 ^ adz == 0)) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case -2: { //also takes care of -3
								if (adz == 0 && adx == 0) { //prevents placing int the center
									break;
								}
								if (adz * adx > 9) { //prevents building out too far
									break;
								}
								if (adz + adx == 5 && adx != 0 && adz != 0) {
									//add noteblocks above and below here
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy + 1, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy - 1, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adz * adx == 3) {
									//add noteblocks above and below here
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy + 1, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy - 1, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adx < 3 && adz < 3 && adx + adz > 0) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adz == 0 ^ adx == 0) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adz * adx == 10) { //expecting one to be 2, and one to be 5.
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adz + adx == 6) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case -1: {
								if (adx + adz == 7 || adx + adz == 0) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case 0: {
								int check = adx + adz;
								if ((check == 8 || check == 6) && adx * adz > 5) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case 1: {
								int addl1 = adx + adz;
								if (addl1 == 7 || addl1 == 3 || addl1 == 2) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adx == 5 ^ adz == 5 && addl1 < 7) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (addl1 == 4 && adx * adz != 0) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adx + adz < 7) {
									break;
								}
								break;
							}
							case 2: {
								int addl2 = adx + adz;
								if (adx == 5 || adz == 5) {
									break;
								}
								if (addl2 == 8 || addl2 == 6 || addl2 == 5 || addl2 == 1) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if ((addl2 == 4) && (adx == 0 ^ adz == 0)) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (addl2 == 0) {
									break;
								}
								break;
							}
							case 3: {
								if (adx * adz == 12 || adx + adz == 0) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if ((adx == 5 ^ adz == 5) && (adx < 2 ^ adz < 2)) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								if (adx > 3 || adz > 3) { //don't allow any more checks passed 3 blocks out
									break;
								}
								if (adx + adz > 1 && adx + adz < 5) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case 4: {
								if (adx == 5 || adz == 5) {
									break;
								}
								if (adx + adz == 4 && adx * adz == 0) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								int addl4 = adx + adz;
								if (addl4 == 1 || addl4 == 5 || addl4 == 6) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case 5: {
								if (adx > 3 || adz > 3) {
									break;
								}
								int addl5 = adx + adz;
								if (addl5 > 1 && addl5 < 5) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
							case 6: {
								if (adx + adz < 2) {
									int[] fard = helperFard(instrNoteToBlock, currentInstrIndex, instrs, currentNoteIndex, notes, dx, dy, dz);
									if (fard == null) {
										return instrNoteToBlock;
									}
									currentInstrIndex = fard[0];
									currentNoteIndex = fard[1];
									break;
								}
								break;
							}
						}
						//all breaks lead here
					}
				}
			}
		}
        return instrNoteToBlock;
    }
	private static int[] helperFard(HashMap<Integer, HashMap<Integer, BlockPos>> instrNoteToBlock, int currentInstrIndex, List<Integer> instrs, int currentNoteIndex, List<Integer> notes, int dx, int dy, int dz) {
		HashMap<Integer, BlockPos> theVal = instrNoteToBlock.get(instrs.get(currentInstrIndex));
		theVal.get(notes.get(currentNoteIndex)).add(dx, dy, dz);
		currentNoteIndex++;
		if (currentNoteIndex >= notes.size()) {
			currentInstrIndex++;
			if (currentInstrIndex >= instrs.size()) {
				return null;
			}
			theVal = instrNoteToBlock.get(instrs.get(currentInstrIndex));
			currentNoteIndex = 0;
			notes = new ArrayList<>(theVal.keySet());
		}
		return new int[] { currentInstrIndex, currentNoteIndex };
	}
    public static boolean building=false;
    public static void buildIt(HashMap<Integer, HashMap<Integer, BlockPos>> instrNoteToBlock){
        if(!PsBot.isPlaying)return;
        building=true;
        PsBot.sendChatOrCommand(PsBot.audioMode==2||PsBot.audioMode==4?"/gmc":"/gamemode creative");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        boolean prevNoGravity=PsBot.mc.player.hasNoGravity();
        if(!prevNoGravity)PsBot.mc.player.setNoGravity(true);
        instrNoteToBlock.forEach((instr, noteBlockPos) -> {
            noteBlockPos.forEach((note, blockPos) -> {
				AtomicBoolean done = new AtomicBoolean(false);
				PsBot.mc.execute(() -> {
					if (PsBot.isPlaying) {
						BlockState bs = PsBot.mc.world.getBlockState(instrNoteToBlock.get(instr).get(note));
						boolean skipIt = false;
						if (bs.getBlock() instanceof NoteBlock) {
							Instrument blockInstrument = bs.get(NoteBlock.INSTRUMENT);
							Integer blockNote = bs.get(NoteBlock.NOTE);
							if (instruments[instr].equals(blockInstrument.asString()) && note == blockNote) {//todo: check if this == causes issues
								skipIt = true;
							}
						}
						if (!skipIt) {
							Vec3d currPos = PsBot.mc.player.getPos().add(-0.5, 0, -0.5);
							Vec3d blockPosVec3d = Vec3d.of(blockPos);//new Vec3d(blockPos.getX(),blockPos.getY(),blockPos.getZ())
							double rangeBlockDist = currPos.add(0, PsBot.mc.player.getEyeHeight(PsBot.mc.player.getPose()), 0).distanceTo(blockPosVec3d);
							double blockDist = currPos.add(0, 0.5, 0).distanceTo(blockPosVec3d);
							while ((!PsBot.isPlaying) && !(rangeBlockDist <= PsBot.mc.interactionManager.getReachDistance() + 1.0F && (blockDist >= 1.25F/*1.1313709*/))) {
								PsBot.mc.player.sendMessage(Text.of(String.valueOf(blockDist)), true);
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
								}
								currPos = PsBot.mc.player.getPos().add(-0.5, 0, -0.5);
								blockPosVec3d = Vec3d.of(blockPos);//new Vec3d(blockPos.getX(),blockPos.getY(),blockPos.getZ())
								rangeBlockDist = currPos.add(0, PsBot.mc.player.getEyeHeight(PsBot.mc.player.getPose()), 0).distanceTo(blockPosVec3d);
								blockDist = currPos.add(0, 0.5, 0).distanceTo(blockPosVec3d);
							}
							if (PsBot.isPlaying) {
								ItemStack noteblocks = new ItemStack(Blocks.NOTE_BLOCK);
								NbtCompound noteblocknbt = new NbtCompound();
								NbtCompound noteblockblocknbt = new NbtCompound();
								noteblockblocknbt.putString("instrument", instruments[instr]);
								noteblockblocknbt.putInt("note", note);
								noteblocknbt.put("BlockStateTag", noteblockblocknbt);
								noteblocks.setNbt(noteblocknbt);
								PsBot.loadItem(noteblocks);
								//PsBot.mc.interactionManager.attackBlock(blockPos.up(), Direction.UP);
								PsBot.mc.interactionManager.attackBlock(blockPos, Direction.UP);
								PsBot.mc.player.setPos(startingPos.getX() + 0.5, startingPos.getY(), startingPos.getZ() + 0.5);
								try {
									Thread.sleep(50);
									PsBot.mc.player.setPos(startingPos.getX() + 0.5, startingPos.getY(), startingPos.getZ() + 0.5);
									PsBot.rightClickBlock(blockPos, Direction.UP);
									Thread.sleep(50);
								} catch (InterruptedException e) {
								}
							}
						}
					}
					done.set(true);
				});
				while (!done.get()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
            });
        });
        if(!prevNoGravity)PsBot.mc.player.setNoGravity(false);
        if(PsBot.isPlaying) {
            PsBot.sendChatOrCommand(PsBot.audioMode == 2 || PsBot.audioMode == 4 ? "/gms" : "/gamemode survival");
            Vec3d offsetPos = new Vec3d(startingPos.getX() + 0.5, startingPos.getY(), startingPos.getZ() + 0.5);
            PsBot.sendChatOrCommand("/tp " + offsetPos.getX() + " " + offsetPos.getY() + " " + offsetPos.getZ());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            PsBot.mc.player.setPos(startingPos.getX() + 0.5, startingPos.getY(), startingPos.getZ() + 0.5);
        }
        building = false;
    }
    public static void playIt(HashMap<Integer, HashMap<Integer, BlockPos>> instrNoteToBlock,int instr,int note){
        if(PsBot.audioMode==4||PsBot.audioMode==5) {
            BlockState bs = PsBot.mc.world.getBlockState(instrNoteToBlock.get(instr).get(note));
            if (bs.getBlock() instanceof NoteBlock) {
                Instrument blockInstrument = bs.get(NoteBlock.INSTRUMENT);
                Integer blockNote = bs.get(NoteBlock.NOTE);
                if (instruments[instr].equals(blockInstrument.asString()) && note == blockNote) {
                    PsBot.mc.interactionManager.attackBlock(instrNoteToBlock.get(instr).get(note), Direction.UP);
                    return;
                }
            }
            buildIt(instrNoteToBlock);//does not sleep thread Sadge (also needs creative mode lol)
            while (building) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }else{
            PsBot.mc.interactionManager.attackBlock(instrNoteToBlock.get(instr).get(note), Direction.UP);
        }
    }
    public static List<int[]> spiral(int size){
        List<int[]> result=new ArrayList<int[]>();
        int x = 0; // current position; x
        int y = 0; // current position; y
        int d = 0; // current direction; 0=RIGHT, 1=DOWN, 2=LEFT, 3=UP
        int c = 0; // counter
        int s = 1; // chain size

        // starting point
        x = ((int)Math.floor(size/2.0))-1;
        y = ((int)Math.floor(size/2.0))-1;

        for (int k=1; k<=(size-1); k++)
        {
            for (int j=0; j<(k<(size-1)?2:3); j++)
            {
                for (int i=0; i<s; i++)
                {
                    result.add(new int[]{x,y});
                    c++;

                    switch (d)
                    {
                        case 0: y = y + 1; break;
                        case 1: x = x + 1; break;
                        case 2: y = y - 1; break;
                        case 3: x = x - 1; break;
                    }
                }
                d = (d+1)%4;
            }
            s = s + 1;
        }
        return result;
    }
}
