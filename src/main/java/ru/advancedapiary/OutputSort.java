package ru.advancedapiary;
import java.util.*;
import net.minecraft.item.ItemStack;
/** Compacts identical stacks and orders output slots without deleting or converting products. */
public final class OutputSort {
    public static void run(TileApiary tile){
        ItemStack[] inventory=new ItemStack[33];List<ItemStack> products=new ArrayList<ItemStack>();
        for(int i=18;i<33;i++)if(tile.getStackInSlot(i)!=null)products.add(tile.getStackInSlot(i).copy());
        Collections.sort(products,new Comparator<ItemStack>(){public int compare(ItemStack a,ItemStack b){return ItemStackKey.key(a).compareTo(ItemStackKey.key(b));}});
        ItemStack[] planned=InventoryOps.plan(inventory,18,33,products);
        if(planned!=null)for(int i=18;i<33;i++)tile.setInventorySlotContents(i,planned[i]);
    }
    private OutputSort(){}
}
