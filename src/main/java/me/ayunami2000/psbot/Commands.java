package me.ayunami2000.psbot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Commands {
    public static boolean processChatMessage(String message) {
        if (message.startsWith("[{")) {
            String[] parts = message.toLowerCase().substring(2).split(" ", 2);
            String name = parts.length>0 ? parts[0] : "";
            String args = parts.length>1 ? parts[1] : "";
            switch(name){
                case "help":
                    PsBot.chatMsg("Commands: [{openai [{chatregex [{audiobpm [{encchat [{8d [{loop [{autoseek [{help [{audio2textlive [{audio2text [{seek [{joystick [{cb [{kaboom [{play [{stop [{fixcore [{autofixcore [{coresize [{paste [{spam [{video [{rendermode [{pixelmode [{linemode [{audiomode [{audio [{audiovideo [{av [{uno [{vnc [{fixarmorstands [{uuidban [{proxaud [{webview [{imgsize [{screensize [{cmd [{model [{subtitle [{pause [{subtitlemode [{chestgames [{massitemframe [{stagemode".replace(" ","\n"));
                    break;
				case "stagemode":
					PsBot.noteStageMode = (PsBot.noteStageMode + 1) % PsBot.noteStageModes.length;
					PsBot.chatMsg("Note Stage Mode is now " + PsBot.noteStageModes[PsBot.noteStageMode]);
					break;
                case "encchat":
                    PsBot.encChat=!PsBot.encChat;
                    PsBot.chatMsg("Encrypted chat is now "+(PsBot.encChat?"en":"dis")+"abled.");
                    break;
                case "8d":
                    PsBot.filter8d=!PsBot.filter8d;
                    PsBot.chatMsg("8D is now "+(PsBot.filter8d?"en":"dis")+"abled.");
                    break;
                case "loop":
                    PsBot.loop=!PsBot.loop;
                    PsBot.chatMsg("Loop is now "+(PsBot.loop?"en":"dis")+"abled.");
                    break;
                case "autoseek":
                    PsBot.autoSeek=!PsBot.autoSeek;
                    PsBot.chatMsg("AutoSeek is now "+(PsBot.autoSeek?"en":"dis")+"abled.");
                    break;
                case "audio2text":
                case "audio2textlive":
                    if(PsBot.audioToTextThread.enabled){
                        PsBot.chatMsg("Error: Already generating text from audio!");
                    }else {
                        PsBot.audioToTextThread.enabled=true;
                        PsBot.audioToTextThread.deviceName=args;
                        PsBot.audioToTextThread.showPartial=name.equals("audio2textlive");
                        (new Thread(PsBot.audioToTextThread)).start();
                    }
                    break;
                case "seek":
                    if(args.trim().equals("")){
                        PsBot.chatMsg("Current seek value is "+DurationFormatUtils.formatDuration((long)PsBot.seek,"HH:mm:ss,SSS")+"ms");
                    }else {
                        try{
                            double oldSeek=PsBot.seek;
                            PsBot.seek=Double.parseDouble(args.trim());
                            PsBot.chatMsg("Set seek value to "+DurationFormatUtils.formatDuration((long)PsBot.seek,"HH:mm:ss,SSS")+"ms");
                        }catch(NumberFormatException e){
                            PsBot.chatMsg("Error: Invalid seek value!");
                        }
                    }
                    break;
                case "joystick":
                    PsBot.joystickThread.enabled=!PsBot.joystickThread.enabled;
                    if(PsBot.joystickThread.enabled){
                        PsBot.chatMsg("Joystick is now enabled.");
                        (new Thread(PsBot.joystickThread)).start();
                    }else {
                        PsBot.joystickThread.disable();
                        PsBot.chatMsg("Joystick is now disabled.");
                    }
                    break;
                case "cb":
                    if(args.trim().equals("")){
                        PsBot.chatMsg("Usage: [{cb <cmd>");
                    }else {
                        PsBot.runCommand(message.substring(5));
                    }
                    break;
                case "fps":
                    if(args.trim().equals("")){
                        PsBot.chatMsg("FPS is currently "+PsBot.fps);
                    }else {
                        try{
                            PsBot.fps=Long.parseLong(args.trim());
                            PsBot.chatMsg("FPS is now "+PsBot.fps);
                        }catch(NumberFormatException e){
                            PsBot.chatMsg("Error: Not a number!");
                        }
                    }
                    break;
                case "pause":
                    PsBot.paused=!PsBot.paused;
                    PsBot.chatMsg("Everything is now "+(PsBot.paused?"paused":"playing")+".");
                    break;
                case "subtitle":
                    //allow usage while something is already happening
                    PsBot.subtitleThread.alreadySubbing=!PsBot.subtitleThread.alreadySubbing;
                    if(PsBot.subtitleThread.alreadySubbing){
                        //start subtitling
                        //read from file
                        File newFile = PsBot.chooseFile("srt", "Subtitle Files");
                        if (newFile != null) {
                            PsBot.subtitleThread.subtitle = "";
                            try {
                                if (newFile.exists()) {
                                    Scanner myReader = new Scanner(newFile);
                                    while (myReader.hasNextLine()) {
                                        PsBot.subtitleThread.subtitle += "\n" + myReader.nextLine();
                                    }
                                    myReader.close();
                                    PsBot.subtitleThread.subtitle = PsBot.subtitleThread.subtitle.replaceFirst("\n", "");
                                }
                            } catch (IOException e) {
                            }
                            if (PsBot.subtitleThread.subtitle.trim().replaceAll("[\n \\s]", "") == "")
                                PsBot.subtitleThread.subtitle = "";
                            if (PsBot.subtitleThread.subtitle != "") {
                                PsBot.chatMsg("Loaded file " + newFile.getName());
                                (new Thread(PsBot.subtitleThread)).start();
                            } else {
                                PsBot.chatMsg("Error: The file is empty or invalid!");
                                PsBot.subtitleThread.alreadySubbing=false;
                            }
                        } else {
                            PsBot.chatMsg("Error: File not found!");
                            PsBot.subtitleThread.alreadySubbing=false;
                        }
                    }else{
                        //no longer
                        PsBot.chatMsg("No longer subtitling!");
                    }
                    break;
                case "model":
                    if(PsBot.isPlaying) {
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else if(args.equals("cmdblock")||args.equals("cmd")||args.equals("cmdblocks")){
                        PsBot.modelThread.useCmdBlocks=!PsBot.modelThread.useCmdBlocks;
                        PsBot.chatMsg("Model mode will no"+(PsBot.modelThread.useCmdBlocks?"w":" longer")+" use command blocks!");
                    }else if(args.equals("block")||args.equals("blocks")||args.equals("useblocks")){
                        PsBot.modelThread.useBlocks=!PsBot.modelThread.useBlocks;
                        PsBot.chatMsg("Model mode will no"+(PsBot.modelThread.useBlocks?"w":" longer")+" use blocks!");
                    }else if(args.equals("move")||args.equals("moveme")){
                        PsBot.modelThread.moveMe=!PsBot.modelThread.moveMe;
                        PsBot.chatMsg("Model mode will no"+(PsBot.modelThread.moveMe?"w":" longer")+" move the player!");
                    }else {
                        File newFile = PsBot.chooseFile(new String[]{"txt"}, "Voxelizer TXT output");
                        if (newFile != null) {
                            PsBot.modelThread.theModel = "";
                            if (newFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                                try {
                                    PsBot.modelThread.theModel = Files.readString(newFile.toPath(), StandardCharsets.US_ASCII);
                                } catch (IOException e) {}
                            }
                            if (!PsBot.modelThread.theModel.isEmpty()) {
                                PsBot.chatMsg("Loaded file " + newFile.getName());
                                PsBot.isPlaying = true;
                                (new Thread(PsBot.modelThread)).start();
                            } else {
                                PsBot.chatMsg("Error: The file is empty or invalid!");
                            }
                        } else {
                            PsBot.chatMsg("Error: File not found!");
                        }
                    }
                    break;
                case "uuidban":
                    if(args.equals("")){
                        PsBot.chatMsg("Error: please specify UUID or \"username\"/\"uname\" followed by offline username!");
                    }else {
                        ItemStack uuidBan = new ItemStack(Items.ARMOR_STAND);
                        String[] uuidSpl;
                        String dispName="";
                        if (args.contains(" ") && (args.toLowerCase().startsWith("username") || args.toLowerCase().startsWith("uname"))) {
                            String uname = args;
                            dispName=uname+" (offline/cracked)";
                            UUID theUuid = DynamicSerializableUuid.getOfflinePlayerUuid(uname);
                            uuidSpl = theUuid.toString().replace("-", "").split("(?<=\\G.{8})");
                        } else {
                            String uuidOrig = args.replace(" [^ ]*$", "");
                            dispName=uuidOrig;
                            uuidSpl = uuidOrig.replace("-", "").split("(?<=\\G.{8})");
                        }
                        int[] intArr = new int[]{0, 0, 0, 0};
                        try {
                            for (int i = 0; i < uuidSpl.length; i++) intArr[i] = (int)Long.parseLong(uuidSpl[i], 16);
                            String finalUuid = "[I;" + Arrays.stream(intArr).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "]";
                            PsBot.chatMsg(dispName);
                            PsBot.chatMsg(finalUuid);
                            try {
                                uuidBan.setNbt(StringNbtReader.parse("{display:{Name:\"{\\\"text\\\":\\\"" + dispName + "\\\"}\",Lore:[\"by ayunami2000\"]},EntityTag:{UUID:" + finalUuid + "}}"));
                            } catch (CommandSyntaxException e) {
                            }
                            PsBot.loadItem(uuidBan);
                        }catch(NumberFormatException e){
                            PsBot.chatMsg("Error: Invalid UUID!");
                        }
                    }
                    break;
                case "proxaud":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        PsBot.isPlaying=true;
                        PsBot.proximityThread.deviceName=args;
                        (new Thread(PsBot.proximityThread)).start();
                    }
                    break;
				case "massitemframe":
					if(PsBot.isPlaying){
						PsBot.chatMsg("Error: Something is already happening!");
					}else {
						PsBot.isPlaying=true;
						(new Thread(PsBot.massItemFrameThread)).start();
					}
					break;
				case "openai":
					if(args.isEmpty()) {
						if(PsBot.openaiThread.enabled){
							PsBot.openaiThread.enabled = false;
							PsBot.chatMsg("Disabled Openai!");
						}else {
							(new Thread(PsBot.openaiThread)).start();
						}
					}else {
						if(PsBot.openaiThread.enabled){
							return false;
						}else {
							try {
								String[] pieces = args.split(" ", 2);
								if (pieces.length > 0) {
									PsBot.openaiThread.msgLimit = Math.max(0, Integer.parseInt(pieces[0]));
									PsBot.chatMsg("Set message limit!");
									if (pieces.length > 1) {
										PsBot.openaiThread.delay = Math.max(0, Integer.parseInt(pieces[1]));
										PsBot.chatMsg("Set message delay!");
									}
								}
								File keyFile = PsBot.chooseFile(new String[]{"txt"}, "Openai API key in a text file :D");
								if (keyFile == null) {
									PsBot.chatMsg("No API key file selected :(");
								} else {
									try {
										PsBot.openaiThread.apiKey = Files.readString(keyFile.toPath()).trim();
										PsBot.chatMsg("API key file loaded :)");
									} catch (IOException e) {
										PsBot.chatMsg("Unable to load API key file :O");
									}
								}
							} catch (NumberFormatException e) {
								PsBot.chatMsg("Error: Invalid message limit or delay!");
							}
						}
					}
					break;
				case "chatregex":
					if(args.isEmpty()) {
						PsBot.chatMsg("Usage: [{chatregex <custom chat parsing regex.> requires 3 groups: username, commands, and arguments.");
					}else {
						PsBot.customRegex = Pattern.compile(args);
						PsBot.chatMsg("Successfully set custom chat regex!");
					}
					break;
                case "webview":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        PsBot.isPlaying=true;
                        (new Thread(PsBot.webviewThread)).start();
                    }
                    break;
                case "fixarmorstands":
                    if(PsBot.renderMode==6){
                        ImageToChars.preRenderMode();
                        PsBot.chatMsg("Fixed armor stands!");
                    }else{
                        PsBot.chatMsg("Error: Not using Armor Stand render mode!");
                    }
                    break;
                case "imgsize":
                    if(args.equals("")){
                        PsBot.chatMsg("Current image size: "+PsBot.imgSize);
                    }else{
                        try {
                            PsBot.imgSize = Math.max(1,Math.min(500,Integer.parseInt(args)));
                            PsBot.chatMsg("Changed image size to "+PsBot.imgSize);
                        }catch(NumberFormatException e){
                            PsBot.chatMsg("Error: Please enter an integer for the image size (e.g. 40)!");
                        }
                    }
                    break;
                case "screensize":
                    if(args.equals("")){
                        PsBot.chatMsg("Current screen capture size: "+Arrays.stream(PsBot.screenSize).mapToObj(String::valueOf).collect(Collectors.joining(", ")));
                    }else{
                        String[] argList=args.split(" ",4);
                        if(argList.length==4) {
							try {
								PsBot.screenSize = new int[]{Integer.parseInt(argList[0]), Integer.parseInt(argList[1]), Integer.parseInt(argList[2]), Integer.parseInt(argList[3])};
								PsBot.chatMsg("Changed screen capture size to " + PsBot.screenSizeString());
							} catch (NumberFormatException e) {
								PsBot.chatMsg("Error: Please enter an integer for each dimension (e.g. 100)!");
							}
						} else if (argList.length == 1 && (argList[0].equalsIgnoreCase("win") || argList[0].equalsIgnoreCase("window") || argList[0].equalsIgnoreCase("gui"))) {
							PsBot.mc.onWindowFocusChanged(false);
							new ResizableFrame().createAndShowGui();
                        }else {
                            PsBot.chatMsg("Error: Please enter 4 values (X, Y, W, H)!");
                        }
                    }
                    break;
                case "vnc":
                    if(PsBot.vncThread.isVnc){
                        PsBot.vncThread.commandEvent(args);
                    }else {
                        if (PsBot.isPlaying) {
                            PsBot.chatMsg("Error: Something is already happening!");
                        } else {
                            PsBot.isPlaying = true;
                            PsBot.vncThread.ipAndPort = args;
                            (new Thread(PsBot.vncThread)).start();
                        }
                    }
                    break;
                case "uno":
                    if(PsBot.unoThread.isPlayingUno){
                        PsBot.unoThread.command=args;
                    }else{
                        if(PsBot.isPlaying){
                            PsBot.chatMsg("Error: Something is already happening!");
                        }else {
                            PsBot.isPlaying=true;
                            PsBot.unoThread.command=args;
                            (new Thread(PsBot.unoThread)).start();
                        }
                    }
                    break;
                case "paste":
                    Clipboard c= Toolkit.getDefaultToolkit().getSystemClipboard();
                    if(c.isDataFlavorAvailable(DataFlavor.imageFlavor)){
                        try {
                            Image img = (Image)c.getData(DataFlavor.imageFlavor);
                            if(PsBot.renderModeUsesCommandBlocks()) {
                                PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
                                PsBot.fixCommandBlocks();
                                Thread.sleep(500);
                            }
                            if(ImageToChars.preRenderMode()) {
                                String textNbt = ImageToChars.imageRender(ImageToChars.convertImageToBuffered(img));
                                for (String cmd : ImageToChars.textToCommands(textNbt)) PsBot.runCommand(cmd);
                            }else{
                                PsBot.chatMsg("Error: Something went wrong during the rendering preparation process!");
                            }
                        }catch(UnsupportedFlavorException | IOException | InterruptedException e){}
                    }else{
                        PsBot.chatMsg("Error: No image was found on your clipboard!");
                    }
                    break;
                case "spam":
                    if(PsBot.isPlaying) {
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else if(args.trim().equals("")){
                        PsBot.chatMsg("Usage: [{spam [delay] <cmd>");
                    }else {
                        PsBot.isPlaying=true;
                        PsBot.spamThread.cmd=message.substring(7);
                        (new Thread(PsBot.spamThread)).start();
                    }
                    break;
                case "av":
                case "audiovideo":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        PsBot.isPlaying=true;
                        PsBot.audioVideoThread.devices=args;
                        (new Thread(PsBot.audioVideoThread)).start();
                    }
                    break;
                case "audiobpm":
                    try {
                        PsBot.audioThread.bpmTypeNumber = Math.max(2,Integer.parseInt(args));
                        PsBot.chatMsg("Set audio bpm thingy to "+PsBot.audioThread.bpmTypeNumber);
                    }catch(NumberFormatException e){
                        PsBot.chatMsg("Error: That is not a valid integer!");
                    }
                    break;
                case "audio":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        PsBot.isPlaying=true;

                        PsBot.audioThread.deviceName=args;
                        (new Thread(PsBot.audioThread)).start();
                    }
                    break;
                case "video":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        PsBot.isPlaying=true;
                        PsBot.streamThread.webcamName=args;
                        (new Thread(PsBot.streamThread)).start();
                    }
                    break;
                case "rendermode":
                    PsBot.renderMode=(++PsBot.renderMode)%PsBot.renderModes.length;
                    PsBot.chatMsg("Changed render mode to "+PsBot.renderModes[PsBot.renderMode]);
                    break;
                case "pixelmode":
                    PsBot.pixelMode=(++PsBot.pixelMode)%PsBot.pixelModes.length;
                    PsBot.chatMsg("Changed pixel mode to "+PsBot.pixelModes[PsBot.pixelMode]);
                    break;
                case "linemode":
                    PsBot.lineMode=(++PsBot.lineMode)%PsBot.lineModes.length;
                    PsBot.chatMsg("Changed line mode to "+PsBot.lineModes[PsBot.lineMode]);
                    break;
                case "audiomode":
                    PsBot.audioMode=(++PsBot.audioMode)%PsBot.audioModes.length;
                    PsBot.chatMsg("Changed audio mode to "+PsBot.audioModes[PsBot.audioMode]);
                    break;
                case "subtitlemode":
                    PsBot.subtitleMode=(++PsBot.subtitleMode)%PsBot.subtitleModes.length;
                    PsBot.chatMsg("Changed subtitle mode to "+PsBot.subtitleModes[PsBot.subtitleMode]);
                    break;
                case "chestgames":
                    if (args.equals("start")) {
                        if(PsBot.isPlaying){
                            PsBot.chatMsg("Error: Something is already happening!");
                        }else {
                            PsBot.isPlaying=true;
                            (new Thread(PsBot.chestGamesThread)).start();
                        }
                    } else {
                        PsBot.chestGameMode=(++PsBot.chestGameMode)%PsBot.chestGameModes.length;
                        PsBot.chatMsg("Changed chest game mode to "+PsBot.chestGameModes[PsBot.chestGameMode]);
                    }
                    break;
                case "kaboom":
                    PsBot.isKaboom=!PsBot.isKaboom;
                    PsBot.chatMsg("Kaboom mode is now "+(PsBot.isKaboom?"enabled":"disabled")+".");
                    break;
                case "fixcore":
                    if(PsBot.originPos==null){
                        PsBot.chatMsg("Error: No origin position has been set!");
                    }else{
                        PsBot.fixCommandBlocks();
                        PsBot.chatMsg("Fixed core!");
                    }
                    break;
                case "coresize":
                    if(args.equals("")){
                        PsBot.chatMsg("Current core size: "+PsBot.coreSize);
                    }else{
                        try {
                            PsBot.coreSize = Math.max(1,Math.min(100,Integer.parseInt(args)));
                            PsBot.chatMsg("Changed core size to "+PsBot.coreSize);
                        }catch(NumberFormatException e){
                            PsBot.chatMsg("Error: Please enter an integer for the core size (e.g. 8)!");
                        }
                    }
                    break;
                case "autofixcore":
                    PsBot.fixCoreThread.auto=!PsBot.fixCoreThread.auto;
                    if(PsBot.fixCoreThread.auto)(new Thread(PsBot.fixCoreThread)).start();
                    PsBot.chatMsg("Auto core repair mode is now "+(PsBot.fixCoreThread.auto?"enabled":"disabled")+".");
                    break;
                case "stop":
                    if(PsBot.vncThread.isVnc) PsBot.vncThread.isVnc = false;
                    if(PsBot.unoThread.isPlayingUno) PsBot.unoThread.endGame();
                    PsBot.isPlaying=false;
                    PsBot.subtitleThread.alreadySubbing=false;
                    PsBot.joystickThread.disable();
                    PsBot.audioToTextThread.enabled=false;
                    PsBot.seek=0;
                    if(ProximityThread.ws!=null) {
                        try {
                            ProximityThread.ws.stop(1);
                        } catch (InterruptedException e) {
                        }
                    }
                    ProximityThread.ws=null;
					if(PsBot.openaiThread.enabled) PsBot.openaiThread.enabled = false;
                    PsBot.chatMsg("Stopped.");
                    break;
                case "play":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        File newFile = PsBot.chooseFile(new String[]{"txt", "nbs", "mid", "midi"}, "Notebot Songs");
                        if (newFile != null) {
                            PsBot.playThread.theSong = "";
                            if (newFile.getName().toLowerCase(Locale.ROOT).endsWith(".nbs")) {
                                PsBot.playThread.theSong = ConvertNBS.doLiveConvert(newFile);
                            } else if (newFile.getName().toLowerCase(Locale.ROOT).endsWith(".mid") || newFile.getName().toLowerCase(Locale.ROOT).endsWith(".midi")) {
                                String midiTxtSong = MidiConverter.midiToTxt(newFile);
                                //set 4th value to velocity
                                PsBot.playThread.theSong = midiTxtSong;
                            } else {
                                try {
                                    PsBot.playThread.theSong = "";
                                    if (newFile.exists()) {
                                        Scanner myReader = new Scanner(newFile);
                                        while (myReader.hasNextLine()) {
                                            PsBot.playThread.theSong += "\n" + myReader.nextLine();
                                        }
                                        myReader.close();
                                        PsBot.playThread.theSong = PsBot.playThread.theSong.replaceFirst("\n", "");
                                    }
                                } catch (IOException e) {
                                }
                                if (PsBot.playThread.theSong.trim().replaceAll("[\n \\s]", "") == "")
                                    PsBot.playThread.theSong = "";
                            }
                            if (PsBot.playThread.theSong.isEmpty()) {
                                PsBot.chatMsg("Loaded file " + newFile.getName());
                                PsBot.isPlaying = true;
                                (new Thread(PsBot.playThread)).start();
                            } else {
                                PsBot.chatMsg("Error: The file is empty or invalid!");
                            }
                        } else {
                            PsBot.chatMsg("Error: File not found!");
                        }
                    }
                    break;
                case "cmd":
                    if(PsBot.isPlaying){
                        PsBot.chatMsg("Error: Something is already happening!");
                    }else {
                        File newFile = PsBot.chooseFile(new String[]{"pscmd"}, "PsCmd files");
                        if (newFile != null) {
                                try {
                                    PsBot.cmdThread.theCmd = "";
                                    if (newFile.exists()) {
                                        Scanner myReader = new Scanner(newFile,"UTF-8");
                                        while (myReader.hasNextLine()) {
                                            PsBot.cmdThread.theCmd += "\n" + myReader.nextLine();
                                        }
                                        myReader.close();
                                        PsBot.cmdThread.theCmd = PsBot.cmdThread.theCmd.replaceFirst("\n", "");
                                    }
                                } catch (IOException e) {
                                }
                                if (PsBot.cmdThread.theCmd.trim().replaceAll("[\n \\s]", "") == "")
                                    PsBot.cmdThread.theCmd = "";
                            if (PsBot.cmdThread.theCmd != "") {
                                PsBot.chatMsg("Loaded file " + newFile.getName());
                                PsBot.isPlaying = true;
                                (new Thread(PsBot.cmdThread)).start();
                            } else {
                                PsBot.chatMsg("Error: The file is empty or invalid!");
                            }
                        } else {
                            PsBot.chatMsg("Error: File not found!");
                        }
                    }
                    break;
                default:
                    PsBot.chatMsg("Error: invalid command!");
            }
            return true;
        }
        return false;
    }
}
