package ru.advancedapiary;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
/** Stable item key, including recursively sorted NBT compound keys. */
public final class ItemStackKey {
    public static String key(ItemStack stack){return Item.itemRegistry.getNameForObject(stack.getItem())+":"+stack.getItemDamage()+":"+canonical(stack.getTagCompound());}
    private static String canonical(NBTBase tag){
        if(tag==null)return "-";
        if(tag instanceof NBTTagCompound){NBTTagCompound compound=(NBTTagCompound)tag;List<String> keys=new ArrayList<String>();for(Object key:compound.func_150296_c())keys.add((String)key);Collections.sort(keys);StringBuilder out=new StringBuilder("{");for(String key:keys)out.append(key.length()).append(':').append(key).append('=').append(canonical(compound.getTag(key))).append(';');return out.append('}').toString();}
        if(tag instanceof NBTTagList){NBTTagList list=(NBTTagList)tag.copy();StringBuilder out=new StringBuilder("[");while(list.tagCount()>0)out.append(canonical(list.removeTag(0))).append(";");return out.append("]").toString();}
        return tag.getId()+":"+tag.toString();
    }
    private ItemStackKey(){}
}
