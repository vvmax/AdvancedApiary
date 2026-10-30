package ru.advancedapiary;
import java.util.*;
import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.*;
import forestry.api.core.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/** Adapter for optional Forestry effects. Queen inventory is read-only to preserve immortality. */
public final class ApiaryBeeHousing implements IBeeHousing {
    private final TileApiary tile;private final int slot;
    private final IErrorLogic errors=new forestry.core.errors.ErrorLogic();
    private final IBeeHousingInventory inventory=new IBeeHousingInventory(){
        public ItemStack getQueen(){ItemStack q=tile.getStackInSlot(slot);return q==null?null:q.copy();}
        public ItemStack getDrone(){return null;}
        public void setQueen(ItemStack ignored){}
        public void setDrone(ItemStack ignored){}
        public boolean addProduct(ItemStack stack,boolean all){return tile.offerProduct(stack);}
    };
    private final IBeekeepingLogic logic=new IBeekeepingLogic(){
        public boolean canWork(){return tile.status[slot]==TileApiary.WORKING;}
        public void doWork(){}
        public void syncToClient(){}
        public void syncToClient(EntityPlayerMP player){}
        public int getBeeProgressPercent(){return Math.min(100,100*tile.progress[slot]/Math.max(1,tile.cycleLength()));}
        public boolean canDoBeeFX(){return false;}
        public void doBeeFX(){}
        public void readFromNBT(NBTTagCompound nbt){}
        public void writeToNBT(NBTTagCompound nbt){}
    };
    public ApiaryBeeHousing(TileApiary tile,int slot){this.tile=tile;this.slot=slot;}
    public Iterable<IBeeModifier> getBeeModifiers(){return Collections.<IBeeModifier>singletonList(new DefaultBeeModifier(){
        @Override public boolean isSealed(){return tile.modules(ItemModule.RAIN)>0;}
        @Override public boolean isSelfLighted(){return tile.modules(ItemModule.LIGHT)>0;}
        @Override public boolean isSunlightSimulated(){return tile.modules(ItemModule.SKY)>0;}
    });}
    public Iterable<IBeeListener> getBeeListeners(){return Collections.<IBeeListener>emptyList();}
    public IBeeHousingInventory getBeeInventory(){return inventory;}
    public IBeekeepingLogic getBeekeepingLogic(){return logic;}
    public IErrorLogic getErrorLogic(){return errors;}
    public EnumTemperature getTemperature(){
        IBee bee=BeeProduction.inspect(tile.getStackInSlot(slot));
        if(tile.modules(ItemModule.CLIMATE)>0&&bee!=null)return bee.getGenome().getPrimary().getTemperature();
        int n=tile.readEnvironment().temperature.ordinal()+tile.modules(ItemModule.HEATER)-tile.modules(ItemModule.COOLER);
        return EnumTemperature.values()[Math.max(1,Math.min(EnumTemperature.values().length-1,n))];
    }
    public EnumHumidity getHumidity(){
        IBee bee=BeeProduction.inspect(tile.getStackInSlot(slot));
        if(tile.modules(ItemModule.CLIMATE)>0&&bee!=null)return bee.getGenome().getPrimary().getHumidity();
        int n=tile.readEnvironment().humidity.ordinal()+tile.modules(ItemModule.HUMIDIFIER)-tile.modules(ItemModule.DRYER);
        return EnumHumidity.values()[Math.max(0,Math.min(EnumHumidity.values().length-1,n))];
    }
    public int getBlockLightValue(){
        IBee bee=BeeProduction.inspect(tile.getStackInSlot(slot));
        boolean night=bee!=null&&bee.getGenome().getPrimary().isNocturnal()&&!bee.getGenome().getNocturnal();
        if(night&&tile.modules(ItemModule.DARK)>0)return 0;
        if(!night&&tile.modules(ItemModule.LIGHT)>0)return 15;
        return tile.readEnvironment().light;
    }
    public boolean canBlockSeeTheSky(){return tile.modules(ItemModule.SKY)>0||tile.readEnvironment().sky;}
    public World getWorld(){return tile.getWorldObj();}
    public BiomeGenBase getBiome(){return getWorld().getBiomeGenForCoords(tile.xCoord,tile.zCoord);}
    public GameProfile getOwner(){return tile.owner();}
    public ChunkCoordinates getCoordinates(){return new ChunkCoordinates(tile.xCoord,tile.yCoord,tile.zCoord);}
    public Vec3 getBeeFXCoordinates(){return Vec3.createVectorHelper(tile.xCoord+.5,tile.yCoord+.5,tile.zCoord+.5);}
}
