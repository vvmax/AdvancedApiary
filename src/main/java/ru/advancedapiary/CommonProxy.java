package ru.advancedapiary;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
public class CommonProxy implements IGuiHandler {
    public void registerRenderers() {}
    @Override public Object getServerGuiElement(int id,EntityPlayer player,World world,int x,int y,int z){return id==0&&world.getTileEntity(x,y,z) instanceof TileApiary?new ContainerApiary(player.inventory,(TileApiary)world.getTileEntity(x,y,z)):null;}
    @Override public Object getClientGuiElement(int id,EntityPlayer player,World world,int x,int y,int z){return null;}
}
