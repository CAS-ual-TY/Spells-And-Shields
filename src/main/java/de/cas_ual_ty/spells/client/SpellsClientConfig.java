package de.cas_ual_ty.spells.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpellsClientConfig
{
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    public static final ForgeConfigSpec.IntValue MANA_HIDE_DELAY;
    public static final ForgeConfigSpec.BooleanValue MANA_ABOVE_FOOD;
    public static final ForgeConfigSpec.BooleanValue MANA_BY_HEALTH;
    public static final ForgeConfigSpec.BooleanValue MANA_JITTER;
    public static final ForgeConfigSpec.BooleanValue ALWAYS_SHOW_SPELL_SLOTS;
    public static final ForgeConfigSpec.IntValue SPELL_SLOTS_POSITION_SURVIVAL;
    public static final ForgeConfigSpec.IntValue SPELL_SLOTS_POSITION_CREATIVE;
    public static final ForgeConfigSpec.IntValue SPELL_SLOTS_POSITION_OFFSET_X;
    public static final ForgeConfigSpec.IntValue SPELL_SLOTS_POSITION_OFFSET_Y;
    
    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        MANA_HIDE_DELAY = builder
                .comment("Hide the mana bar if it has not changed for the set amount of time (in ticks, 1 second = 20 ticks). Set to 0 to always have it visible.")
                .defineInRange("manaHideDelay", 0, 0, 1200); // max = 1 min
        MANA_ABOVE_FOOD = builder
                .comment("Put the mana bar above the food/health bar (true) or else put it below (false).")
                .define("manaAboveFood", true);
        MANA_BY_HEALTH = builder
                .comment("Put the mana bar next to the health bar (true) or else leave it next to the food bar (false).")
                .define("manaByHealth", false);
        MANA_JITTER = builder
                .comment("The mana bar icons jitter when your current mana is low (true) or else the icons always stay put (false).")
                .define("manaJitter", true);
        ALWAYS_SHOW_SPELL_SLOTS = builder
                .comment("Always show all spell slots (true) or else only show them if at least 1 spell is equipped (false).")
                .define("alwaysShowSpellSlots", true);
        
        builder.push("spellSlots");
        SPELL_SLOTS_POSITION_SURVIVAL = builder
                .comment("The position to put the spell slots in the survival inventory gui. These values represent the position by going counter-clockwise around the gui with 3 available positions on each side.")
                .defineInRange("spellSlotsPositionSurvival", 0, 0, 11);
        SPELL_SLOTS_POSITION_CREATIVE = builder
                .comment("The position to put the spell slots in the creative inventory gui. These values represent the position by going counter-clockwise around the gui with 3 available positions on each side.")
                .defineInRange("spellSlotsPositionCreative", 0, 0, 11);
        SPELL_SLOTS_POSITION_OFFSET_X = builder
                .comment("Manually move the x position of the spell slots by the set amount.")
                .defineInRange("spellSlotsPositionOffsetX", 0, -800, 800);
        SPELL_SLOTS_POSITION_OFFSET_Y = builder
                .comment("Manually move the y position of the spell slots by the set amount.")
                .defineInRange("spellSlotsPositionOffsetY", 0, -800, 800);
        builder.pop();
        
        CLIENT_SPEC = builder.build();
    }
}
