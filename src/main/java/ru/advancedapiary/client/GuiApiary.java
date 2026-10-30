package ru.advancedapiary.client;
import java.util.*;
import ru.advancedapiary.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

public final class GuiApiary extends GuiContainer {
    private static final ResourceLocation TEX=new ResourceLocation("advancedapiary","textures/gui/advanced_apiary.png");
    private final ContainerApiary container;
    public GuiApiary(ContainerApiary c){super(c);container=c;xSize=248;ySize=238;}
    private void text(String value,float[] box,int color){text(value,box,color,0,0);}
    private void text(String value,float[] box,int color,int dx,int dy){
        float scale=Math.min(box[4],Math.min(box[2]/Math.max(1,fontRendererObj.getStringWidth(value)),box[3]/fontRendererObj.FONT_HEIGHT));
        float x=box[0]+dx+(box[5]>0?(box[2]-fontRendererObj.getStringWidth(value)*scale)/2:0);
        float y=box[1]+dy+(box[3]-fontRendererObj.FONT_HEIGHT*scale)/2;
        GL11.glPushMatrix();GL11.glTranslatef(x,y,0);GL11.glScalef(scale,scale,1);fontRendererObj.drawString(value,0,0,color);GL11.glPopMatrix();
    }
    private String tr(String key,Object...args){return StatCollector.translateToLocalFormatted(key,args);}
    @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
        text(tr("container.advancedapiary"),GuiTextLayout.TITLE,0xF4DFB4);
        int active=0;for(int s:container.tile.status)if(s==TileApiary.WORKING)active++;
        text(tr("gui.apiary.active",active),GuiTextLayout.COUNTER,0xF4DFB4);
        text(tr("gui.apiary.products"),GuiTextLayout.PRODUCTS,0x493A25);
        text(tr("gui.apiary.flowers"),GuiTextLayout.PLANTS,0xF4DFB4);
        text(tr("gui.apiary.modules"),GuiTextLayout.MODULES,0xF4DFB4);
        for(int i=0;i<6;i++)text(tr("gui.apiary.queen",i+1),GuiTextLayout.QUEEN,0xFFF0C9,(i%3)*56,(i/3)*48);
        text(tr(container.tile.hasInternalGarden()?"gui.apiary.flowerNote":"gui.apiary.gardenLocked"),GuiTextLayout.NOTE,0xF4DFB4);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float t,int mx,int my){
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(TEX);
        drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);
        if(!container.tile.hasInternalGarden())for(int i=0;i<4;i++){
            int x=guiLeft+10+i*18,y=guiTop+134;
            drawRect(x+1,y+1,x+17,y+17,0xFF625C49);
            drawRect(x+6,y+6,x+12,y+8,0xFFD6BB7C);
            drawRect(x+5,y+8,x+13,y+13,0xFFD6BB7C);
            drawRect(x+7,y+9,x+10,y+11,0xFF625C49);
        }
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(TEX);
        for(int i=0;i<6;i++){
            int x=8+(i%3)*56,y=24+(i/3)*48;
            int state=container.tile.status[i],u=state==TileApiary.WORKING?0:state==TileApiary.EMPTY?14:6;
            drawTexturedModalRect(guiLeft+x+35,guiTop+y+18,u,247,5,5);
            int length=Math.max(1,container.clientCycle),width=(int)(42*Math.min(1F,(float)container.tile.progress[i]/length));
            drawTexturedModalRect(guiLeft+x+4,guiTop+y+37,0,240,width,4);
        }
    }
    private boolean over(int mx,int my,int x,int y,int w,int h){return mx>=x&&my>=y&&mx<x+w&&my<y+h;}
    @Override public void drawScreen(int mx,int my,float t){
        super.drawScreen(mx,my,t);int x=mx-guiLeft,y=my-guiTop;List<String> tips=null;
        for(int i=0;i<6;i++){
            int bx=8+(i%3)*56,by=24+(i/3)*48;
            if(over(x,y,bx+31,by+12,15,16)) {
                String requirement=tr("gui.apiary.unknownFlowers");
                forestry.api.apiculture.IBee bee=BeeProduction.inspect(container.tile.getStackInSlot(i));
                if(bee!=null)requirement=bee.getGenome().getFlowerProvider().getDescription();
                tips=Arrays.asList(tr("gui.apiary.state."+container.tile.status[i]),tr("gui.apiary.requiredFlowers",requirement),tr(container.clientDisableBeeAI?"gui.apiary.aiDisabled":"gui.apiary.aiEnabled"));
            }
            if(over(x,y,bx+3,by+35,44,9))tips=Arrays.asList(tr("gui.apiary.cycle",container.tile.progress[i],container.clientCycle));
        }
        if(over(x,y,88,125,145,9))tips=Arrays.asList(
            tr("gui.apiary.productionSummary",1+container.tile.modules(ItemModule.PARALLEL),String.format(java.util.Locale.ROOT,"%.2f",container.clientCycle/20.0)),
            tr("gui.apiary.productionChance"));
        // Item hints are appended to vanilla item tooltips by the module item; blank slots get role hints here.
        for(int i=0;i<18;i++){
            Slot slot=(Slot)inventorySlots.inventorySlots.get(i);
            if(over(x,y,slot.xDisplayPosition,slot.yDisplayPosition,16,16)&&i>=6&&i<10&&!container.tile.hasInternalGarden()) {tips=slot.getHasStack()?Arrays.asList(slot.getStack().getDisplayName(),tr("gui.apiary.gardenLocked"),tr("gui.apiary.gardenStored")):Arrays.asList(tr("gui.apiary.gardenLocked"));}
            else if(!slot.getHasStack()&&over(x,y,slot.xDisplayPosition,slot.yDisplayPosition,16,16)) {
                if(i<6)tips=Arrays.asList(tr("gui.apiary.queenHint"),tr("gui.apiary.immortal"));
                else if(i<10)tips=Arrays.asList(tr("gui.apiary.flowersHint"),tr("gui.apiary.flowerNote"));
                else tips=Arrays.asList(tr("gui.apiary.moduleHint"));
            }
        }
        if(tips!=null){List<String> wrapped=new ArrayList<String>();for(String line:tips)wrapped.addAll(fontRendererObj.listFormattedStringToWidth(line,Math.min(230,width-24)));drawHoveringText(wrapped,mx,my,fontRendererObj);}
    }
}
