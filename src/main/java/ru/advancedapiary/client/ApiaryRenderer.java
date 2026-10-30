package ru.advancedapiary.client;

import cpw.mods.fml.client.registry.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.*;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import ru.advancedapiary.BlockApiary;

/** Static multipart mesh, chunk-batched: no TileEntity renderer or per-frame allocations. */
public final class ApiaryRenderer implements ISimpleBlockRenderingHandler {
    private static final double[][] PARTS={
        {0,0,0,1,.125,1}, {.0625,.125,.0625,.9375,.8125,.9375},
        {0,.8125,0,1,.9375,1}, {.0625,.9375,.0625,.9375,1,.9375},
        {0,.125,0,.125,.8125,.125}, {.875,.125,0,1,.8125,.125},
        {0,.125,.875,.125,.8125,1}, {.875,.125,.875,1,.8125,1},
        {.125,.125,0,.875,.1875,.0625}, {.125,.4375,0,.875,.5,.0625},
        {.125,.75,0,.875,.8125,.0625},
        {.125,.125,.9375,.875,.1875,1}, {.125,.4375,.9375,.875,.5,1},
        {.125,.75,.9375,.875,.8125,1}
    };
    public static void register(){BlockApiary.renderId=RenderingRegistry.getNextAvailableRenderId();RenderingRegistry.registerBlockHandler(new ApiaryRenderer());}
    private void bounds(RenderBlocks r,double[] b){r.setRenderBounds(b[0],b[1],b[2],b[3],b[4],b[5]);}
    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks r){
        boolean old=r.renderAllFaces;r.renderAllFaces=true;
        for(double[] part:PARTS){bounds(r,part);r.renderStandardBlock(block,x,y,z);}
        r.renderAllFaces=old;r.setRenderBounds(0,0,0,1,1,1);return true;
    }
    @Override public void renderInventoryBlock(Block block,int meta,int modelId,RenderBlocks r){
        GL11.glPushMatrix();GL11.glTranslatef(-.5F,-.5F,-.5F);Tessellator t=Tessellator.instance;
        for(double[] part:PARTS){bounds(r,part);
            t.startDrawingQuads();t.setNormal(0,-1,0);r.renderFaceYNeg(block,0,0,0,block.getIcon(0,meta));t.draw();
            t.startDrawingQuads();t.setNormal(0,1,0);r.renderFaceYPos(block,0,0,0,block.getIcon(1,meta));t.draw();
            t.startDrawingQuads();t.setNormal(0,0,-1);r.renderFaceZNeg(block,0,0,0,block.getIcon(2,meta));t.draw();
            t.startDrawingQuads();t.setNormal(0,0,1);r.renderFaceZPos(block,0,0,0,block.getIcon(3,meta));t.draw();
            t.startDrawingQuads();t.setNormal(-1,0,0);r.renderFaceXNeg(block,0,0,0,block.getIcon(4,meta));t.draw();
            t.startDrawingQuads();t.setNormal(1,0,0);r.renderFaceXPos(block,0,0,0,block.getIcon(5,meta));t.draw();
        }r.setRenderBounds(0,0,0,1,1,1);GL11.glPopMatrix();
    }
    @Override public boolean shouldRender3DInInventory(int modelId){return true;}
    @Override public int getRenderId(){return BlockApiary.renderId;}
}
