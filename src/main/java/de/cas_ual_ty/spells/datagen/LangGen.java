package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import de.cas_ual_ty.spells.registers.RequirementTypes;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.requirement.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.function.Supplier;

public class LangGen extends LanguageProvider
{
    public LangGen(DataGenerator dataGen, String locale)
    {
        super(dataGen.getPackOutput(), SpellsAndShields.MOD_ID, locale);
    }
    
    public static final String PERMANENT_EFFECT_NAME = "Perm. %s";
    public static final String PERMANENT_EFFECT_DESC = "Applies the %s effect while this spell is equipped.";
    public static final String TEMPORARY_EFFECT_NAME = "Temp. %s";
    public static final String TEMPORARY_EFFECT_DESC = "Applies the %s effect for a short period of time.";
    public static final String TOGGLE_EFFECT_NAME = "Toggle %s";
    public static final String TOGGLE_EFFECT_DESC = "Toggles the %s effect and burns mana (per 5 seconds) while it is active.";
    
    public static final String PERMANENT_SPELL_NAME = "Perm. %s";
    public static final String PERMANENT_SPELL_DESC = "Applies the following effect while this spell is equipped: %s";
    public static final String TEMPORARY_SPELL_NAME = "Temp. %s";
    public static final String TEMPORARY_SPELL_DESC = "Applies the following effect for a short period of time: %s";
    public static final String TOGGLE_SPELL_NAME = "Toggle %s";
    public static final String TOGGLE_SPELL_DESC = "Toggles the following effect and burns mana (per 5 seconds) while it is active: %s";
    
    @Override
    protected void addTranslations()
    {
        addAttribute(BuiltInRegisters.MAX_MANA_ATTRIBUTE, "Max Mana");
        addAttribute(BuiltInRegisters.MANA_REGENERATION_ATTRIBUTE, "Mana Regeneration");
        
        // support JEI Enchantment Info
        // https://www.curseforge.com/minecraft/mc-mods/jei-enchantment-info
        add("enchantment." + SpellsAndShields.MOD_ID + ".type." + BuiltInRegisters.SHIELD_ENCHANTMENT_CATEGORY.name().toLowerCase(), "shields");
        add("enchantment." + SpellsAndShields.MOD_ID + ".type." + BuiltInRegisters.SWORD_OR_AXE_ENCHANTMENT_CATEGORY.name().toLowerCase(), "axes, swords");
        
        addEnchantment(BuiltInRegisters.MAGIC_PROTECTION_ENCHANTMENT, "Magic Protection", "Reduces magic damage.");
        addEnchantment(BuiltInRegisters.MANA_BLADE_ENCHANTMENT, "Mana Blade", "Consumes mana to increase damage.");
        addEnchantment(BuiltInRegisters.MANA_SHIELD_ENCHANTMENT, "Mana Shield", "WIP"); //TODO mana shield ench description
        addEnchantment(BuiltInRegisters.MAX_MANA_ENCHANTMENT, "Maximum Mana", "Increases your maximum mana while worn. More potent on chestplates and leggings than helmets and boots.");
        addEnchantment(BuiltInRegisters.MANA_REGENERATION_ENCHANTMENT, "Mana Regeneration", "Increases your mana regeneration while worn. More potent on chestplates and leggings than helmets and boots.");
        
        addEffect(BuiltInRegisters.INSTANT_MANA_EFFECT, "Instant Mana", "Replenishes mana; higher levels increase the effect potency.");
        addEffect(BuiltInRegisters.MANA_BOMB_EFFECT, "Mana Bomb", "Burns mana; higher levels increase the effect potency.");
        addEffect(BuiltInRegisters.REPLENISHMENT_EFFECT, "Replenishment", "Replenishes mana over time; higher levels make mana be replenished quicker.");
        addEffect(BuiltInRegisters.LEAKING_MOB_EFFECT, "Leaking", "Burns mana over time; higher levels burn more mana.");
        addEffect(BuiltInRegisters.MANA_BOOST_EFFECT, "Mana Boost", "Increases maximum mana; higher levels give more additional mana bottles.");
        addEffect(BuiltInRegisters.EXTRA_MANA_EFFECT, "Extra Mana", "Adds burnable mana bottles (which can't be replenished); higher levels give more extra mana.");
        addEffect(BuiltInRegisters.SILENCE_EFFECT, "Silence", "No spells can be used while this effect is active.");
        addEffect(BuiltInRegisters.MAGIC_IMMUNE_EFFECT, "Magic Immune", "Makes you ignore any magic damage.");
        
        addPotion(BuiltInRegisters.INSTANT_MANA, "Instant Mana");
        addPotion(BuiltInRegisters.STRONG_INSTANT_MANA, "Instant Mana");
        
        addPotion(BuiltInRegisters.MANA_BOMB, "Mana Bomb");
        addPotion(BuiltInRegisters.STRONG_MANA_BOMB, "Mana Bomb");
        
        addPotion(BuiltInRegisters.REPLENISHMENT, "Replenishment");
        addPotion(BuiltInRegisters.LONG_REPLENISHMENT, "Replenishment");
        addPotion(BuiltInRegisters.STRONG_REPLENISHMENT, "Replenishment");
        
        addPotion(BuiltInRegisters.LEAKING, "Leaking");
        addPotion(BuiltInRegisters.LONG_LEAKING, "Leaking");
        addPotion(BuiltInRegisters.STRONG_LEAKING, "Leaking");
        
        add(SpellKeyBindings.CATEGORY, "Spells & Shields");
        for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
        {
            add(SpellKeyBindings.key(i), "Spell Slot " + (i + 1));
        }
        add(SpellKeyBindings.keyRadialMenu(), "Radial Menu");
        
        addRequirement(RequirementTypes.BOOKSHELVES, "%s/%s Bookshelves");
        addRequirement(RequirementTypes.ADVANCEMENT, "Advancement: %s");
        addRequirement(RequirementTypes.ADVANCEMENT, AdvancementRequirement.ERROR_SUFFIX, "Unknown Advancement (config error): %s");
        addRequirement(RequirementTypes.ITEM, "%s (Not Consumed)");
        addRequirement(RequirementTypes.ITEM, ItemRequirement.CONSUMED_SUFFIX, "%s (Consumed)");
        addRequirement(RequirementTypes.ITEM, ItemRequirement.MULTIPLE_SUFFIX, "%sx %s (Not Consumed)");
        addRequirement(RequirementTypes.ITEM, ItemRequirement.MULTIPLE_CONSUMED_SUFFIX, "%sx %s (Consumed)");
        addRequirement(RequirementTypes.CONFIG, "Disabled by Configuration File");
        addRequirement(RequirementTypes.LIST, "%s/%s of the following:");
        addRequirement(RequirementTypes.LIST, ListRequirement.ANY_SUFFIX, "Any of the following:");
        addRequirement(RequirementTypes.LIST, ListRequirement.ALL_SUFFIX, "All of the following:");
        addRequirement(RequirementTypes.NOT, "Not the following:");
        
        add(SpellProgressionMenu.TITLE.getString(), "Spell Progression");
        
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_SINGLE, "Spell '%s' has been learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_SINGLE_FAILED, "Spell '%s' was already learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_MULTIPLE, "Spell '%s' has been learned by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_TREE_SINGLE, "Spell tree '%s' has been learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_TREE_SINGLE_FAILED, "Spell tree '%s' was already learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_TREE_MULTIPLE, "Spell tree '%s' has been learned by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_ALL_SINGLE, "All %s unlearned spells in %s spell trees have been learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_ALL_SINGLE_FAILED, "%s already learned all %s spells in %s spell trees");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_ALL_MULTIPLE, "All %s spells in %s spell trees have been learned by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_SINGLE, "Spell '%s' has been forgotten by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_SINGLE_FAILED, "Spell '%s' was already forgotten by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_MULTIPLE, "Spell '%s' has been forgotten by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_TREE_SINGLE, "Spell tree '%s' was forgotten by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_TREE_SINGLE_FAILED, "Not a single spell in spell tree '%s' was learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_TREE_MULTIPLE, "Spell tree 's%' have been forgotten by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_ALL_SINGLE, "All %s learned spells in %s spell trees have been forgotten by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_ALL_SINGLE_FAILED, "%s has never learned a single spell");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_ALL_MULTIPLE, "All %s spells have been forgotten by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_RESET_SINGLE, "Cleared all learned spells of %s");
        add(SpellCommand.SPELLS_PROGRESSION_RESET_MULTIPLE, "Cleared all learned spells of %s players");
        add(SpellCommand.SPELLS_SLOT_REMOVE_SINGLE, "Cleared active spell in slot %s of %s");
        add(SpellCommand.SPELLS_SLOT_REMOVE_MULTIPLE, "Cleared active spell in slot %s of %s players");
        add(SpellCommand.SPELLS_SLOT_SET_DIRECT_SINGLE, "Set active spell in slot %s of %s to '%s'");
        add(SpellCommand.SPELLS_SLOT_SET_DIRECT_MULTIPLE, "Set active spell in slot %s of %s players to '%s'");
        add(SpellCommand.SPELLS_SLOT_SET_SINGLE, "Set active spell in slot %s of %s to '%s' from spell tree '%s'");
        add(SpellCommand.SPELLS_SLOT_SET_MULTIPLE, "Set active spell in slot %s of %s players to '%s' from spell tree '%s'");
        add(SpellCommand.SPELLS_SLOT_CLEAR_SINGLE, "Cleared active spells of %s");
        add(SpellCommand.SPELLS_SLOT_CLEAR_MULTIPLE, "Cleared active spells of %s players");
        add(SpellCommand.UNKNOWN_NODE, "Spell tree '%s' does not have a spell node '%s'");
        
        add(SpellProgressionScreen.KEY_LEARN, "Learn");
        add(SpellProgressionScreen.KEY_EQUIP, "Equip");
        add(SpellProgressionScreen.KEY_UNAVAILABLE, "Unavailable");
        add(SpellProgressionScreen.KEY_CHOOSE_SLOT, "Choose a Slot");
        
        add(SpellTrees.KEY_NETHER, "Nether");
        add(SpellTrees.KEY_OCEAN, "Ocean");
        add(SpellTrees.KEY_MINING, "Mining");
        add(SpellTrees.KEY_MOVEMENT, "Movement");
        add(SpellTrees.KEY_END, "End");
        
        add(SpellsGen.KEY_HAND_ITEM_REQUIREMENT_TITLE, "Item Requirement (In Your Hand):");
        add(SpellsGen.KEY_HAND_ITEM_COST_TITLE, "Item Costs (Hand):");
        add(SpellsGen.KEY_MAINHAND_ITEM_COST_TITLE, "Item Costs (Mainhand):");
        add(SpellsGen.KEY_OFFHAND_ITEM_COST_TITLE, "Item Costs (Offhand):");
        add(SpellsGen.KEY_INVENTORY_ITEM_COST_TITLE, "Item Costs (Inventory):");
        add(SpellsGen.KEY_ITEM_COST, "- %sx %s");
        add(SpellsGen.KEY_ITEM_COST_SINGLE, "- %s");
        add(SpellsGen.KEY_ITEM_COST_TEXT, "- %s");
        
        add(Spells.KEY_LEAP, "Leap");
        add(Spells.KEY_LEAP_DESC, "Leap forward.");
        add(Spells.KEY_SUMMON_ANIMAL, "Summon Animal");
        add(Spells.KEY_SUMMON_ANIMAL_DESC, "Create life based on the item in your hand.");
        add(Spells.KEY_FIRE_BALL, "Fire Ball");
        add(Spells.KEY_FIRE_BALL_DESC, "Shoot a fire ball forward.");
        add(Spells.KEY_BLAST_SMELT, "Blast Smelt");
        add(Spells.KEY_BLAST_SMELT_DESC, "Works like an instant blast furnace on the item in your hand.");
        add(Spells.KEY_BLAST_SMELT_DESC_COST, "Any item that goes into a blast furnace");
        add(Spells.KEY_TRANSFER_MANA, "Transfer Mana");
        //add(Spells.KEY_TRANSFER_MANA_DESC, "."); TODO transfer mana desc
        add(Spells.KEY_BLOW_ARROW, "Blow Arrow");
        add(Spells.KEY_BLOW_ARROW_DESC, "Shoot a projectile from your hand without a bow.");
        add(Spells.KEY_HEALTH_BOOST, "Health Boost");
        add(Spells.KEY_HEALTH_BOOST_DESC, "Increases your maximum health.");
        add(Spells.KEY_MANA_BOOST, "Mana Boost");
        add(Spells.KEY_MANA_BOOST_DESC, "Increases your maximum mana.");
        add(Spells.KEY_WATER_LEAP, "Water Leap");
        add(Spells.KEY_WATER_LEAP_DESC, "Leap forward like a dolphin (must be underwater).");
        add(Spells.KEY_PERMANENT_AQUA_RESISTANCE, PERMANENT_EFFECT_NAME.formatted("Aqua Resistance"));
        add(Spells.KEY_PERMANENT_AQUA_RESISTANCE_DESC, "Take less damage from most mobs while underwater.");
        add(Spells.KEY_WATER_WHIP, "Water Whip");
        add(Spells.KEY_WATER_WHIP_DESC, "Shoots water out of the water bucket in your hand. The water returns and the bucket refills if you hold it on return");
        add(Spells.KEY_POTION_SHOT, "Potion Shot");
        add(Spells.KEY_POTION_SHOT_DESC, "Shoots the contents of the potion in your hand forward.");
        add(Spells.KEY_PERMANENT_FROST_WALKER, PERMANENT_SPELL_NAME.formatted("Frost Walker"));
        add(Spells.KEY_PERMANENT_FROST_WALKER_DESC, PERMANENT_SPELL_DESC.formatted("Walk on water by turning the blocks you walk on into ice."));
        add(Spells.KEY_TEMPORARY_FROST_WALKER, TEMPORARY_SPELL_NAME.formatted("Frost Walker"));
        add(Spells.KEY_TEMPORARY_FROST_WALKER_DESC, TEMPORARY_SPELL_DESC.formatted("Walk on water by turning the blocks you walk on into ice."));
        add(Spells.KEY_TOGGLE_FROST_WALKER, TOGGLE_SPELL_NAME.formatted("Frost Walker"));
        add(Spells.KEY_TOGGLE_FROST_WALKER_DESC, TOGGLE_SPELL_DESC.formatted("Walk on water by turning the blocks you walk on into ice."));
        add(Spells.KEY_JUMP, "Jump");
        add(Spells.KEY_JUMP_DESC, "High jump. Be aware of fall damage.");
        add(Spells.KEY_MANA_SOLES, "Mana Soles");
        add(Spells.KEY_MANA_SOLES_DESC, "Consumes mana to reduce or cancel fall damage.");
        add(Spells.KEY_FIRE_CHARGE, "Fire Charge");
        add(Spells.KEY_FIRE_CHARGE_DESC, "Shoot a fire charge forward instantly.");
        add(Spells.KEY_PRESSURIZE, "Pressurize");
        add(Spells.KEY_PRESSURIZE_DESC, "Knock back every entity around you and remove any fluid.");
        add(Spells.KEY_INSTANT_MINE, "Instant Mine");
        add(Spells.KEY_INSTANT_MINE_DESC, "Breaks the block you are looking at using the tool in your hand.");
        add(Spells.KEY_INSTANT_MINE_DESC_REQUIREMENT, "The tool to break the block with");
        add(Spells.KEY_SPIT_METAL, "Spit Metal");
        add(Spells.KEY_SPIT_METAL_DESC, "Spit a nugget that deals damage (from your hand).");
        add(Spells.KEY_FLAMETHROWER, "Flamethrower");
        add(Spells.KEY_FLAMETHROWER_DESC, "Breath flames from your mouth setting everything on fire.");
        add(Spells.KEY_PERMANENT_LAVA_WALKER, PERMANENT_SPELL_NAME.formatted("Lava Walker"));
        add(Spells.KEY_PERMANENT_LAVA_WALKER_DESC, PERMANENT_SPELL_DESC.formatted("Walk on lava by turning the blocks you walk on into obsidian."));
        add(Spells.KEY_TEMPORARY_LAVA_WALKER, TEMPORARY_SPELL_NAME.formatted("Lava Walker"));
        add(Spells.KEY_TEMPORARY_LAVA_WALKER_DESC, TEMPORARY_SPELL_DESC.formatted("Walk on lava by turning the blocks you walk on into obsidian."));
        add(Spells.KEY_TOGGLE_LAVA_WALKER, TOGGLE_SPELL_NAME.formatted("Lava Walker"));
        add(Spells.KEY_TOGGLE_LAVA_WALKER_DESC, TOGGLE_SPELL_DESC.formatted("Walk on lava by turning the blocks you walk on into obsidian."));
        add(Spells.KEY_SILENCE_TARGET, "Silence Target");
        add(Spells.KEY_SILENCE_TARGET_DESC, "Silence the target you are looking at within a certain range.");
        add(Spells.KEY_RANDOM_TELEPORT, "Random Teleport");
        add(Spells.KEY_RANDOM_TELEPORT_DESC, "Randomly teleport away. This spell can fail.");
        add(Spells.KEY_FORCED_TELEPORT, "Forced Teleport");
        add(Spells.KEY_FORCED_TELEPORT_DESC, "Randomly teleport the target you are looking at away. This spell can fail.");
        add(Spells.KEY_TELEPORT, "Teleport");
        add(Spells.KEY_TELEPORT_DESC, "Teleport to where you are looking at.");
        add(Spells.KEY_LIGHTNING_STRIKE, "Lightning Strike");
        add(Spells.KEY_LIGHTNING_STRIKE_DESC, "Summon a lightning strike where you are looking at. The target must see skylight.");
        add(Spells.KEY_DRAIN_FLAME, "Drain Flame");
        add(Spells.KEY_DRAIN_FLAME_DESC, "Drain fire to convert it into mana regeneration. The fire must be infinite (eg. on top of Netherrack) for this to work.");
        add(Spells.KEY_GROWTH, "Growth");
        add(Spells.KEY_GROWTH_DESC, "Apply the effect of Bonemeal to plants around you.");
        add(Spells.KEY_GHAST, "Ghast");
        add(Spells.KEY_GHAST_DESC, "Shoot a fire charge forward, like a Ghast.");
        add(Spells.KEY_ENDER_ARMY, "Ender Army");
        add(Spells.KEY_ENDER_ARMY_DESC, "Make all Endermen close to the target you are looking at attack said target.");
        add(Spells.KEY_EVOKER_FANGS, "Evoker Fangs");
        add(Spells.KEY_EVOKER_FANGS_DESC, "Summon Fangs forward like an Evoker.");
        add(Spells.KEY_POCKET_ROCKET, "Pocket Rocket");
        add(Spells.KEY_POCKET_ROCKET_DESC, "Fire a bunch of Firework Rockets in a row to boost Elytra flight.");
        
        add(Spells.KEY_PERMANENT_REPLENISHMENT, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_REPLENISHMENT_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_REPLENISHMENT, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_REPLENISHMENT_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_REPLENISHMENT, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_REPLENISHMENT_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_MAGIC_IMMUNE, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_MAGIC_IMMUNE_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_MAGIC_IMMUNE, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_MAGIC_IMMUNE_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_MAGIC_IMMUNE, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_MAGIC_IMMUNE_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_SPEED, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_SPEED_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_SPEED, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_SPEED_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_SPEED, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_SPEED_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_JUMP_BOOST, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_JUMP_BOOST_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_JUMP_BOOST, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_JUMP_BOOST_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_JUMP_BOOST, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_JUMP_BOOST_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_DOLPHINS_GRACE, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_DOLPHINS_GRACE_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_DOLPHINS_GRACE, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_DOLPHINS_GRACE_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_DOLPHINS_GRACE, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_DOLPHINS_GRACE_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_WATER_BREATHING, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_WATER_BREATHING_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_WATER_BREATHING, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_WATER_BREATHING_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_WATER_BREATHING, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_WATER_BREATHING_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_SLOW_FALLING, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_SLOW_FALLING_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_SLOW_FALLING, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_SLOW_FALLING_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_SLOW_FALLING, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_SLOW_FALLING_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_HASTE, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_HASTE_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_HASTE, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_HASTE_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_HASTE, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_HASTE_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_REGENERATION, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_REGENERATION_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_REGENERATION, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_REGENERATION_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_REGENERATION, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_REGENERATION_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_FIRE_RESISTANCE, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_FIRE_RESISTANCE_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_FIRE_RESISTANCE, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_FIRE_RESISTANCE_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_FIRE_RESISTANCE, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_FIRE_RESISTANCE_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_NIGHT_VISION, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_NIGHT_VISION_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_NIGHT_VISION, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_NIGHT_VISION_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_NIGHT_VISION, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_NIGHT_VISION_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_STRENGTH, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_STRENGTH_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_STRENGTH, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_STRENGTH_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_STRENGTH, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_STRENGTH_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_RESISTANCE, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_RESISTANCE_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_RESISTANCE, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_RESISTANCE_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_RESISTANCE, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_RESISTANCE_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_INVISIBILITY, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_INVISIBILITY_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_INVISIBILITY, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_INVISIBILITY_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_INVISIBILITY, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_INVISIBILITY_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_GLOWING, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_GLOWING_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_GLOWING, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_GLOWING_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_GLOWING, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_GLOWING_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_LUCK, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_LUCK_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_LUCK, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_LUCK_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_LUCK, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_LUCK_DESC, TOGGLE_EFFECT_DESC);
        
        add(Spells.KEY_PERMANENT_CONDUIT_POWER, PERMANENT_EFFECT_NAME);
        add(Spells.KEY_PERMANENT_CONDUIT_POWER_DESC, PERMANENT_EFFECT_DESC);
        add(Spells.KEY_TEMPORARY_CONDUIT_POWER, TEMPORARY_EFFECT_NAME);
        add(Spells.KEY_TEMPORARY_CONDUIT_POWER_DESC, TEMPORARY_EFFECT_DESC);
        add(Spells.KEY_TOGGLE_CONDUIT_POWER, TOGGLE_EFFECT_NAME);
        add(Spells.KEY_TOGGLE_CONDUIT_POWER_DESC, TOGGLE_EFFECT_DESC);
    }
    
    public void addAttribute(Supplier<? extends Attribute> key, String name)
    {
        add(key.get().getDescriptionId(), name);
    }
    
    public void addEnchantment(Supplier<? extends Enchantment> key, String name, String desc)
    {
        // support JEI Enchantment Info
        // https://www.curseforge.com/minecraft/mc-mods/jei-enchantment-info
        super.addEnchantment(key, name);
        add(key.get().getDescriptionId() + ".desc", desc);
    }
    
    public void addEffect(Supplier<? extends MobEffect> key, String name, String desc)
    {
        // support Just Enough Effect Descriptions
        // https://www.curseforge.com/minecraft/mc-mods/just-enough-effect-descriptions-jeed
        super.addEffect(key, name);
        add(key.get().getDescriptionId() + ".description", desc);
    }
    
    public void addPotion(Supplier<? extends Potion> key, String name)
    {
        add(PotionUtils.setPotion(new ItemStack(Items.POTION), key.get()), "Potion of " + name);
        add(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), key.get()), "Splash Potion of " + name);
        add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), key.get()), "Lingering Potion of " + name);
        add(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), key.get()), "Arrow of " + name);
    }
    
    public void addRequirement(Supplier<? extends RequirementType<?>> requirement, String desc)
    {
        addRequirement(requirement, "", desc);
    }
    
    public void addRequirement(Supplier<? extends RequirementType<?>> requirement, String suffix, String desc)
    {
        Requirement inst = requirement.get().makeInstance();
        String descriptionId = inst.getDescriptionId();
        add(descriptionId + suffix, desc);
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Lang File";
    }
}
