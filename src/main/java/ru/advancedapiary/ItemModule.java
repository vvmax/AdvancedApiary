package ru.advancedapiary;
import java.util.List;
import net.minecraft.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.*;
public final class ItemModule extends Item {
    public static final int SPEED=0,PRODUCTIVITY=1,SPECIALTY=2,PARALLEL=3,EXPORT=4,FOCUS=5,SORTER=6,HEATER=7,COOLER=8,HUMIDIFIER=9,DRYER=10,RAIN=11,LIGHT=12,DARK=13,SKY=14,CLIMATE=15,GARDEN=16,TYPES=17;
    private static final String[] NAMES={"speed","productivity","specialty","parallel","export","focus","sorter","heater","cooler","humidifier","dryer","rain","light","dark","sky","climate","garden"};
    public ItemModule(){setHasSubtypes(true);setMaxDamage(0);setUnlocalizedName("apiaryModule");setCreativeTab(CreativeTabs.tabMisc);}
    public static String name(int damage){return damage>=0&&damage<TYPES?NAMES[damage]:"invalid";}
    @Override public String getUnlocalizedName(ItemStack stack){return "item.apiaryModule."+name(stack.getItemDamage());}
    @Override public void getSubItems(Item item,CreativeTabs tab,List list){for(int i=0;i<TYPES;i++)list.add(new ItemStack(item,1,i));}
    @SideOnly(Side.CLIENT) private net.minecraft.util.IIcon[] icons;
    @Override @SideOnly(Side.CLIENT) public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister r){icons=new net.minecraft.util.IIcon[TYPES];for(int i=0;i<TYPES;i++)icons[i]=r.registerIcon("advancedapiary:module_"+NAMES[i]);itemIcon=icons[0];}
    @Override @SideOnly(Side.CLIENT) public net.minecraft.util.IIcon getIconFromDamage(int damage){return icons[damage>=0&&damage<TYPES?damage:0];}
    @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack stack,EntityPlayer player,List lines,boolean advanced){lines.addAll(net.minecraft.client.Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(StatCollector.translateToLocal("gui.apiary.module."+name(stack.getItemDamage())),220));}
}
