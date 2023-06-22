package me.ayunami2000.psbot;

import me.ayunami2000.psbot.joystick.Joystick;
/*import me.ayunami2000.psbot.joystick.JoystickException;
import org.json.JSONException;
import org.json.JSONObject;*/

public class JoystickThread implements Runnable{
    public static volatile boolean enabled=false;

    /*private static Joystick joystick=null;*/

    @Override
    public void run() {
        /*
        try {
            joystick = new Joystick();
            if(PsBot.renderModeUsesCommandBlocks()) {
                PsBot.originPos = PsBot.mc.player.getBlockPos().withY(0);
                PsBot.fixCommandBlocks();
            }
            int secCounter=0;
            while (enabled) {
                joystick.send();
                if(secCounter++==10){
                    secCounter=0;
                    if(PsBot.renderModeUsesCommandBlocks()){
                        PsBot.runCommand("tellraw @a [\" \",{\"text\":\"⬆\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{joystick {\\\"analog\\\":{\\\"1\\\":1}}\"}},\"\\n\",{\"text\":\"⬅\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{joystick {\\\"analog\\\":{\\\"0\\\":1}}\"}},{\"text\":\"➡\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{joystick {\\\"analog\\\":{\\\"0\\\":32767}}\"}},\"\\n \",{\"text\":\"⬇\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"[{joystick {\\\"analog\\\":{\\\"1\\\":32767}}\"}}]");
                    }
                }
                Thread.sleep(100);//sends this often
            }
            disable();
        } catch (JoystickException | InterruptedException e) {
            if(e instanceof JoystickException) {
                e.printStackTrace();
                disable();
            }
        }
        */
        enabled=false;
    }

    public static void joystickEvent(String in){
        /*
        if(joystick==null)return;
        JSONObject joydata = null;
        try{
            joydata=new JSONObject(in);

            if(joydata.has("a")){
                //analog values
                JSONObject analog=joydata.getJSONObject("a");

                for(int i=0;i<Joystick.NUM_ANALOG;i++){
                    if(analog.has(i+"")){
                        joystick.analog[Joystick.ANALOG_AXIS_Z] = Math.max(Joystick.ANALOG_MIN,Math.min(Joystick.ANALOG_MAX,analog.getInt(i+"")));
                    }
                }
            }
            if(joydata.has("d")){
                //digital values
                JSONObject digital=joydata.getJSONObject("d");

                for(int i=0;i<Joystick.NUM_DIGITAL;i++){
                    if(digital.has(i+"")){
                        joystick.digital[i] = digital.getBoolean(i+"")?Joystick.DIGITAL_ON:Joystick.DIGITAL_OFF;
                    }
                }
            }
        }catch(JSONException e){
            //invalid data
            return;
        }
        */
    }

    public static void disable(){
        enabled=false;
        /*
        try {
            if(joystick!=null)joystick.close();
        } catch (JoystickException e) {
            e.printStackTrace();
        }
        joystick=null;
        */
    }
}