package de.cas_ual_ty.spells;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class SpellsConfig
{
    public static final ForgeConfigSpec GENERAL_SPEC;
    
    public static final ForgeConfigSpec.BooleanValue RESPAWN_WITH_FULL_MANA;
    public static final ForgeConfigSpec.BooleanValue CLEAR_SLOTS_ON_DEATH;
    public static final ForgeConfigSpec.BooleanValue FORGET_SPELLS_ON_DEATH;
    
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENCHANTING_TABLE;
    
    public static final ForgeConfigSpec.BooleanValue DEBUG_SPELLS;
    public static final ForgeConfigSpec.IntValue ACTION_JUMP_LIMIT;
    
    public static final ForgeConfigSpec.BooleanValue GLOBAL_ITEM_COSTS;
    public static final ForgeConfigSpec.BooleanValue GLOBAL_TERRAIN_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue GLOBAL_PVP;
    
    static
    {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        
        configBuilder.comment("Settings related to when a player dies.").push("onDeath");
        RESPAWN_WITH_FULL_MANA = configBuilder
                .comment("Players respawn with their mana bar filled (true) or empty (false).")
                .define("respawnWithFullMana", false);
        CLEAR_SLOTS_ON_DEATH = configBuilder
                .comment("Clear a player's spell slots on death (true) or not (false). If forgetSpellsOnDeath is set to true, this always behaves as if it was also set to true.")
                .define("clearSlots", true);
        FORGET_SPELLS_ON_DEATH = configBuilder
                .comment("Make a player forget all learned spells on death (true) or not (false). Forgotten spells are still visible in spell trees but must be relearned before you can equip them.")
                .define("forgetSpells", true);
        configBuilder.pop();
        
        configBuilder.push("misc");
        ENCHANTING_TABLE = configBuilder
                .comment("Resource location of the enchanting table. Some mods could change that.")
                .defineList("enchantingTables", ImmutableList.of("minecraft:enchanting_table", "quark:matrix_enchanter"), s -> true);
        configBuilder.pop();
        
        configBuilder.comment("Settings relevant for data pack creators.").push("technical");
        DEBUG_SPELLS = configBuilder
                .comment("Debug spells on use. For data pack creators.")
                .define("debugSpells", false);
        ACTION_JUMP_LIMIT = configBuilder
                .comment("Hard limit of jumps by spell actions to prevent endless loops (which would result in a crash).")
                .defineInRange("actionJumpLimit", 1000, 10, 10000);
        configBuilder.pop();
        
        configBuilder.push("globals");
        GLOBAL_ITEM_COSTS = configBuilder
                .comment("Switch to turn off item costs.")
                .define("itemCosts", true);
        GLOBAL_TERRAIN_DAMAGE = configBuilder
                .comment("Switch to turn off terrain damage. Fire may still be spawned.")
                .define("terrainDamage", true);
        GLOBAL_PVP = configBuilder
                .comment("Switch to turn off PvP for spells. Any negative aspects are not applied to the player anymore if they come from a player source.")//TODO
                .define("pvp", true);
        configBuilder.pop();
        
        GENERAL_SPEC = configBuilder.build();
    }
    
    public static void registerGlobals()
    {
        Compiler.registerSupplier("item_costs", CtxVarTypes.BOOLEAN, GLOBAL_ITEM_COSTS);
        Compiler.registerSupplier("terrain_damage", CtxVarTypes.BOOLEAN, GLOBAL_TERRAIN_DAMAGE);
        Compiler.registerSupplier("pvp", CtxVarTypes.BOOLEAN, GLOBAL_PVP);
    }
}
