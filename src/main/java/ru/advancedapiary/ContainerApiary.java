package ru.advancedapiary;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.*;
public final class ContainerApiary extends Container {
    public final TileApiary tile;
    public int clientCycle;
    public boolean clientDisableBeeAI=false;
    private final int[] last=new int[21];
    public ContainerApiary(InventoryPlayer player,TileApiary tile) {
        this.tile=tile;java.util.Arrays.fill(last,Integer.MIN_VALUE);
        for(int i=0;i<6;i++) addSlotToContainer(new ApiarySlot(tile,i,ApiaryLayout.queenX(i),ApiaryLayout.queenY(i)));
        for(int i=0;i<4;i++) addSlotToContainer(new ApiarySlot(tile,6+i,11+i*18,135));
        for(int i=0;i<8;i++) addSlotToContainer(new ApiarySlot(tile,10+i,89+i*18,135));
        for(int r=0;r<5;r++)for(int c=0;c<3;c++)addSlotToContainer(new ApiarySlot(tile,18+r*3+c,183+c*18,33+r*18));
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlotToContainer(new Slot(player,c+r*9+9,43+c*18,160+r*18));
        for(int c=0;c<9;c++)addSlotToContainer(new Slot(player,c,43+c*18,218));
    }
    private static class ApiarySlot extends Slot {
        private final TileApiary tile;
        ApiarySlot(TileApiary tile,int index,int x,int y){super(tile,index,x,y);this.tile=tile;}
        @Override public boolean isItemValid(ItemStack stack){return tile.isItemValidForSlot(getSlotIndex(),stack);}
        @Override public int getSlotStackLimit(){int i=getSlotIndex();return i<6||i>=10&&i<18?1:64;}
    }
    @Override public boolean canInteractWith(EntityPlayer p){return tile.isUseableByPlayer(p);}
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int index) {
        if(index<0||index>=inventorySlots.size())return null;
        Slot slot=(Slot)inventorySlots.get(index);if(!slot.getHasStack())return null;
        ItemStack stack=slot.getStack(),copy=stack.copy();
        boolean moved;
        if(index<33)moved=mergeItemStack(stack,33,69,true);
        else if(BeeProduction.inspect(stack)!=null)moved=mergeItemStack(stack,0,6,false);
        else if(InternalFlowers.accepted(stack))moved=mergeItemStack(stack,6,10,false);
        else if(stack.getItem()==AdvancedApiary.module)moved=mergeItemStack(stack,10,18,false);
        else moved=index<60?mergeItemStack(stack,60,69,false):mergeItemStack(stack,33,60,false);
        if(!moved)return null;
        if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();
        slot.onPickupFromSlot(p,stack);return copy;
    }
    // Vanilla 1.7.10 mergeItemStack does not respect slot-specific limits for every path.
    @Override protected boolean mergeItemStack(ItemStack source,int start,int end,boolean reverse) {
        boolean changed=false;
        for(int pass=0;pass<2;pass++)for(int step=0;step<end-start&&source.stackSize>0;step++) {
            Slot slot=(Slot)inventorySlots.get(reverse?end-1-step:start+step);
            if(!slot.isItemValid(source))continue;
            ItemStack target=slot.getStack();
            int max=Math.min(slot.getSlotStackLimit(),source.getMaxStackSize());
            if(pass==0&&target!=null&&InventoryOps.same(source,target)) {
                int n=Math.min(source.stackSize,Math.max(0,max-target.stackSize));if(n>0){target.stackSize+=n;source.stackSize-=n;slot.onSlotChanged();changed=true;}
            } else if(pass==1&&target==null) {
                int n=Math.min(source.stackSize,max);ItemStack insert=source.copy();insert.stackSize=n;slot.putStack(insert);source.stackSize-=n;changed=true;
            }
        }
        return changed;
    }
    private int[] snapshot() {
        int[] values=new int[21];
        for(int i=0;i<6;i++){values[i]=tile.progress[i]&65535;values[6+i]=tile.progress[i]>>>16;values[12+i]=tile.status[i];}
        values[18]=tile.cycleLength()&65535;values[19]=tile.cycleLength()>>>16;values[20]=Settings.disableBeeAI?1:0;
        return values;
    }
    @Override public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        int[] values=snapshot();for(int i=0;i<values.length;i++)listener.sendProgressBarUpdate(this,i,values[i]);
    }
    @Override public void detectAndSendChanges() {
        super.detectAndSendChanges();int[] values=snapshot();
        for(int i=0;i<values.length;i++)if(last[i]!=values[i]) {
            for(Object o:crafters)((ICrafting)o).sendProgressBarUpdate(this,i,values[i]);
            last[i]=values[i];
        }
    }
    @Override @SideOnly(Side.CLIENT) public void updateProgressBar(int id,int value) {
        int v=value&65535;
        if(id<6)tile.progress[id]=(tile.progress[id]&0xffff0000)|v;
        else if(id<12)tile.progress[id-6]=(tile.progress[id-6]&65535)|(v<<16);
        else if(id<18)tile.status[id-12]=v;
        else if(id==18)clientCycle=(clientCycle&0xffff0000)|v;
        else if(id==19)clientCycle=(clientCycle&65535)|(v<<16);
        else if(id==20)clientDisableBeeAI=v!=0;

    }
}
