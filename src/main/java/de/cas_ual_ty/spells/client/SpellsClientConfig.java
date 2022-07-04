package de.cas_ual_ty.spells.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpellsClientConfig
{
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    public static final ForgeConfigSpec.IntValue MANA_HIDE_DELAY;
    public static final ForgeConfigSpec.BooleanValue MANA_ABOVE_FOOD;
    
    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        MANA_HIDE_DELAY = builder
                .comment("Hide the mana bar if it has not changed for the set amount of time (in ticks, 1 second = 20 ticks). Set to 0 to always have it visible")
                .defineInRange("manaHideDelay", 0, 0, 1200); // max = 1 min
        MANA_ABOVE_FOOD = builder
                .comment("Put the mana bar above the food bar (true) or else put it below (false).")
                .define("manaAboveFood", false);
        
        CLIENT_SPEC = builder.build();
    }
}
