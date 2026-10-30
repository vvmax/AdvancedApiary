package ru.advancedapiary;
import forestry.api.apiculture.*;
import forestry.api.core.*;
import forestry.api.genetics.EnumTolerance;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/** Reads environmental conditions only. Never runs bee lifecycle, flower searches or effects. */
public final class ApiaryEnvironment {
    public static final int RAINING=5,NEEDS_DAY=6,NEEDS_NIGHT=7,TOO_DARK=8,TOO_BRIGHT=9,
        NO_SKY=10,TOO_COLD=11,TOO_HOT=12,TOO_DRY=13,TOO_HUMID=14,UNKNOWN=15;
    public static final class Snapshot {
        public final EnumTemperature temperature;public final EnumHumidity humidity;
        public final boolean raining,day,sky,noSky;public final int light;
        public Snapshot(EnumTemperature t,EnumHumidity h,boolean rain,boolean day,boolean sky,boolean noSky,int light){this.temperature=t;this.humidity=h;this.raining=rain;this.day=day;this.sky=sky;this.noSky=noSky;this.light=light;}
    }
    public static Snapshot read(World world,int x,int y,int z){
        BiomeGenBase biome=world.getBiomeGenForCoords(x,z);int above=Math.min(255,y+1);
        return new Snapshot(EnumTemperature.getFromBiome(biome,x,y,z),EnumHumidity.getFromValue(biome.rainfall),
            world.isRaining()&&(biome.getEnableSnow()||biome.canSpawnLightningBolt()),world.isDaytime(),
            world.canBlockSeeTheSky(x,above,z),world.provider.hasNoSky,world.getBlockLightValue(x,above,z));
    }
    public static boolean within(int actual,int expected,EnumTolerance tolerance){
        String name=tolerance.name();if(name.equals("NONE"))return actual==expected;
        int steps=name.charAt(name.length()-1)-'0';int delta=actual-expected;
        if(name.startsWith("BOTH"))return Math.abs(delta)<=steps;
        if(name.startsWith("UP"))return delta>=0&&delta<=steps;
        return delta<=0&&delta>=-steps;
    }
    private static int clamp(int n,int min,int max){return Math.max(min,Math.min(max,n));}
    public static int check(IBeeGenome genome,Snapshot env,int[] modules){
        if(env==null||env.temperature==null||env.temperature==EnumTemperature.NONE||env.humidity==null)return UNKNOWN;
        IAlleleBeeSpecies species=genome.getPrimary();
        if(env.raining&&!genome.getTolerantFlyer()&&modules[ItemModule.RAIN]==0)return RAINING;
        boolean nightOnly=species.isNocturnal()&&!genome.getNocturnal();
        boolean day=env.day;int light=env.light;
        if(nightOnly&&modules[ItemModule.DARK]>0){day=false;light=0;}
        else if(!nightOnly&&modules[ItemModule.LIGHT]>0){day=true;light=15;}
        boolean canDay=!species.isNocturnal()||genome.getNocturnal();
        boolean canNight=species.isNocturnal()||genome.getNocturnal();
        if(day&&!canDay)return NEEDS_NIGHT;
        if(!day&&!canNight)return NEEDS_DAY;
        if(light>11&&!canDay)return TOO_BRIGHT;
        if(light<=11&&!canNight)return TOO_DARK;
        if(!env.noSky&&!env.sky&&!genome.getCaveDwelling()&&modules[ItemModule.SKY]==0)return NO_SKY;
        int temperature=clamp(env.temperature.ordinal()+modules[ItemModule.HEATER]-modules[ItemModule.COOLER],1,EnumTemperature.values().length-1);
        int humidity=clamp(env.humidity.ordinal()+modules[ItemModule.HUMIDIFIER]-modules[ItemModule.DRYER],0,EnumHumidity.values().length-1);
        if(modules[ItemModule.CLIMATE]>0){temperature=species.getTemperature().ordinal();humidity=species.getHumidity().ordinal();}
        if(!within(temperature,species.getTemperature().ordinal(),genome.getToleranceTemp()))return temperature<species.getTemperature().ordinal()?TOO_COLD:TOO_HOT;
        if(!within(humidity,species.getHumidity().ordinal(),genome.getToleranceHumid()))return humidity<species.getHumidity().ordinal()?TOO_DRY:TOO_HUMID;
        return TileApiary.WORKING;
    }
    private ApiaryEnvironment(){}
}
