package ru.advancedapiary;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
/** Machine output only; never invokes any bee effect or flower callback. */
public final class OutputExport {
    public static boolean moveOne(TileApiary source,int from,IInventory target,int slot,int face){
        ItemStack stack=source.getStackInSlot(from);
        if(target==source||from<18||from>=33||stack==null||slot<0||slot>=target.getSizeInventory()||!target.isItemValidForSlot(slot,stack))return false;
        if(target instanceof ISidedInventory&&!((ISidedInventory)target).canInsertItem(slot,stack,face))return false;
        ItemStack present=target.getStackInSlot(slot);
        if(present!=null&&!InventoryOps.same(present,stack))return false;
        int limit=Math.min(stack.getMaxStackSize(),target.getInventoryStackLimit());
        if(limit<1||present!=null&&present.stackSize>=limit)return false;
        ItemStack insert=present==null?stack.copy():present.copy();insert.stackSize=present==null?1:present.stackSize+1;
        int expected=insert.stackSize;ItemStack previous=present==null?null:present.copy();
        target.setInventorySlotContents(slot,insert);ItemStack actual=target.getStackInSlot(slot);
        if(!InventoryOps.same(actual,stack)||actual.stackSize!=expected){target.setInventorySlotContents(slot,previous);return false;}
        source.decrStackSize(from,1);target.markDirty();return true;
    }
    public static void run(TileApiary source,int budget){
        for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
            if(budget<=0)return;
            TileEntity te=source.getWorldObj().getTileEntity(source.xCoord+dir.offsetX,source.yCoord+dir.offsetY,source.zCoord+dir.offsetZ);
            if(!(te instanceof IInventory)||te==source)continue;
            IInventory target=(IInventory)te;int face=dir.getOpposite().ordinal();int[] slots;
            if(target instanceof ISidedInventory)slots=((ISidedInventory)target).getAccessibleSlotsFromSide(face);
            else{slots=new int[target.getSizeInventory()];for(int i=0;i<slots.length;i++)slots[i]=i;}
            for(int from=18;from<33&&budget>0;from++)while(budget>0&&source.getStackInSlot(from)!=null){
                boolean moved=false;
                for(int slot:slots)if(moveOne(source,from,target,slot,face)){budget--;moved=true;break;}
                if(!moved)break;
            }
        }
    }
    private OutputExport(){}
}
