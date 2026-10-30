package ru.advancedapiary;
import java.util.List;
import net.minecraft.item.ItemStack;
public final class InventoryOps {
    public static boolean same(ItemStack a,ItemStack b) { return a!=null && b!=null && a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a,b); }
    /** Transactional insertion: never inserts a partial batch when output space is exhausted. */
    public static ItemStack[] plan(ItemStack[] inventory,int start,int end,List<ItemStack> batch) {
        ItemStack[] copy=new ItemStack[inventory.length];
        for(int i=0;i<copy.length;i++) if(inventory[i]!=null) copy[i]=inventory[i].copy();
        for(ItemStack product:batch) {
            if(product==null||product.stackSize<=0) continue;
            int remaining=product.stackSize;
            for(int i=start;i<end && remaining>0;i++) if(same(copy[i],product)) {
                int take=Math.min(remaining,Math.max(0,Math.min(64,copy[i].getMaxStackSize())-copy[i].stackSize));
                copy[i].stackSize+=take; remaining-=take;
            }
            for(int i=start;i<end && remaining>0;i++) if(copy[i]==null) {
                int take=Math.min(remaining,Math.min(64,product.getMaxStackSize()));
                copy[i]=product.copy();copy[i].stackSize=take;remaining-=take;
            }
            if(remaining>0) return null;
        }
        return copy;
    }
    private InventoryOps() {}
}
