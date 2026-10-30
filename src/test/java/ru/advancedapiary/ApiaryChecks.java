package ru.advancedapiary;

import java.lang.reflect.*;
import java.util.*;
import forestry.api.apiculture.*;
import forestry.api.genetics.*;
import forestry.api.core.*;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import cpw.mods.fml.common.registry.GameRegistry;

/** Real ItemStack/NBT/inventory, strict Forestry API doubles reject every lifecycle/world hook. */
public final class ApiaryChecks {
    public static class TestTile extends TileApiary {
        public TestTile(){this(true);}
        public TestTile(boolean garden){if(garden)setInventorySlotContents(17,new ItemStack(AdvancedApiary.module,1,ItemModule.GARDEN));}
        public ApiaryEnvironment.Snapshot environment=new ApiaryEnvironment.Snapshot(EnumTemperature.NORMAL,EnumHumidity.NORMAL,false,true,true,false,15);
        @Override protected ApiaryEnvironment.Snapshot readEnvironment(){return environment;}
    }
    private static int checks,rolls;
    private static float specialtyChance=1F;
    private static Item queen,food;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private interface Call {Object run(Method method,Object[] args);}
    @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> type,final Call call){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},new InvocationHandler(){public Object invoke(Object p,Method m,Object[] a){if(m.getName().equals("toString"))return "strict-test-"+m.getDeclaringClass().getSimpleName();if(m.getName().equals("hashCode"))return System.identityHashCode(p);if(m.getName().equals("equals"))return p==a[0];return call.run(m,a);}});}
    private static IBee bee(final boolean speciesPure,final boolean allPure){return bee(speciesPure,allPure,"flowers");}
    private static IBee bee(final boolean speciesPure,final boolean allPure,final String flowerType){
        final Map<ItemStack,Float> products=new LinkedHashMap<ItemStack,Float>();products.put(new ItemStack(Items.gold_nugget),1F);
        final Map<ItemStack,Float> special=new LinkedHashMap<ItemStack,Float>();special.put(new ItemStack(Items.redstone),specialtyChance);
        final IAlleleBeeSpecies species=proxy(IAlleleBeeSpecies.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getTemperature"))return EnumTemperature.NORMAL;if(m.getName().equals("getHumidity"))return EnumHumidity.NORMAL;if(m.getName().equals("isNocturnal"))return false;if(m.getName().equals("getProductChances")){rolls++;return products;}if(m.getName().equals("getSpecialtyChances"))return special;throw new AssertionError("Forbidden species callback: "+m);}});
        final IBeeGenome genome=proxy(IBeeGenome.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getToleranceTemp")||m.getName().equals("getToleranceHumid"))return EnumTolerance.NONE;if(m.getName().equals("getNocturnal")||m.getName().equals("getTolerantFlyer")||m.getName().equals("getCaveDwelling"))return false;if(m.getName().equals("getPrimary"))return species;if(m.getName().equals("getSpeed"))return 1F;if(m.getName().equals("getFlowerProvider"))return provider(flowerType);throw new AssertionError("Forbidden genome callback: "+m);}});
        return proxy(IBee.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("isPureBred"))return a[0]==EnumBeeChromosome.SPECIES?speciesPure:allPure;if(m.getName().equals("getGenome"))return genome;throw new AssertionError("Forbidden bee callback (lifespan/effect/flower/breeding): "+m);}});
    }
    private static Set<IFlower> flowers(final String type){
        final Block block=type.equals("cacti")?Blocks.cactus:Blocks.yellow_flower;
        IFlower f=proxy(IFlower.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getBlock"))return block;if(m.getName().equals("getMeta"))return 0;throw new AssertionError("Forbidden flower method "+m);}});
        return Collections.singleton(f);
    }
    private static IFlowerProvider provider(final String type){return proxy(IFlowerProvider.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getFlowers"))return flowers(type);if(m.getName().equals("getFlowerType")||m.getName().equals("getDescription"))return type;throw new AssertionError("Forbidden flower/world callback "+m);}});}
    private static int plantCount(TileApiary tile){int n=0;for(int i=6;i<10;i++)if(tile.getStackInSlot(i)!=null)n+=tile.getStackInSlot(i).stackSize;return n;}
    private static <T>T override(Class<T> type,final T original,final Map<String,Object> values){return proxy(type,new Call(){public Object run(Method m,Object[] a){if(values.containsKey(m.getName()))return values.get(m.getName());try{return m.invoke(original,a);}catch(Exception e){throw new AssertionError(e);}}});}
    private static ApiaryEnvironment.Snapshot env(EnumTemperature temp,EnumHumidity humidity,boolean rain,boolean day,boolean sky,int light){return new ApiaryEnvironment.Snapshot(temp,humidity,rain,day,sky,false,light);}
    private static ItemStack queen(int kind){ItemStack q=new ItemStack(queen);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("TestKind",kind);tag.setInteger("Health",17);q.setTagCompound(tag);return q;}
    private static int output(TileApiary t,Item item){int n=0;for(int i=18;i<33;i++){ItemStack s=t.getStackInSlot(i);if(s!=null&&s.getItem()==item)n+=s.stackSize;}return n;}
    public static void main(String[] args)throws Exception {
        Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
        Bootstrap.func_151354_b();
        check(!Settings.disableBeeAI,"AI must be enabled by default");Settings.disableBeeAI=true;
        queen=new Item().setMaxStackSize(1);food=Item.getItemFromBlock(Blocks.yellow_flower);
        FlowerManager.flowerRegistry=proxy(IFlowerRegistry.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getFlowerTypes"))return Arrays.asList("flowers","cacti");if(m.getName().equals("getAcceptableFlowers"))return flowers((String)a[0]);throw new AssertionError("Forbidden flower registry/world callback "+m);}});
        Item.itemRegistry.addObject(31001,"advancedapiary:test_queen",queen);
        AdvancedApiary.module=new ItemModule();Item.itemRegistry.addObject(31003,"advancedapiary:module",AdvancedApiary.module);
        GameRegistry.registerTileEntity(TestTile.class,"advancedapiary:test_apiary");
        final IBee pure=bee(true,true),hybrid=bee(false,false),mixed=bee(true,false),cactusBee=bee(true,true,"cacti");
        specialtyChance=.25F;final IBee fractional=bee(true,true);specialtyChance=1F;
        BeeManager.beeRoot=proxy(IBeeRoot.class,new Call(){public Object run(Method m,Object[] a){
            ItemStack q=(ItemStack)a[0];
            if(m.getName().equals("getType"))return q.getItem()==queen&&q.getTagCompound().getInteger("TestKind")!=3?EnumBeeType.QUEEN:EnumBeeType.DRONE;
            if(m.getName().equals("getMember")){int kind=q.getTagCompound().getInteger("TestKind");q.getTagCompound().setInteger("Health",0);return kind==1?hybrid:kind==2?mixed:kind==4?cactusBee:kind==5?fractional:pure;}
            throw new AssertionError("Forbidden root callback: "+m);
        }});
        Settings.cycleTicks=20;Settings.production=1;Settings.specialties=true;Settings.allGenesPure=false;
        check(BeeProduction.inspect(queen(0))!=null,"pure queen rejected");
        check(BeeProduction.inspect(queen(1))==null,"hybrid accepted");
        check(BeeProduction.inspect(queen(3))==null,"drone accepted");
        check(BeeProduction.inspect(new ItemStack(Items.apple))==null,"nonbee accepted");
        check(BeeProduction.inspect(queen(2))!=null,"pure species with mixed other genes rejected in default mode");
        Settings.allGenesPure=true;check(BeeProduction.inspect(queen(2))==null,"strict genotype mode failed");Settings.allGenesPure=false;
        TileApiary t=new TestTile();
        check(t.isItemValidForSlot(6,new ItemStack(food)),"food rejected");
        check(!t.isItemValidForSlot(6,new ItemStack(Items.apple)),"wrong food accepted");
        check(!t.isItemValidForSlot(18,new ItemStack(food)),"manual insertion to outputs allowed");
        for(int i=0;i<6;i++)t.setInventorySlotContents(i,queen(0));
        NBTTagCompound[] before=new NBTTagCompound[6];for(int i=0;i<6;i++)before[i]=(NBTTagCompound)t.getStackInSlot(i).getTagCompound().copy();
        for(int i=0;i<6;i++)t.advance(i,new Random(1));
        for(int i=0;i<6;i++)check(t.status[i]==TileApiary.NO_FLOWERS&&t.progress[i]==0,"no-food cycle advanced");
        t.setInventorySlotContents(6,new ItemStack(food,64));
        for(int tick=0;tick<200;tick++)for(int i=0;i<6;i++)t.advance(i,new Random(tick));
        check(output(t,Items.gold_nugget)==60,"six queens did not produce concurrently");
        check(output(t,Items.redstone)==60,"specialties missing");
        check(plantCount(t)==64,"shared plants were consumed");
        for(int i=0;i<6;i++)check(before[i].equals(t.getStackInSlot(i).getTagCompound()),"queen health or genes changed");
        check(t.canInsertItem(0,queen(0),1)&&!t.canInsertItem(0,queen(1),1),"hopper purity validation");
        check(!t.canExtractItem(0,queen(0),0)&&t.canExtractItem(18,new ItemStack(Items.gold_nugget),0),"automation extraction isolation");
        TileApiary blocked=new TestTile();blocked.setInventorySlotContents(0,queen(0));blocked.setInventorySlotContents(6,new ItemStack(food,5));
        for(int i=18;i<33;i++)blocked.setInventorySlotContents(i,new ItemStack(Blocks.dirt,64));
        for(int k=0;k<20;k++)blocked.advance(0,new Random(1));int frozenRolls=rolls;
        check(blocked.status[0]==TileApiary.OUTPUT_FULL&&plantCount(blocked)==5,"full output changed plant specimen");
        for(int k=0;k<50;k++)blocked.advance(0,new Random(2));check(rolls==frozenRolls,"blocked batch rerolled");
        NBTTagCompound saved=new NBTTagCompound();blocked.writeToNBT(saved);TileApiary restored=new TestTile();restored.readFromNBT(saved);
        restored.setInventorySlotContents(18,null);restored.setInventorySlotContents(19,null);restored.advance(0,new Random(9));
        check(rolls==frozenRolls,"save/load rerolled pending batch");
        check(plantCount(restored)==5&&output(restored,Items.gold_nugget)==1&&output(restored,Items.redstone)==1,"pending batch lost or duplicated after reload");
        check(restored.getStackInSlot(0).getTagCompound().getInteger("Health")==17,"queen health lost on reload");
        restored.setInventorySlotContents(10,new ItemStack(AdvancedApiary.module,1,0));restored.setInventorySlotContents(11,new ItemStack(AdvancedApiary.module,1,1));
        check(restored.modules(0)==1&&restored.modules(1)==1,"modules not functional");
        ItemStack[] inv=new ItemStack[2];inv[0]=new ItemStack(Items.gold_nugget,63);inv[1]=new ItemStack(Blocks.dirt,64);
        check(InventoryOps.plan(inv,0,2,Arrays.asList(new ItemStack(Items.gold_nugget,2)))==null&&inv[0].stackSize==63,"failed output transaction mutated inventory");
        check(InventoryOps.plan(inv,0,2,Arrays.asList(new ItemStack(Items.gold_nugget)))[0].stackSize==64,"stack merge failed");
        net.minecraft.entity.player.InventoryPlayer player=new net.minecraft.entity.player.InventoryPlayer(null);
        TileApiary transfer=new TestTile(false);ContainerApiary c=new ContainerApiary(player,transfer);
        player.setInventorySlotContents(9,new ItemStack(AdvancedApiary.module,9,0));
        c.transferStackInSlot(null,33);
        check(transfer.modules(0)==8&&player.getStackInSlot(9).stackSize==1,"shift-click module limit caused loss or stacking");
        for(int i=10;i<18;i++)check(transfer.getStackInSlot(i).stackSize==1,"module slot stack limit violated");
        player.setInventorySlotContents(10,new ItemStack(food,64));c.transferStackInSlot(null,34);
        check(plantCount(transfer)==0&&player.getStackInSlot(10).stackSize==64,"shift-click bypassed locked garden");
        ItemStack removedSpeed=transfer.decrStackSize(17,1);check(removedSpeed!=null&&removedSpeed.stackSize==1,"module extraction lost item");
        transfer.setInventorySlotContents(17,new ItemStack(AdvancedApiary.module,1,ItemModule.GARDEN));c.transferStackInSlot(null,34);
        check(plantCount(transfer)==64&&player.getStackInSlot(10)==null,"shift-click food routing failed");
        player.setInventorySlotContents(11,queen(1));c.transferStackInSlot(null,35);
        check(transfer.getStackInSlot(0)==null,"shift-click inserted a hybrid");
        player.setInventorySlotContents(12,queen(0));c.transferStackInSlot(null,36);
        check(transfer.getStackInSlot(0)!=null&&player.getStackInSlot(12)==null,"shift-click queen routing failed");
        c.updateProgressBar(0,4464);c.updateProgressBar(6,1);check(transfer.progress[0]==70000,"long cycle synchronization overflowed");
        transfer.setInventorySlotContents(17,removedSpeed);
        Settings.cycleTicks=550;check(transfer.cycleLength()==61,"eight speed modules have no effect");
        TileApiary botanical=new TestTile();botanical.setInventorySlotContents(0,queen(0));botanical.setInventorySlotContents(1,queen(4));
        botanical.setInventorySlotContents(6,new ItemStack(food));
        botanical.advance(0,new Random(1));botanical.advance(1,new Random(1));
        check(botanical.status[0]==TileApiary.WORKING&&botanical.status[1]==TileApiary.NO_FLOWERS,"queen-specific plant requirements ignored");
        botanical.setInventorySlotContents(7,new ItemStack(Blocks.cactus));botanical.advance(1,new Random(1));
        check(botanical.status[1]==TileApiary.WORKING,"shared cactus did not satisfy cactus queen");
        int paused=botanical.progress[0];botanical.setInventorySlotContents(6,null);botanical.advance(0,new Random(1));
        check(botanical.progress[0]==paused&&botanical.status[0]==TileApiary.NO_FLOWERS,"removing plants did not pause production");
        check(!botanical.isItemValidForSlot(6,new ItemStack(Items.gold_nugget)),"nonplant accepted");
        Settings.cycleTicks=20;
        TileApiary parallel=new TestTile();parallel.setInventorySlotContents(0,queen(0));parallel.setInventorySlotContents(6,new ItemStack(food));parallel.setInventorySlotContents(10,new ItemStack(AdvancedApiary.module,1,ItemModule.PARALLEL));
        for(int i=0;i<20;i++)parallel.advance(0,new Random(1));
        check(output(parallel,Items.gold_nugget)==2&&output(parallel,Items.redstone)==2,"parallel module did not add production pass");
        check(plantCount(parallel)==1,"parallel production consumed plant specimen");
        for(int i=0;i<ItemModule.TYPES;i++)check(parallel.isItemValidForSlot(11,new ItemStack(AdvancedApiary.module,1,i)),"new module rejected");
        check(!parallel.isItemValidForSlot(11,new ItemStack(AdvancedApiary.module,1,ItemModule.TYPES)),"unknown module accepted");
        specialtyChance=.5F;IBee probabilistic=bee(true,true);specialtyChance=1F;
        Random fixed=new Random(){@Override public float nextFloat(){return .6F;}};
        check(BeeProduction.roll(probabilistic,fixed,1,1).size()==1,"baseline specialty chance ignored");
        check(BeeProduction.roll(probabilistic,fixed,1,1.5).size()==2,"specialty modifier has no effect");
        TileApiary export=new TestTile();export.setInventorySlotContents(18,new ItemStack(Items.gold_nugget,4));
        net.minecraft.inventory.InventoryBasic chest=new net.minecraft.inventory.InventoryBasic("test",false,1);
        check(OutputExport.moveOne(export,18,chest,0,1)&&export.getStackInSlot(18).stackSize==3&&chest.getStackInSlot(0).stackSize==1,"export lost or duplicated an item");
        chest.setInventorySlotContents(0,new ItemStack(Items.gold_nugget,64));
        check(!OutputExport.moveOne(export,18,chest,0,1)&&export.getStackInSlot(18).stackSize==3,"export into full inventory consumed output");
        net.minecraft.inventory.InventoryBasic restrictive=new net.minecraft.inventory.InventoryBasic("one",false,1){@Override public void setInventorySlotContents(int i,ItemStack stack){if(stack!=null)stack.stackSize=Math.min(1,stack.stackSize);super.setInventorySlotContents(i,stack);}};
        restrictive.setInventorySlotContents(0,new ItemStack(Items.gold_nugget));
        check(!OutputExport.moveOne(export,18,restrictive,0,1)&&export.getStackInSlot(18).stackSize==3&&restrictive.getStackInSlot(0).stackSize==1,"restrictive export target lost an item");
        net.minecraft.tileentity.TileEntityFurnace furnace=new net.minecraft.tileentity.TileEntityFurnace();
        check(!OutputExport.moveOne(export,18,furnace,2,1)&&export.getStackInSlot(18).stackSize==3,"export ignored sided output-only restrictions");
        final IFlower wart=proxy(IFlower.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getBlock"))return Blocks.nether_wart;if(m.getName().equals("getMeta"))return 32767;throw new AssertionError("Unexpected flower callback");}});
        check(InternalFlowers.matches(new ItemStack(Items.nether_wart),Collections.singleton(wart)),"nether wart item not mapped to internal plant");
        final IFlower variant=proxy(IFlower.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getBlock"))return Blocks.red_flower;if(m.getName().equals("getMeta"))return 1;throw new AssertionError("Unexpected flower callback");}});
        check(InternalFlowers.matches(new ItemStack(Blocks.red_flower,1,1),Collections.singleton(variant)),"matching flower metadata rejected");
        check(!InternalFlowers.matches(new ItemStack(Blocks.red_flower,1,2),Collections.singleton(variant)),"wrong flower metadata accepted");
        check(BeeProduction.roll(fractional,fixed,1,3,.5).size()==1,"focus did not trade ordinary products for specialties");
        TileApiary sorted=new TestTile();sorted.setInventorySlotContents(18,new ItemStack(Items.gold_nugget,2));sorted.setInventorySlotContents(20,new ItemStack(Items.gold_nugget,3));sorted.setInventorySlotContents(25,new ItemStack(Items.redstone));
        OutputSort.run(sorted);int occupied=0;for(int i=18;i<33;i++)if(sorted.getStackInSlot(i)!=null)occupied++;
        check(occupied==2&&output(sorted,Items.gold_nugget)==5&&output(sorted,Items.redstone)==1,"sorter lost products or failed to compact stacks");
        ItemStack red=new ItemStack(Items.gold_nugget),blue=new ItemStack(Items.gold_nugget);NBTTagCompound redTag=new NBTTagCompound(),blueTag=new NBTTagCompound();redTag.setString("Color","red");blueTag.setString("Color","blue");red.setTagCompound(redTag);blue.setTagCompound(blueTag);
        sorted.setInventorySlotContents(18,red);sorted.setInventorySlotContents(19,blue);OutputSort.run(sorted);occupied=0;for(int i=18;i<33;i++)if(sorted.getStackInSlot(i)!=null)occupied++;
        check(occupied==2,"sorter merged different NBT products");
        IBeeGenome g=pure.getGenome();int[] upgrades=new int[ItemModule.TYPES];
        ApiaryEnvironment.Snapshot normal=env(EnumTemperature.NORMAL,EnumHumidity.NORMAL,false,true,true,15);
        check(ApiaryEnvironment.check(g,normal,upgrades)==TileApiary.WORKING,"suitable external environment rejected");
        ApiaryEnvironment.Snapshot rainy=env(EnumTemperature.NORMAL,EnumHumidity.NORMAL,true,true,true,15);
        check(ApiaryEnvironment.check(g,rainy,upgrades)==ApiaryEnvironment.RAINING,"rain did not stop work");
        upgrades[ItemModule.RAIN]=1;check(ApiaryEnvironment.check(g,rainy,upgrades)==TileApiary.WORKING,"weather shield ineffective");upgrades[ItemModule.RAIN]=0;
        ApiaryEnvironment.Snapshot night=env(EnumTemperature.NORMAL,EnumHumidity.NORMAL,false,false,true,0);
        check(ApiaryEnvironment.check(g,night,upgrades)==ApiaryEnvironment.NEEDS_DAY,"day queen worked at night");
        upgrades[ItemModule.LIGHT]=1;check(ApiaryEnvironment.check(g,night,upgrades)==TileApiary.WORKING,"lighting failed to simulate day");upgrades[ItemModule.LIGHT]=0;
        ApiaryEnvironment.Snapshot roof=env(EnumTemperature.NORMAL,EnumHumidity.NORMAL,false,true,false,15);
        check(ApiaryEnvironment.check(g,roof,upgrades)==ApiaryEnvironment.NO_SKY,"roof ignored");
        upgrades[ItemModule.SKY]=1;check(ApiaryEnvironment.check(g,roof,upgrades)==TileApiary.WORKING,"sky simulator ineffective");upgrades[ItemModule.SKY]=0;
        ApiaryEnvironment.Snapshot cold=env(EnumTemperature.COLD,EnumHumidity.NORMAL,false,true,true,15);
        check(ApiaryEnvironment.check(g,cold,upgrades)==ApiaryEnvironment.TOO_COLD,"cold climate ignored");
        upgrades[ItemModule.HEATER]=1;check(ApiaryEnvironment.check(g,cold,upgrades)==TileApiary.WORKING,"heater ineffective");upgrades[ItemModule.HEATER]=0;
        ApiaryEnvironment.Snapshot warm=env(EnumTemperature.WARM,EnumHumidity.NORMAL,false,true,true,15);
        check(ApiaryEnvironment.check(g,warm,upgrades)==ApiaryEnvironment.TOO_HOT,"warm climate ignored");
        upgrades[ItemModule.COOLER]=1;check(ApiaryEnvironment.check(g,warm,upgrades)==TileApiary.WORKING,"cooler ineffective");upgrades[ItemModule.COOLER]=0;
        ApiaryEnvironment.Snapshot dry=env(EnumTemperature.NORMAL,EnumHumidity.ARID,false,true,true,15);
        check(ApiaryEnvironment.check(g,dry,upgrades)==ApiaryEnvironment.TOO_DRY,"dry climate ignored");
        upgrades[ItemModule.HUMIDIFIER]=1;check(ApiaryEnvironment.check(g,dry,upgrades)==TileApiary.WORKING,"humidifier ineffective");upgrades[ItemModule.HUMIDIFIER]=0;
        ApiaryEnvironment.Snapshot damp=env(EnumTemperature.NORMAL,EnumHumidity.DAMP,false,true,true,15);
        check(ApiaryEnvironment.check(g,damp,upgrades)==ApiaryEnvironment.TOO_HUMID,"humid climate ignored");
        upgrades[ItemModule.DRYER]=1;check(ApiaryEnvironment.check(g,damp,upgrades)==TileApiary.WORKING,"dehumidifier ineffective");upgrades[ItemModule.DRYER]=0;
        ApiaryEnvironment.Snapshot hostile=env(EnumTemperature.HELLISH,EnumHumidity.ARID,false,true,true,15);
        upgrades[ItemModule.CLIMATE]=1;check(ApiaryEnvironment.check(g,hostile,upgrades)==TileApiary.WORKING,"climate controller ineffective");
        check(ApiaryEnvironment.check(g,rainy,upgrades)==ApiaryEnvironment.RAINING,"climate controller bypassed rain");
        check(ApiaryEnvironment.check(g,roof,upgrades)==ApiaryEnvironment.NO_SKY,"climate controller bypassed sky");upgrades[ItemModule.CLIMATE]=0;
        check(ApiaryEnvironment.check(g,env(EnumTemperature.NORMAL,EnumHumidity.NORMAL,false,true,true,4),upgrades)==ApiaryEnvironment.TOO_DARK,"darkness ignored");
        Map<String,Object> values=new HashMap<String,Object>();values.put("isNocturnal",true);
        IAlleleBeeSpecies nocturnalSpecies=override(IAlleleBeeSpecies.class,g.getPrimary(),values);
        values=new HashMap<String,Object>();values.put("getPrimary",nocturnalSpecies);IBeeGenome nocturnal=override(IBeeGenome.class,g,values);
        check(ApiaryEnvironment.check(nocturnal,normal,upgrades)==ApiaryEnvironment.NEEDS_NIGHT,"night queen worked in daylight");
        upgrades[ItemModule.DARK]=1;check(ApiaryEnvironment.check(nocturnal,normal,upgrades)==TileApiary.WORKING,"darkening ineffective");upgrades[ItemModule.DARK]=0;
        check(ApiaryEnvironment.check(nocturnal,night,upgrades)==TileApiary.WORKING,"night queen cannot work at night");
        values=new HashMap<String,Object>();values.put("getTolerantFlyer",true);check(ApiaryEnvironment.check(override(IBeeGenome.class,g,values),rainy,upgrades)==TileApiary.WORKING,"rain-tolerant gene ignored");
        values=new HashMap<String,Object>();values.put("getCaveDwelling",true);check(ApiaryEnvironment.check(override(IBeeGenome.class,g,values),roof,upgrades)==TileApiary.WORKING,"cave gene ignored");
        values=new HashMap<String,Object>();values.put("getToleranceTemp",EnumTolerance.DOWN_1);check(ApiaryEnvironment.check(override(IBeeGenome.class,g,values),cold,upgrades)==TileApiary.WORKING,"temperature tolerance ignored");
        check(ApiaryEnvironment.within(4,3,EnumTolerance.UP_1)&&!ApiaryEnvironment.within(2,3,EnumTolerance.UP_1),"directional tolerance incorrect");
        TestTile affected=new TestTile();affected.setInventorySlotContents(0,queen(0));affected.setInventorySlotContents(6,new ItemStack(food));affected.environment=normal;affected.advance(0,new Random(1));int beforeWeather=affected.progress[0];
        affected.environment=rainy;affected.advance(0,new Random(1));check(affected.progress[0]==beforeWeather&&affected.status[0]==ApiaryEnvironment.RAINING,"bad weather did not pause actual production");
        affected.setInventorySlotContents(10,new ItemStack(AdvancedApiary.module,1,ItemModule.RAIN));affected.advance(0,new Random(1));check(affected.progress[0]==beforeWeather+1,"weather module did not resume production");
        final int[] aiCalls=new int[5]; // effect, grow, retrieve, pollinate, memory reuse
        final IEffectData memory=proxy(IEffectData.class,new Call(){public Object run(Method m,Object[] a){throw new AssertionError("unexpected effect storage call");}});
        final IIndividual pollenSample=proxy(IIndividual.class,new Call(){public Object run(Method m,Object[] a){throw new AssertionError("unexpected pollen method");}});
        final IBee aiBee=proxy(IBee.class,new Call(){public Object run(Method m,Object[] a){
            if(m.getName().equals("doEffect")){aiCalls[0]++;IEffectData[] state=(IEffectData[])a[0];if(state[0]==memory)aiCalls[4]++;else check(state[0]==null,"unexpected effect memory");return new IEffectData[]{memory,null};}
            if(m.getName().equals("plantFlowerRandom")){aiCalls[1]++;return null;}
            if(m.getName().equals("retrievePollen")){aiCalls[2]++;return pollenSample;}
            if(m.getName().equals("pollinateRandom")){check(a[1]==pollenSample,"pollen changed");return ++aiCalls[3]%2==0;}
            try{return m.invoke(pure,a);}catch(Exception e){throw new AssertionError(e);}
        }});
        final IBeeHousing aiHousing=proxy(IBeeHousing.class,new Call(){public Object run(Method m,Object[] a){throw new AssertionError("unexpected housing call "+m);}});
        BeeActivity activity=new BeeActivity(aiHousing);
        Settings.disableBeeAI=true;activity.tick(aiBee,true);
        check(aiCalls[0]==0&&aiCalls[1]==0&&aiCalls[2]==0&&aiCalls[3]==0,"disabled AI invoked world hooks");
        check(!activity.hasExternalFlowers(aiBee,0),"disabled AI searched external flowers");
        Settings.disableBeeAI=false;activity.tick(aiBee,false);
        check(aiCalls[0]==1&&aiCalls[1]==0,"per-tick effects or cycle gating incorrect");
        activity.tick(aiBee,true);check(aiCalls[0]==2&&aiCalls[4]==1&&aiCalls[1]==1&&aiCalls[2]==1&&aiCalls[3]==1,"enabled AI hooks not executed or effect state lost");
        activity.tick(aiBee,true);check(aiCalls[2]==1&&aiCalls[3]==2,"pollen was retrieved again before pollination finished");
        activity.tick(aiBee,true);check(aiCalls[2]==2,"successful pollination did not clear pollen");
        Settings.disableBeeAI=true;int stopped=aiCalls[0];activity.tick(aiBee,true);check(aiCalls[0]==stopped,"AI toggle failed to stop effects");
        final IFlowerRegistry originalFlowers=FlowerManager.flowerRegistry;final int[] searches={0};
        FlowerManager.flowerRegistry=proxy(IFlowerRegistry.class,new Call(){public Object run(Method m,Object[] a){
            if(m.getName().equals("getAcceptedFlowerCoordinates")){searches[0]++;return new net.minecraft.util.ChunkCoordinates(1,64,1);}
            try{return m.invoke(originalFlowers,a);}catch(Exception e){throw new AssertionError(e);}
        }});
        Settings.disableBeeAI=false;check(activity.hasExternalFlowers(aiBee,0)&&activity.hasExternalFlowers(aiBee,100)&&searches[0]==1,"external flower search not enabled or cached");
        activity.hasExternalFlowers(aiBee,201);check(searches[0]==2,"external flower cache never refreshed");
        Settings.disableBeeAI=true;check(!activity.hasExternalFlowers(aiBee,202)&&searches[0]==2,"disabled AI bypassed search guard");
        final IBeeRoot originalRoot=BeeManager.beeRoot;
        BeeManager.beeRoot=proxy(IBeeRoot.class,new Call(){public Object run(Method m,Object[] a){if(m.getName().equals("getMember")&&((ItemStack)a[0]).getTagCompound().getInteger("TestKind")==6)return aiBee;try{return m.invoke(originalRoot,a);}catch(Exception e){throw new AssertionError(e);}}});
        TestTile enabledTile=new TestTile(false){@Override protected IBeeHousing createBeeHousing(int slot){return aiHousing;}};
        enabledTile.setInventorySlotContents(0,queen(6));Settings.cycleTicks=20;Settings.disableBeeAI=false;
        for(int i=0;i<20;i++)enabledTile.advance(0,new Random(1));
        check(output(enabledTile,Items.gold_nugget)==1&&enabledTile.getStackInSlot(0).getTagCompound().getInteger("Health")==17,"AI mode broke production or immortal queen NBT");
        check(searches[0]==3,"AI mode without internal plants did not use external flowers");
        int stoppedByRain=aiCalls[0];enabledTile.environment=rainy;enabledTile.advance(0,new Random(1));
        check(enabledTile.status[0]==ApiaryEnvironment.RAINING&&aiCalls[0]==stoppedByRain,"enabled AI bypassed environmental pause");
        Settings.disableBeeAI=true;enabledTile.environment=normal;enabledTile.advance(0,new Random(1));
        check(enabledTile.status[0]==TileApiary.NO_FLOWERS,"disabled AI accepted only external flowers");
        c.updateProgressBar(20,0);check(!c.clientDisableBeeAI,"server AI flag not synchronized to GUI");c.updateProgressBar(20,1);check(c.clientDisableBeeAI,"disabled AI flag not synchronized to GUI");
        BeeManager.beeRoot=originalRoot;FlowerManager.flowerRegistry=originalFlowers;
        ApiaryBeeHousing readOnly=new ApiaryBeeHousing(affected,0);ItemStack exposed=readOnly.getBeeInventory().getQueen();exposed.getTagCompound().setInteger("Health",0);readOnly.getBeeInventory().setQueen(null);
        check(affected.getStackInSlot(0).getTagCompound().getInteger("Health")==17,"housing exposed mutable queen NBT");
        com.mojang.authlib.GameProfile profile=new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ApiaryTester");affected.setOwner(profile);NBTTagCompound owned=new NBTTagCompound();affected.writeToNBT(owned);TileApiary ownerReloaded=new TestTile();ownerReloaded.readFromNBT(owned);
        check(ownerReloaded.owner().getId().equals(profile.getId()),"bee housing owner lost on reload");
        java.io.File configFile=new java.io.File("build/config-check/advancedapiary.cfg");configFile.getParentFile().mkdirs();if(configFile.exists()&&!configFile.delete())throw new AssertionError("cannot reset test config");
        Field gameDir=cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");gameDir.setAccessible(true);gameDir.set(null,new java.io.File("build/config-check"));
        Settings.load(configFile);check(!Settings.disableBeeAI,"fresh config must enable AI");
        net.minecraftforge.common.config.Configuration config=new net.minecraftforge.common.config.Configuration(configFile);config.load();config.get("bees","disableBeeAI",false).set(true);config.save();Settings.load(configFile);
        check(Settings.disableBeeAI,"existing explicit true config overwritten by new default");
        config.get("bees","disableBeeAI",false).set(false);config.save();Settings.load(configFile);
        check(!Settings.disableBeeAI,"config false value ignored");Settings.disableBeeAI=true;
        TestTile locked=new TestTile(false);locked.setInventorySlotContents(0,queen(0));locked.setInventorySlotContents(6,new ItemStack(food));
        check(!locked.hasInternalGarden(),"garden unlocked without upgrade");
        for(int slot=6;slot<10;slot++)check(!locked.isItemValidForSlot(slot,new ItemStack(food)),"locked plant slot accepted insertion");
        check(!locked.canInsertItem(6,new ItemStack(food),2)&&locked.getAccessibleSlotsFromSide(2).length==15,"hopper bypassed garden lock");
        locked.advance(0,new Random(1));check(locked.status[0]==TileApiary.NO_FLOWERS&&locked.progress[0]==0,"locked stored plants enabled production");
        locked.setInventorySlotContents(10,new ItemStack(AdvancedApiary.module,1,ItemModule.GARDEN));
        check(locked.hasInternalGarden()&&locked.getAccessibleSlotsFromSide(2).length==19,"garden did not expose four plant slots");
        for(int slot=6;slot<10;slot++)check(locked.isItemValidForSlot(slot,new ItemStack(food)),"garden did not unlock all four slots");
        locked.advance(0,new Random(1));check(locked.progress[0]==1&&locked.status[0]==TileApiary.WORKING,"unlocked internal plant failed");
        ItemStack removedGarden=locked.decrStackSize(10,1);locked.advance(0,new Random(1));
        check(removedGarden!=null&&!locked.hasInternalGarden()&&locked.status[0]==TileApiary.NO_FLOWERS&&locked.progress[0]==1,"removing garden did not pause internal production");
        check(plantCount(locked)==1,"garden removal deleted stored plants");
        NBTTagCompound lockedNbt=new NBTTagCompound();locked.writeToNBT(lockedNbt);TestTile loadedLocked=new TestTile();loadedLocked.readFromNBT(lockedNbt);
        check(!loadedLocked.hasInternalGarden()&&plantCount(loadedLocked)==1&&!loadedLocked.isItemValidForSlot(6,new ItemStack(food)),"reload unlocked garden or lost old plants");
        net.minecraft.entity.player.InventoryPlayer recoveryPlayer=new net.minecraft.entity.player.InventoryPlayer(null);ContainerApiary recovery=new ContainerApiary(recoveryPlayer,loadedLocked);
        check(recovery.transferStackInSlot(null,6)!=null&&loadedLocked.getStackInSlot(6)==null,"locked stored plants cannot be recovered");
        TestTile nightMissing=new TestTile(false);nightMissing.setInventorySlotContents(0,queen(0));nightMissing.environment=night;
        nightMissing.advance(0,new Random(1));check(nightMissing.status[0]==ApiaryEnvironment.NEEDS_DAY,"missing flowers masked night reason");
        nightMissing.environment=normal;nightMissing.advance(0,new Random(1));check(nightMissing.status[0]==TileApiary.NO_FLOWERS,"missing flowers not reported in suitable climate");
        for(int face=0;face<6;face++){
            int[] pipeSlots=locked.getAccessibleSlotsFromSide(face);
            for(int slot=18;slot<33;slot++)check(java.util.Arrays.binarySearch(java.util.Arrays.copyOf(pipeSlots,15),slot)>=0&&locked.canExtractItem(slot,new ItemStack(Items.gold_nugget),face),"pipe cannot extract output on face "+face);
            for(int slot=0;slot<18;slot++)check(!locked.canExtractItem(slot,new ItemStack(food),face),"pipe exposed protected inventory");
        }
        Settings.cycleTicks=550;TestTile seven=new TestTile();seven.setInventorySlotContents(0,queen(0));seven.setInventorySlotContents(6,new ItemStack(food));
        for(int slot=10;slot<17;slot++)seven.setInventorySlotContents(slot,new ItemStack(AdvancedApiary.module,1,ItemModule.PARALLEL));
        for(int tick=0;tick<550;tick++)seven.advance(0,new Random(tick));
        check(output(seven,Items.gold_nugget)==8&&output(seven,Items.redstone)==8,"seven parallel modules must produce eight passes");
        TestTile balanced=new TestTile();balanced.setInventorySlotContents(0,queen(0));balanced.setInventorySlotContents(6,new ItemStack(food));
        for(int slot=10;slot<17;slot++)balanced.setInventorySlotContents(slot,new ItemStack(AdvancedApiary.module,1,slot<13?ItemModule.SPEED:ItemModule.PARALLEL));
        for(int tick=0;tick<550;tick++)balanced.advance(0,new Random(tick));
        check(balanced.cycleLength()==137&&output(balanced,Items.gold_nugget)==20,"balanced speed/parallel combination has incorrect throughput");
        // Fixed seeded long series tests the chance calculation, rather than recreating the RNG every tick.
        Random sequence=new Random(68291);int successes=0;
        for(int attempt=0;attempt<80000;attempt++)for(ItemStack result:BeeProduction.roll(fractional,sequence,1,1))if(result.getItem()==Items.redstone)successes++;
        check(Math.abs(successes-20000)<600,"parallel specialty yield differs from binomial expectation");
        Settings.disableBeeAI=false;
        System.out.println("PASS: "+checks+" checks; six immortal queens, internal plants, purity, external environment, no bee effect/lifecycle callbacks, modules, atomic output and pending NBT persistence");
    }
}
