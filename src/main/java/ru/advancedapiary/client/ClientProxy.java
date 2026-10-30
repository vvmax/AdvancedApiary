package ru.advancedapiary.client;
import ru.advancedapiary.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
public final class ClientProxy extends CommonProxy {
    @Override public void registerRenderers(){ApiaryRenderer.register();}
    @Override public Object getClientGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){return id==0&&w.getTileEntity(x,y,z) instanceof TileApiary?new GuiApiary(new ContainerApiary(p.inventory,(TileApiary)w.getTileEntity(x,y,z))):null;}
}
