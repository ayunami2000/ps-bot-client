package me.ayunami2000.psbot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PsBot implements ModInitializer {
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static PlayThread playThread = new PlayThread();
    public static CmdThread cmdThread = new CmdThread();
    public static StreamThread streamThread = new StreamThread();
    public static SpamThread spamThread = new SpamThread();
    public static AudioThread audioThread = new AudioThread();
    public static PitchDetection pitchDetection = new PitchDetection();
    public static AudioVideoThread audioVideoThread = new AudioVideoThread();
    public static FixCoreThread fixCoreThread = new FixCoreThread();
    public static UnoThread unoThread = new UnoThread();
    public static VncThread vncThread = new VncThread();
    public static ProximityThread proximityThread = new ProximityThread();
    public static WebviewThread webviewThread = new WebviewThread();
    public static ModelThread modelThread = new ModelThread();
    public static SubtitleThread subtitleThread = new SubtitleThread();
    public static JoystickThread joystickThread = new JoystickThread();
    public static AudioToTextThread audioToTextThread = new AudioToTextThread();
    public static ChestGamesThread chestGamesThread = new ChestGamesThread();
	public static MassItemFrameThread massItemFrameThread = new MassItemFrameThread();
	public static OpenaiThread openaiThread = new OpenaiThread();
    public static boolean isPlaying=false;
    public static boolean isKaboom=false;
	public static int noteStageMode=0;
	public static String[] noteStageModes=new String[] {
			"Default",
			"Sphere"
	};
    public static boolean encChat=false;
    public static BlockPos originPos=null;
    public static Robot robot = null;
    public static int cmdPerTick = 0;
    public static int coreSize = 8;
    public static int imgSize = 40;
    public static int[] screenSize = new int[]{0,0,0,0};
    public static long fps = 20;
    public static double seek = 0;
    public static boolean paused = false;
    public static boolean autoSeek=false;
    public static boolean loop=false;
    public static boolean filter8d=false;
    public static int renderMode=0;
    public static String[] renderModes=new String[]{
            "Tellraw",
            "More Pixels Tellraw",
            "Particles",
            "Item Lore (1.16)",
            "Item Lore (1.17)",
            "Item Lore (No Command)",
            "Armor Stands",
            "Armor Stands (Scanlines)",
            "Armor Stands (Alternating)",
            "Area Effect Clouds",
            "Lectern",
            "Chat",
            "Chest"
    };
    public static int pixelMode=0;
    public static String[] pixelModes=new String[]{
            "Blocks",
            "Braille"
    };
    public static int lineMode=0;
    public static String[] lineModes=new String[]{
            "One",
            "Multi"
    };
    public static int audioMode=0;
    public static String[] audioModes=new String[]{
            "Playsound",
            "Playsound (1.5.2)",
            "Noteblocks (Essentials)",
            "Noteblocks (Vanilla)",
            "Noteblocks (Essentials) (Autorepair)",
            "Noteblocks (Vanilla) (Autorepair)"
    };
    public static int subtitleMode=0;
    public static String[] subtitleModes=new String[]{
            "Default",
            "Chat"
    };
    public static int chestGameMode=0;
    public static String[] chestGameModes=new String[]{
            "Screen",
            "Dino"
    };
    public static Pattern chatRegex=Pattern.compile("^(?:\\\\[.+\\\\]|.)*\\s(.{1,16})\\s?[>:\\-»\\])\\[\\{]+\\s\\[\\{(\\w*)\\s?(.*)"),
            vanillaRegex=Pattern.compile("<(.{1,16})>\\s\\[\\{(\\w*)\\s?(.*)"),
			customRegex=null;
    public static HttpServer httpServer=null;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        System.setProperty("java.awt.headless", "false");
        try {
            robot=new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        ClientTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8469), 0);
            httpServer.createContext("/", new ReqHandler());
            httpServer.setExecutor(null);
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        (new Thread(() -> {
            long lastTime = Instant.now().toEpochMilli();
            while(true){
                if(PsBot.isPlaying||PsBot.subtitleThread.alreadySubbing){
                    long nownow = Instant.now().toEpochMilli();
                    long timeSpent = nownow - lastTime;
                    if(!PsBot.paused)PsBot.seek+=timeSpent;
                    lastTime = nownow;
                    long timeToSleep = 1000L / PsBot.fps - timeSpent;//use fps for timer speed
                    if (timeToSleep > 0) {
                        try {
                            Thread.sleep(timeToSleep);
                        } catch (InterruptedException e) {}
                    }
                }else{
                    if(PsBot.seek!=0)PsBot.seek=0;
                }
                Thread.onSpinWait();
            }
        })).start();
        System.out.println("Using ps-bot by ayunami2000!");
    }

    private static class ReqHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if(t.getRequestURI().getPath().toLowerCase().startsWith("/prox")&&proximityThread.isProx){
                proximityThread.handleHttp(t);
            }else if(t.getRequestURI().getPath().toLowerCase().startsWith("/webview")&&webviewThread.isWebview){
                webviewThread.handleHttp(t);
            }else{
                String response = "ps-bot Minecraft client/bot by ayunami2000";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    public static void fixCommandBlocks(){
        if (originPos == null) originPos = mc.player.getBlockPos().withY(0);
        PsBot.sendChatOrCommand("/fill " + (originPos.getX() - (int) Math.floor(coreSize / 2.0)) + " " + originPos.getY() + " " + (originPos.getZ() - (int) Math.floor(coreSize / 2.0)) + " " + (originPos.getX() + (int) Math.ceil(coreSize / 2.0)) + " " + originPos.getY() + " " + (originPos.getZ() + (int) Math.ceil(coreSize / 2.0)) + " " + (isKaboom ? "repeating_" : "") + "command_block");
    }

    private static int lastCmdBlock = 0;

    public static void runCommand(String cmd){
        try {
            if (originPos == null) originPos = mc.player.getBlockPos().withY(0);
            BlockPos cmdBlockPos = originPos.add(Math.floor(lastCmdBlock/((double)coreSize)) - Math.floor(coreSize/2.0), 0, (lastCmdBlock%coreSize) - Math.floor(coreSize/2.0));
            lastCmdBlock++;
            CommandBlockBlockEntity cmdBlockEntity = (CommandBlockBlockEntity) PsBot.mc.world.getBlockEntity(cmdBlockPos);
            if(cmdBlockEntity!=null) {
                CommandBlockExecutor cmdBlockExecutor = cmdBlockEntity.getCommandExecutor();
                if (isKaboom) {
                    mc.getNetworkHandler().sendPacket(new UpdateCommandBlockC2SPacket(cmdBlockPos, cmd, CommandBlockBlockEntity.Type.AUTO, cmdBlockExecutor.isTrackingOutput(), false, true));
                } else {
                    mc.getNetworkHandler().sendPacket(new UpdateCommandBlockC2SPacket(cmdBlockPos, "Using ps-bot by ayunami2000!", CommandBlockBlockEntity.Type.REDSTONE, cmdBlockExecutor.isTrackingOutput(), false, false));
                    mc.getNetworkHandler().sendPacket(new UpdateCommandBlockC2SPacket(cmdBlockPos, cmd, CommandBlockBlockEntity.Type.REDSTONE, cmdBlockExecutor.isTrackingOutput(), false, true));
                }
            }
            lastCmdBlock = lastCmdBlock % (coreSize * coreSize);
            cmdPerTick++;
        }catch(NullPointerException e){
            isPlaying=false;
            subtitleThread.alreadySubbing=false;
            joystickThread.disable();
        }
    }

    public static boolean renderModeUsesCommandBlocks(){
        return renderMode!=5&&renderMode!=10&&renderMode!=11&&renderMode!=12;
    }

    public static void rightClickBlock(BlockPos block, Direction side){
		mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(block), side, block, false));
		mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
    public static void clearHotbar(){
        int prevSelectedSlot=mc.player.getInventory().selectedSlot;
        for (int o=0;o<9;o++) {
            mc.player.getInventory().selectedSlot=o;
            mc.player.clearActiveItem();
            mc.player.getInventory().setStack(o, new ItemStack(Blocks.AIR));
        }
        mc.player.getInventory().selectedSlot=prevSelectedSlot;
        //must be updated after using updateInventory()
    }
    public static void updateInventory() {
        mc.player.getInventory().updateItems();
        mc.setScreen(new InventoryScreen(mc.player));
        mc.player.closeScreen();
    }
    public static void loadItem(ItemStack itemStack){
        loadItem(itemStack,mc.player.getInventory().selectedSlot);
    }
    public static void loadItem(ItemStack itemStack,int slot){
        mc.interactionManager.clickCreativeStack(itemStack,slot+36);
        //mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot+36,itemStack));
    }

    private void onWorldTick(World world) {
        if(cmdPerTick>coreSize*coreSize){
            PsBot.mc.player.sendMessage(Text.of("WARNING: You are sending more commands than the core size!"),true);
        }
        cmdPerTick=0;

        for (BlockPos blockPos : ModelThread.tickQueue) {
            rightClickBlock(blockPos,Direction.UP);
        }
        modelThread.tickQueue.clear();
    }

    public static void onChatMessage(Text message) {
        String trimMsg=message.getString();
        //if(trimMsg.length()>500)trimMsg=trimMsg.substring(0,500);
		if(trimMsg.length()>2048)trimMsg=trimMsg.substring(0,2048);
        Matcher m;
		String username=null,
				command=null,
				arguments=null;
		if (customRegex == null) {
			m = chatRegex.matcher(trimMsg);
			if(m.find()){
				//command
				username = m.group(1);
				command = m.group(2);
				for (int i = 3; i <= m.groupCount(); i++) {
					String ma = m.group(i);
					if (ma != null) {
						if (arguments == null) arguments = "";
						arguments += ma;
					}
				}
			}
			m=vanillaRegex.matcher(trimMsg);
			if(m.find()){
				//vanilla command
				username = m.group(1);
				command = m.group(2);
				for (int i = 3; i <= m.groupCount(); i++) {
					String ma = m.group(i);
					if (ma != null) {
						if (arguments == null) arguments = "";
						arguments += ma;
					}
				}
			}
		} else {
			m = customRegex.matcher(trimMsg);
			if (m.find()) {
				//custom command
				username = m.group(1);
				command = m.group(2);
				for (int i = 3; i <= m.groupCount(); i++) {
					String ma = m.group(i);
					if (ma != null) {
						if (arguments == null) arguments = "";
						arguments += ma;
					}
				}
			}
		}
        if(username!=null&&command!=null&&arguments!=null){
			arguments = arguments.replaceAll("(?:§[0-9a-fA-FkKlLmMnNoOrR]|§[xX](?:§.){6})", "");
            //COMMAND HAPPENED
            switch(command){
                case "uno":
                    if(PsBot.unoThread.isPlayingUno)PsBot.unoThread.command=arguments;
                    break;
                case "vnc":
                    if(PsBot.vncThread.isVnc)PsBot.vncThread.commandEvent(arguments);
                    break;
                case "joystick":
                    //fard
                    if(PsBot.joystickThread.enabled)PsBot.joystickThread.joystickEvent(arguments);
                    break;
				case "openai":
					if(PsBot.openaiThread.enabled){
						// PsBot.openaiThread.unm=username;
						PsBot.openaiThread.cmd=arguments;
					}
					break;
                default:
            }
        }
    }

    public static String ultraTrim(String s){
        return s.trim().replaceAll("^\\s+|\\s+$","");
    }

    public static void chatMsg(String s){
        mc.player.sendMessage(Text.of(s),false);
    }

    public static File chooseFile(String filter, String filterName){
        return chooseFile(new String[]{filter},filterName);
    }
    public static File chooseFile(String[] filters,String filterName){
        File fileResult=showOpenFileDialog("Select File to Open",mc.runDirectory,filterName,filters);
        return fileResult;
    }
    //originally from LWJGUI at https://github.com/orange451/LWJGUI
    private static File showOpenFileDialog(String title, File defaultPath, String filterDescription, String[] acceptedFileExtensions){

        MemoryStack stack = MemoryStack.stackPush();

        PointerBuffer filters = stack.mallocPointer(acceptedFileExtensions.length);

        for(int i = 0; i < acceptedFileExtensions.length; i++){
            filters.put(stack.UTF8("*." + acceptedFileExtensions[i]));
        }

        filters.flip();

        defaultPath = defaultPath.getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
            defaultString += File.separator;
        }

        String result = TinyFileDialogs.tinyfd_openFileDialog(title, defaultString, filters, filterDescription, false);

        stack.pop();

        return result != null ? new File(result) : null;
    }
    public static File saveFile(String ext,String desc){
        return showSaveFileDialog("Save file as...",mc.runDirectory,desc,ext,true);
    }
    public static File showSaveFileDialog(String title, File defaultPath, String filterDescription, String fileExtension, boolean forceExtension){

        MemoryStack stack = MemoryStack.stackPush();

        PointerBuffer filters = stack.mallocPointer(1);

        filters.put(stack.UTF8("*." + fileExtension)).flip();

        defaultPath = defaultPath.getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
            defaultString += File.separator;
        }

        String result = TinyFileDialogs.tinyfd_saveFileDialog(title, defaultString, filters, filterDescription);

        stack.pop();

        if(result == null){
            return null;
        }

        if(forceExtension && !result.endsWith("." + fileExtension)){
            result += "." + fileExtension;
        }

        return new File(result);
    }

	public static void sendChatOrCommand(String msg) {
		if (msg.startsWith("/")) {
			mc.player.sendCommand(msg.substring(1));
		} else {
			mc.player.sendChatMessage(msg, null);
		}
	}

	public static String screenSizeString() {
		return Arrays.stream(screenSize).mapToObj(String::valueOf).collect(Collectors.joining(", "));
	}
}
