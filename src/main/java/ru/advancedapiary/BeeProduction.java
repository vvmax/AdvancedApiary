package ru.advancedapiary;

import java.util.*;
import forestry.api.apiculture.*;
import net.minecraft.item.ItemStack;

/** Does not accept a World or IBeeHousing. Never executes any bee lifecycle or effect hook. */
public final class BeeProduction {
    public static IBee inspect(ItemStack stack) {
        if(stack==null || stack.stackSize!=1 || BeeManager.beeRoot==null) return null;
        try {
            if(BeeManager.beeRoot.getType(stack)!=EnumBeeType.QUEEN) return null;
            // Forestry reads a defensive copy: original queen NBT must never be modified.
            IBee bee = BeeManager.beeRoot.getMember(stack.copy());
            return isPure(bee) ? bee : null;
        } catch(RuntimeException invalidNbt) { return null; }
    }
    public static boolean isPure(IBee bee) {
        if(bee==null || !bee.isPureBred(EnumBeeChromosome.SPECIES)) return false;
        if(Settings.allGenesPure) for(EnumBeeChromosome gene: EnumBeeChromosome.values()) if(!bee.isPureBred(gene)) return false;
        return true;
    }
    public static List<ItemStack> roll(IBee bee,Random random,double multiplier,double specialtyMultiplier) {
        return roll(bee,random,multiplier,specialtyMultiplier,1);
    }
    public static List<ItemStack> roll(IBee bee,Random random,double multiplier,double specialtyMultiplier,double normalMultiplier) {
        List<ItemStack> result=new ArrayList<ItemStack>();
        double speed=Math.max(0,bee.getGenome().getSpeed())*Settings.production*multiplier;
        append(result,bee.getGenome().getPrimary().getProductChances(),random,speed*normalMultiplier);
        if(Settings.specialties)append(result,bee.getGenome().getPrimary().getSpecialtyChances(),random,speed*specialtyMultiplier);
        return result;
    }
    private static void append(List<ItemStack> out,Map<ItemStack,Float> table,Random rng,double speed) {
        for(Map.Entry<ItemStack,Float> e:table.entrySet()) {
            if(e.getKey()!=null&&e.getValue()!=null&&rng.nextFloat()<Math.min(1,e.getValue()*speed))out.add(e.getKey().copy());
        }
    }
    private BeeProduction(){}
}
