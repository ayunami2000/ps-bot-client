package me.ayunami2000.psbot.mixin;

import me.ayunami2000.psbot.Commands;
import me.ayunami2000.psbot.PsBot;
import me.ayunami2000.psbot.SteganographyUtil;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayerEntity;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "sendChatMessage(Ljava/lang/String;Lnet/minecraft/text/Text;)V", cancellable=true)
    private void onSendChatMessage(String message, Text preview, CallbackInfo ci) {
        boolean isCommand = Commands.processChatMessage(message);
        if (isCommand) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "sendChatMessage(Ljava/lang/String;Lnet/minecraft/text/Text;)V", at = @At("HEAD"), ordinal = 0)
    private String injected(String message) {
        if(PsBot.encChat&&!message.startsWith("/")){
            return SteganographyUtil.encrypt(message);
        }else{
            return message;
        }
    }
}