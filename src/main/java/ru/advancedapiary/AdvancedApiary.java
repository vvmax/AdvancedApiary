package ru.advancedapiary;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid=AdvancedApiary.ID,name="Advanced Apiary",version="0.6.5",dependencies="required-after:Forestry@[4.2.16,5.0)")
public final class AdvancedApiary {
    public static final String ID="advancedapiary";
    @Mod.Instance(ID) public static AdvancedApiary instance;
    @SidedProxy(clientSide="ru.advancedapiary.client.ClientProxy",serverSide="ru.advancedapiary.CommonProxy") public static CommonProxy proxy;
    public static Block apiary;
    public static Item module;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent e){
        Settings.load(e.getSuggestedConfigurationFile());
        apiary=new BlockApiary();module=new ItemModule();
        GameRegistry.registerBlock(apiary,"advancedApiary");GameRegistry.registerItem(module,"module");
        GameRegistry.registerTileEntity(TileApiary.class,ID+":apiary");
        NetworkRegistry.INSTANCE.registerGuiHandler(this,proxy);
    }
    @Mod.EventHandler public void init(FMLInitializationEvent e){
        proxy.registerRenderers();
        Item jelly=GameRegistry.findItem("Forestry","royalJelly");
        if(jelly==null)throw new IllegalStateException("Forestry apiculture items are required (royalJelly missing)");
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(apiary),"GJG","DCD","GJG",'G',Items.gold_ingot,'J',jelly,'D',Items.diamond,'C',Blocks.chest));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(module,1,ItemModule.SPEED)," R ","GIG"," R ",'R',Items.redstone,'G',Items.gold_ingot,'I',Items.iron_ingot));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(module,1,ItemModule.PRODUCTIVITY)," J ","GDG"," J ",'J',jelly,'G',Items.gold_ingot,'D',Items.diamond));
        Object[] centers={Items.emerald,Blocks.crafting_table,Blocks.hopper,Items.ender_pearl,Items.comparator,Items.blaze_powder,Blocks.ice,Items.water_bucket,Blocks.sponge,Blocks.glass,Blocks.glowstone,Items.dye,Items.ender_eye,Items.nether_star,Items.flower_pot};
        for(int i=2;i<ItemModule.TYPES;i++)GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(module,1,i)," G ","RCR"," G ",'G',Items.gold_ingot,'R',Items.redstone,'C',centers[i-2]));
    }
}
