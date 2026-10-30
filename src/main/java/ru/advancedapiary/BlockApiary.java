package ru.advancedapiary;
import java.util.Random;
import cpw.mods.fml.relauncher.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
public final class BlockApiary extends BlockContainer {
    @SideOnly(Side.CLIENT) private IIcon front,top,side;
    public BlockApiary(){super(Material.wood);setBlockName("advancedApiary");setHardness(3F);setResistance(8F);setStepSound(soundTypeWood);setCreativeTab(CreativeTabs.tabDecorations);}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,net.minecraft.entity.EntityLivingBase entity,ItemStack stack){
        if(!world.isRemote&&entity instanceof EntityPlayer&&world.getTileEntity(x,y,z) instanceof TileApiary)((TileApiary)world.getTileEntity(x,y,z)).setOwner(((EntityPlayer)entity).getGameProfile());
    }
    public static int renderId=-1;
    @Override public int getRenderType(){return renderId;}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileApiary();}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float a,float b,float c){if(!world.isRemote)player.openGui(AdvancedApiary.instance,0,world,x,y,z);return true;}
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){
        TileEntity te=world.getTileEntity(x,y,z);
        if(te instanceof TileApiary){TileApiary tile=(TileApiary)te;for(int i=0;i<tile.getSizeInventory();i++){ItemStack stack=tile.getStackInSlot(i);if(stack==null)continue;
            EntityItem item=new EntityItem(world,x+.5,y+.5,z+.5,stack.copy());world.spawnEntityInWorld(item);tile.setInventorySlotContents(i,null);}}
        super.breakBlock(world,x,y,z,block,meta);
    }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r){front=r.registerIcon("advancedapiary:apiary_front");side=r.registerIcon("advancedapiary:apiary_side");top=r.registerIcon("advancedapiary:apiary_top");}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int face,int meta){return face==1||face==0?top:face==3?front:side;}
}
