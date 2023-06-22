package me.ayunami2000.psbot.mixin;

import me.ayunami2000.psbot.PsBot;
import me.ayunami2000.psbot.SteganographyUtil;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(value = ChatHud.class, priority = 0)
public class ChatHudMixin extends DrawableHelper {
    @Inject(at = @At("HEAD"),
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            cancellable = true)
    private void addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        if(message.getString().startsWith("Command set: ") && (PsBot.isPlaying||PsBot.subtitleThread.alreadySubbing)) {
            ci.cancel();
        }else{
            PsBot.onChatMessage(message);
        }
    }
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), ordinal = 0)
    private Text injected(Text message) {
        if(PsBot.encChat){
            /*
            StringBuilder dec = new StringBuilder();
            message.visit((style, asString) -> {
                if(asString.startsWith("⎮")) {
                    TextColor tc = style.getColor();
                    if (tc != null) dec.append(Integer.toHexString(tc.getRgb()).substring(2));
                }
                return Optional.empty();
            },Style.EMPTY);
            if(dec.isEmpty()){
                return message;
            }else{
                try {
                    return Text.of(message.getString() + "\nps-bot dec: " + new String(Hex.decodeHex(dec.toString().toCharArray()), "UTF-8"));
                } catch (UnsupportedEncodingException | DecoderException e) {
                    return message;
                }
            }
            */
            String dec=SteganographyUtil.decryptMessage(message);
            if(dec.equals("")){
                return message;
            }else{
                return Text.of(message.getString()+"\n§rps-bot dec: "+SteganographyUtil.decryptMessage(message));
            }
        }else{
            return message;
        }
    }
}