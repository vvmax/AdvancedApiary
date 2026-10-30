package ru.advancedapiary;
import java.util.Set;
import forestry.api.apiculture.*;
import forestry.api.genetics.*;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraftforge.oredict.OreDictionary;

/** Matches inventory specimens using registry data only. No World or growth/search callbacks. */
public final class InternalFlowers {
    private static Block block(ItemStack stack) {
        Item item=stack.getItem();
        if(item==Items.nether_wart)return Blocks.nether_wart;
        if(item==Items.wheat_seeds)return Blocks.wheat;
        if(item==Items.pumpkin_seeds)return Blocks.pumpkin_stem;
        if(item==Items.melon_seeds)return Blocks.melon_stem;
        if(item==Items.reeds)return Blocks.reeds;
        return Block.getBlockFromItem(item);
    }
    public static boolean matches(ItemStack stack,Set<IFlower> flowers) {
        if(stack==null||stack.stackSize<=0||flowers==null)return false;
        Block block=block(stack);if(block==null||block==Blocks.air)return false;
        int meta=stack.getItem() instanceof ItemBlock?((ItemBlock)stack.getItem()).getMetadata(stack.getItemDamage()):0;
        for(IFlower flower:flowers)if(flower.getBlock()==block&&(flower.getMeta()==OreDictionary.WILDCARD_VALUE||flower.getMeta()==meta))return true;
        return false;
    }
    public static boolean accepted(ItemStack stack) {
        if(stack==null||FlowerManager.flowerRegistry==null)return false;
        for(String type:FlowerManager.flowerRegistry.getFlowerTypes())if(matches(stack,FlowerManager.flowerRegistry.getAcceptableFlowers(type)))return true;
        return false;
    }
    public static boolean supports(IBee bee,ItemStack[] inventory) {
        Set<IFlower> flowers=bee.getGenome().getFlowerProvider().getFlowers();
        for(int i=6;i<10;i++)if(matches(inventory[i],flowers))return true;
        return false;
    }
    private InternalFlowers(){}
}
