package me.ayunami2000.psbot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class MassItemFrameThread implements Runnable{
    @Override
    public void run() {
        try {
            BlockPos originPos = PsBot.mc.player.getBlockPos().mutableCopy().up();
            for (Item item : Registry.ITEM) {
                if (!PsBot.isPlaying) return;

                //FAILED EXPERIMENT LIES HERE...

                Identifier fard = Registry.ITEM.getId(item);

                ItemStack itemFrame = new ItemStack(Items.ITEM_FRAME);
                NbtCompound nbt = new NbtCompound();
                NbtCompound itemnbt = new NbtCompound();
                NbtCompound itemitemnbt = new NbtCompound();
                itemitemnbt.putString("id", fard.toString());
                itemitemnbt.putInt("Count", 1);
                itemnbt.put("Item", itemitemnbt);
                itemnbt.putBoolean("Fixed", true);
                itemnbt.putBoolean("Invisible", true);
                nbt.put("EntityTag", itemnbt);
                itemFrame.setNbt(nbt);
                PsBot.loadItem(itemFrame);
                PsBot.rightClickBlock(originPos, Direction.UP);
                Thread.sleep(1000L / PsBot.fps);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}