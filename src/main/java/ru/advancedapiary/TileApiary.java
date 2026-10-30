package ru.advancedapiary;

import java.util.*;
import forestry.api.apiculture.IBee;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;

public class TileApiary extends TileEntity implements ISidedInventory {
    public static final int EMPTY=0, WORKING=1, NO_FLOWERS=2, OUTPUT_FULL=3, INVALID=4;
    private ItemStack[] items=new ItemStack[ApiaryLayout.SIZE];
    public final int[] progress=new int[6], status=new int[6];
    private final List<List<ItemStack>> pending=new ArrayList<List<ItemStack>>();
    private int clock, nextQueen;
    private final BeeActivity[] activities=new BeeActivity[6];
    private com.mojang.authlib.GameProfile owner=new com.mojang.authlib.GameProfile(java.util.UUID.fromString("25c828e7-b1f7-47e6-9089-c51dd34ad911"),"AdvancedApiary");
    public com.mojang.authlib.GameProfile owner(){return owner;}
    public void setOwner(com.mojang.authlib.GameProfile profile){if(profile!=null){owner=profile;markDirty();}}
    protected forestry.api.apiculture.IBeeHousing createBeeHousing(int slot){return new ApiaryBeeHousing(this,slot);}
    private BeeActivity activity(int slot){if(activities[slot]==null)activities[slot]=new BeeActivity(createBeeHousing(slot));return activities[slot];}
    private void tickAI(int slot,IBee bee,boolean completed){if(!Settings.disableBeeAI)activity(slot).tick(bee,completed);}
    public boolean offerProduct(ItemStack stack){if(stack==null)return false;ItemStack[] planned=InventoryOps.plan(items,18,33,Collections.singletonList(stack));if(planned==null)return false;for(int i=18;i<33;i++)items[i]=planned[i];markDirty();return true;}

    public TileApiary() { for(int i=0;i<6;i++)pending.add(null); }
    public int modules(int type) {
        int count=0;for(int i=10;i<18;i++) if(items[i]!=null&&items[i].getItem()==AdvancedApiary.module&&items[i].getItemDamage()==type) count++;
        return count;
    }
    public boolean hasInternalGarden(){return modules(ItemModule.GARDEN)>0;}
    public int cycleLength() { return Math.max(20,Settings.cycleTicks/(1+modules(ItemModule.SPEED))); }
    private ApiaryEnvironment.Snapshot environmentCache;
    private long environmentTick=Long.MIN_VALUE;
    protected ApiaryEnvironment.Snapshot readEnvironment(){
        if(worldObj==null)return null;
        if(environmentCache==null||environmentTick!=worldObj.getTotalWorldTime()/20){environmentTick=worldObj.getTotalWorldTime()/20;environmentCache=ApiaryEnvironment.read(worldObj,xCoord,yCoord,zCoord);}
        return environmentCache;
    }
    private int[] moduleCounts(){int[] result=new int[ItemModule.TYPES];for(int i=10;i<18;i++)if(items[i]!=null&&items[i].getItem()==AdvancedApiary.module){int type=items[i].getItemDamage();if(type>=0&&type<result.length)result[type]++;}return result;}
    @Override public void updateEntity() {
        if(worldObj==null||worldObj.isRemote) return;
        // Every tick advances all six independently; ordering rotates so no chamber permanently has priority over shared outputs.
        for(int j=0;j<6;j++) advance((nextQueen+j)%6,worldObj.rand);
        nextQueen=(nextQueen+1)%6;
        if(++clock>=20) {clock=0;if(modules(ItemModule.SORTER)>0)OutputSort.run(this);if(modules(ItemModule.EXPORT)>0)OutputExport.run(this,modules(ItemModule.EXPORT));markDirty();}
    }
    /** Public for a deterministic headless test; only updateEntity calls this in normal gameplay. */
    public void advance(int i,Random rng) {
        if(items[i]==null) {status[i]=EMPTY;progress[i]=0;pending.set(i,null);return;}
        IBee bee=BeeProduction.inspect(items[i]);
        if(bee==null) {status[i]=INVALID;return;}
        int environmentStatus=ApiaryEnvironment.check(bee.getGenome(),readEnvironment(),moduleCounts());
        if(environmentStatus!=WORKING){status[i]=environmentStatus;return;}
        if((!hasInternalGarden()||!InternalFlowers.supports(bee,items))&&(Settings.disableBeeAI||!activity(i).hasExternalFlowers(bee,worldObj==null?0:worldObj.getTotalWorldTime()))) {status[i]=NO_FLOWERS;return;}
        status[i]=WORKING;
        if(pending.get(i)==null) {
            progress[i]=Math.min(cycleLength(),progress[i]+1);
            if(progress[i]<cycleLength()){tickAI(i,bee,false);return;}
            List<ItemStack> batch=new ArrayList<ItemStack>();
            int focus=1+modules(ItemModule.FOCUS);
            for(int pass=0;pass<=modules(ItemModule.PARALLEL);pass++)batch.addAll(BeeProduction.roll(bee,rng,
                1+0.25*modules(ItemModule.PRODUCTIVITY),(1+0.5*modules(ItemModule.SPECIALTY))*focus,
                1.0/focus));
            pending.set(i,batch);
            markDirty();
        }
        ItemStack[] planned=InventoryOps.plan(items,18,33,pending.get(i));
        if(planned==null) {status[i]=OUTPUT_FULL;return;}
        // Output batch commits atomically. Internal plants are permanent specimens.
        for(int k=18;k<33;k++) items[k]=planned[k];
        progress[i]=0;pending.set(i,null);markDirty();tickAI(i,bee,true);
    }
    private void resetQueen(int i) {progress[i]=0;status[i]=EMPTY;pending.set(i,null);activities[i]=null;}
    @Override public int getSizeInventory() {return items.length;}
    @Override public ItemStack getStackInSlot(int i) {return i>=0&&i<items.length?items[i]:null;}
    @Override public ItemStack decrStackSize(int i,int n) {
        if(n<=0||getStackInSlot(i)==null)return null;
        ItemStack out;
        if(n>=items[i].stackSize) {out=items[i];items[i]=null;} else out=items[i].splitStack(n);
        if(i<6)resetQueen(i);markDirty();return out;
    }
    @Override public ItemStack getStackInSlotOnClosing(int i) {ItemStack out=getStackInSlot(i);if(out!=null){items[i]=null;if(i<6)resetQueen(i);markDirty();}return out;}
    @Override public void setInventorySlotContents(int i,ItemStack stack) {
        if(i<0||i>=items.length)return;
        if(stack!=null) stack.stackSize=Math.min(stack.stackSize,i<6||i>=10&&i<18?1:64);
        items[i]=stack;if(i<6)resetQueen(i);markDirty();
    }
    @Override public String getInventoryName() {return "container.advancedapiary";}
    @Override public boolean hasCustomInventoryName() {return false;}
    @Override public int getInventoryStackLimit() {return 64;}
    @Override public boolean isUseableByPlayer(EntityPlayer p) {return worldObj!=null&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&p.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64;}
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int i,ItemStack stack) {
        if(stack==null)return false;
        if(i>=0&&i<6) return BeeProduction.inspect(stack)!=null;
        if(i>=6&&i<10) return hasInternalGarden()&&InternalFlowers.accepted(stack);
        return i>=10&&i<18&&stack.getItem()==AdvancedApiary.module&&stack.getItemDamage()>=0&&stack.getItemDamage()<ItemModule.TYPES;
    }
    @Override public int[] getAccessibleSlotsFromSide(int side) {
        if(side<0||side>5)return new int[0];
        int inputs=side==1?6:side>1&&hasInternalGarden()?4:0;
        int[] slots=new int[15+inputs];
        for(int i=0;i<15;i++)slots[i]=18+i;
        for(int i=0;i<inputs;i++)slots[15+i]=side==1?i:6+i;
        return slots;
    }
    @Override public boolean canInsertItem(int i,ItemStack stack,int side) {return (side==1&&i>=0&&i<6||side>=2&&side<=5&&i>=6&&i<10)&&isItemValidForSlot(i,stack);}
    @Override public boolean canExtractItem(int i,ItemStack stack,int side) {return side>=0&&side<=5&&i>=18&&i<33;}
    @Override public void writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound ownerTag=new NBTTagCompound();NBTUtil.func_152460_a(ownerTag,owner);nbt.setTag("Owner",ownerTag);
        super.writeToNBT(nbt);NBTTagList list=new NBTTagList();
        for(int i=0;i<items.length;i++)if(items[i]!=null){NBTTagCompound tag=new NBTTagCompound();tag.setInteger("Slot",i);items[i].writeToNBT(tag);list.appendTag(tag);}nbt.setTag("Items",list);
        nbt.setIntArray("Progress",progress);nbt.setInteger("NextQueen",nextQueen);
        NBTTagList batches=new NBTTagList();
        for(int i=0;i<6;i++)if(pending.get(i)!=null){NBTTagCompound b=new NBTTagCompound();b.setInteger("Queen",i);NBTTagList products=new NBTTagList();for(ItemStack product:pending.get(i)){NBTTagCompound t=new NBTTagCompound();product.writeToNBT(t);products.appendTag(t);}b.setTag("Products",products);batches.appendTag(b);}nbt.setTag("Pending",batches);
    }
    @Override public void readFromNBT(NBTTagCompound nbt) {
        if(nbt.hasKey("Owner",10)){com.mojang.authlib.GameProfile savedOwner=NBTUtil.func_152459_a(nbt.getCompoundTag("Owner"));if(savedOwner!=null)owner=savedOwner;}
        Arrays.fill(activities,null);
        super.readFromNBT(nbt);items=new ItemStack[ApiaryLayout.SIZE];Arrays.fill(progress,0);Arrays.fill(status,EMPTY);
        for(int i=0;i<6;i++)pending.set(i,null);
        NBTTagList list=nbt.getTagList("Items",10);for(int j=0;j<list.tagCount();j++){NBTTagCompound tag=list.getCompoundTagAt(j);int i=tag.getInteger("Slot");if(i>=0&&i<items.length) items[i]=ItemStack.loadItemStackFromNBT(tag);}
        int[] saved=nbt.getIntArray("Progress");for(int i=0;i<Math.min(6,saved.length);i++)progress[i]=Math.max(0,Math.min(72000,saved[i]));
        nextQueen=Math.max(0,nbt.getInteger("NextQueen"))%6;
        NBTTagList batches=nbt.getTagList("Pending",10);for(int j=0;j<batches.tagCount();j++){NBTTagCompound b=batches.getCompoundTagAt(j);int i=b.getInteger("Queen");if(i<0||i>=6||items[i]==null)continue;List<ItemStack> products=new ArrayList<ItemStack>();NBTTagList ps=b.getTagList("Products",10);for(int k=0;k<ps.tagCount();k++){ItemStack stack=ItemStack.loadItemStackFromNBT(ps.getCompoundTagAt(k));if(stack!=null)products.add(stack);}pending.set(i,products);}
    }
}
