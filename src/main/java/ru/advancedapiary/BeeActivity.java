package ru.advancedapiary;
import forestry.api.apiculture.*;
import forestry.api.genetics.*;
/** Optional bee behaviour, separated from production and immortal queen storage. */
public final class BeeActivity {
    private final IBeeHousing housing;
    private IEffectData[] effects=new IEffectData[2];
    private IIndividual pollen;
    private int attempts;
    private long nextFlowerCheck=Long.MIN_VALUE;
    private boolean externalFlowers;
    public BeeActivity(IBeeHousing housing){this.housing=housing;}
    public void clear(){effects=new IEffectData[2];pollen=null;attempts=0;nextFlowerCheck=Long.MIN_VALUE;externalFlowers=false;}
    public boolean hasExternalFlowers(IBee bee,long tick){
        if(Settings.disableBeeAI)return false;
        if(tick>=nextFlowerCheck){
            externalFlowers=FlowerManager.flowerRegistry.getAcceptedFlowerCoordinates(housing,bee,bee.getGenome().getFlowerProvider().getFlowerType())!=null;
            nextFlowerCheck=tick+200;
        }
        return externalFlowers;
    }
    public void tick(IBee bee,boolean completedCycle){
        if(Settings.disableBeeAI){clear();return;}
        IEffectData[] result=bee.doEffect(effects,housing);
        effects=result==null?new IEffectData[2]:result;
        if(!completedCycle)return;
        bee.plantFlowerRandom(housing);
        if(pollen==null){pollen=bee.retrievePollen(housing);attempts=0;}
        if(pollen!=null&&(bee.pollinateRandom(housing,pollen)||++attempts>=20)){pollen=null;attempts=0;}
        // Never age, spawn offspring, execute standard beekeeping logic, or write bee NBT.
    }
}
