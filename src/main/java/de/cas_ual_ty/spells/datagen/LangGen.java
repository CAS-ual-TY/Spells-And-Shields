package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import de.cas_ual_ty.spells.registers.RequirementTypes;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.requirement.AdvancementRequirement;
import de.cas_ual_ty.spells.requirement.ItemRequirement;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
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
        super(dataGen, SpellsAndShields.MOD_ID, locale);
    }
    
    public static final String PERMANENT_EFFECT_NAME = "%s";
    public static final String PERMANENT_EFFECT_DESC = "Applies the %s effect while this spell is equipped.";
    public static final String TEMPORARY_EFFECT_NAME = "%s";
    public static final String TEMPORARY_EFFECT_DESC = "Applies the %s effect for a short period of time.";
    
    @Override
    protected void addTranslations()
    {
        addAttribute(BuiltinRegistries.MAX_MANA_ATTRIBUTE, "Max Mana");
        addAttribute(BuiltinRegistries.MANA_REGENERATION_ATTRIBUTE, "Mana Regeneration");
        
        // support JEI Enchantment Info
        // https://www.curseforge.com/minecraft/mc-mods/jei-enchantment-info
        add("enchantment." + SpellsAndShields.MOD_ID + ".type." + BuiltinRegistries.SHIELD_ENCHANTMENT_CATEGORY.name().toLowerCase(), "shields");
        add("enchantment." + SpellsAndShields.MOD_ID + ".type." + BuiltinRegistries.SWORD_OR_AXE_ENCHANTMENT_CATEGORY.name().toLowerCase(), "axes, swords");
        
        addEnchantment(BuiltinRegistries.MAGIC_PROTECTION_ENCHANTMENT, "Magic Protection", "Reduces magic damage.");
        addEnchantment(BuiltinRegistries.MANA_BLADE_ENCHANTMENT, "Mana Blade", "Consumes mana to increase damage.");
        addEnchantment(BuiltinRegistries.MANA_SHIELD_ENCHANTMENT, "Mana Shield", "WIP"); //TODO mana shield ench description
        addEnchantment(BuiltinRegistries.MAX_MANA_ENCHANTMENT, "Maximum Mana", "Increases your maximum mana while worn. More potent on chestplates and leggings than helmets and boots.");
        addEnchantment(BuiltinRegistries.MANA_REGENERATION_ENCHANTMENT, "Mana Regeneration", "Increases your mana regeneration while worn. More potent on chestplates and leggings than helmets and boots.");
        
        addEffect(BuiltinRegistries.INSTANT_MANA_EFFECT, "Instant Mana", "Replenishes mana; higher levels increase the effect potency.");
        addEffect(BuiltinRegistries.MANA_BOMB_EFFECT, "Mana Bomb", "Burns mana; higher levels increase the effect potency.");
        addEffect(BuiltinRegistries.REPLENISHMENT_EFFECT, "Replenishment", "Replenishes mana over time; higher levels make mana be replenished quicker.");
        addEffect(BuiltinRegistries.LEAKING_MOB_EFFECT, "Leaking", "Burns mana over time; higher levels burn more mana.");
        addEffect(BuiltinRegistries.MANA_BOOST_EFFECT, "Mana Boost", "Increases maximum mana; higher levels give more additional mana bottles.");
        addEffect(BuiltinRegistries.EXTRA_MANA_EFFECT, "Extra Mana", "Adds burnable mana bottles (which can't be replenished); higher levels give more extra mana.");
        addEffect(BuiltinRegistries.SILENCE_EFFECT, "Silence", "No spells can be used while this effect is active.");
        addEffect(BuiltinRegistries.MAGIC_IMMUNE_EFFECT, "Magic Immune", "Makes you ignore any magic damage.");
        
        addPotion(BuiltinRegistries.INSTANT_MANA, "Instant Mana");
        addPotion(BuiltinRegistries.STRONG_INSTANT_MANA, "Instant Mana");
        
        addPotion(BuiltinRegistries.MANA_BOMB, "Mana Bomb");
        addPotion(BuiltinRegistries.STRONG_MANA_BOMB, "Mana Bomb");
        
        addPotion(BuiltinRegistries.REPLENISHMENT, "Replenishment");
        addPotion(BuiltinRegistries.LONG_REPLENISHMENT, "Replenishment");
        addPotion(BuiltinRegistries.STRONG_REPLENISHMENT, "Replenishment");
        
        addPotion(BuiltinRegistries.LEAKING, "Leaking");
        addPotion(BuiltinRegistries.LONG_LEAKING, "Leaking");
        addPotion(BuiltinRegistries.STRONG_LEAKING, "Leaking");
        
        add(SpellKeyBindings.CATEGORY, "Spells & Shields");
        for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
        {
            add(SpellKeyBindings.key(i), "Spell Slot " + (i + 1));
        }
        
        addRequirement(RequirementTypes.BOOKSHELVES, "%s/%s Bookshelves");
        addRequirement(RequirementTypes.ADVANCEMENT, "Advancement: %s");
        addRequirement(RequirementTypes.ADVANCEMENT, AdvancementRequirement.ERROR_SUFFIX, "Unknown Advancement (config error): %s");
        addRequirement(RequirementTypes.ITEM, "%s (Not Consumed)");
        addRequirement(RequirementTypes.ITEM, ItemRequirement.CONSUMED_SUFFIX, "%s (Consumed)");
        addRequirement(RequirementTypes.ITEM, ItemRequirement.MULTIPLE_SUFFIX, "%sx %s (Not Consumed)");
        addRequirement(RequirementTypes.ITEM, ItemRequirement.MULTIPLE_CONSUMED_SUFFIX, "%sx %s (Consumed)");
        
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
        add(SpellCommand.SPELLS_SLOT_SET_SINGLE, "Set active spell in slot %s of %s to '%s'");
        add(SpellCommand.SPELLS_SLOT_SET_MULTIPLE, "Set active spell in slot %s of %s players to '%s'");
        add(SpellCommand.SPELLS_SLOT_CLEAR_SINGLE, "Cleared active spells of %s");
        add(SpellCommand.SPELLS_SLOT_CLEAR_MULTIPLE, "Cleared active spells of %s players");
        
        add(SpellProgressionScreen.KEY_LEARN, "Learn");
        add(SpellProgressionScreen.KEY_EQUIP, "Equip");
        add(SpellProgressionScreen.KEY_UNAVAILABLE, "Unavailable");
        add(SpellProgressionScreen.KEY_CHOOSE_SLOT, "Choose a Slot");
        
        add(SpellTrees.KEY_NETHER, "Nether");
        add(SpellTrees.KEY_OCEAN, "Ocean");
        add(SpellTrees.KEY_MINING, "Mining");
        add(SpellTrees.KEY_MOVEMENT, "Movement");
        add(SpellTrees.KEY_END, "End");
        
        add(Spells.KEY_TEST, "Test Spell");
        add(Spells.KEY_LEAP, "Leap");
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
