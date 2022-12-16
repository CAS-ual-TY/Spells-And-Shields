package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellTrees;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
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
        addAttribute(SpellsRegistries.MAX_MANA_ATTRIBUTE, "Max Mana");
        addAttribute(SpellsRegistries.MANA_REGENERATION_ATTRIBUTE, "Mana Regeneration");
        
        // support JEI Enchantment Info
        // https://www.curseforge.com/minecraft/mc-mods/jei-enchantment-info
        add("enchantment." + SpellsAndShields.MOD_ID + ".type." + SpellsRegistries.SHIELD_ENCHANTMENT_CATEGORY.name().toLowerCase(), "shields");
        add("enchantment." + SpellsAndShields.MOD_ID + ".type." + SpellsRegistries.SWORD_OR_AXE_ENCHANTMENT_CATEGORY.name().toLowerCase(), "axes, swords");
        
        addEnchantment(SpellsRegistries.MAGIC_PROTECTION_ENCHANTMENT, "Magic Protection", "Reduces magic damage.");
        addEnchantment(SpellsRegistries.MANA_BLADE_ENCHANTMENT, "Mana Blade", "Consumes mana to increase damage.");
        addEnchantment(SpellsRegistries.MANA_SHIELD_ENCHANTMENT, "Mana Shield", "WIP"); //TODO mana shield ench description
        addEnchantment(SpellsRegistries.MAX_MANA_ENCHANTMENT, "Maximum Mana", "Increases your maximum mana while worn. More potent on chestplates and leggings than helmets and boots.");
        addEnchantment(SpellsRegistries.MANA_REGENERATION_ENCHANTMENT, "Mana Regeneration", "Increases your mana regeneration while worn. More potent on chestplates and leggings than helmets and boots.");
        
        addEffect(SpellsRegistries.INSTANT_MANA_EFFECT, "Instant Mana", "Replenishes mana; higher levels increase the effect potency.");
        addEffect(SpellsRegistries.MANA_BOMB_EFFECT, "Mana Bomb", "Burns mana; higher levels increase the effect potency.");
        addEffect(SpellsRegistries.REPLENISHMENT_EFFECT, "Replenishment", "Replenishes mana over time; higher levels make mana be replenished quicker.");
        addEffect(SpellsRegistries.LEAKING_MOB_EFFECT, "Leaking", "Burns mana over time; higher levels burn more mana.");
        addEffect(SpellsRegistries.MANA_BOOST_EFFECT, "Mana Boost", "Increases maximum mana; higher levels give more additional mana bottles.");
        addEffect(SpellsRegistries.EXTRA_MANA_EFFECT, "Extra Mana", "Adds burnable mana bottles (which can't be replenished); higher levels give more extra mana.");
        addEffect(SpellsRegistries.SILENCE_EFFECT, "Silence", "No spells can be used while this effect is active.");
        addEffect(SpellsRegistries.MAGIC_IMMUNE_EFFECT, "Magic Immune", "Makes you ignore any magic damage.");
        
        addPotion(SpellsRegistries.INSTANT_MANA, "Instant Mana");
        addPotion(SpellsRegistries.STRONG_INSTANT_MANA, "Instant Mana");
        
        addPotion(SpellsRegistries.MANA_BOMB, "Mana Bomb");
        addPotion(SpellsRegistries.STRONG_MANA_BOMB, "Mana Bomb");
        
        addPotion(SpellsRegistries.REPLENISHMENT, "Replenishment");
        addPotion(SpellsRegistries.LONG_REPLENISHMENT, "Replenishment");
        addPotion(SpellsRegistries.STRONG_REPLENISHMENT, "Replenishment");
        
        addPotion(SpellsRegistries.LEAKING, "Leaking");
        addPotion(SpellsRegistries.LONG_LEAKING, "Leaking");
        addPotion(SpellsRegistries.STRONG_LEAKING, "Leaking");
        
        add(SpellKeyBindings.CATEGORY, "Spells & Shields");
        for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
        {
            add(SpellKeyBindings.key(i), "Spell Slot " + (i + 1));
        }
        
        addRequirement(SpellsRegistries.BOOKSHELVES_REQUIREMENT, "%s/%s Bookshelves");
        addRequirement(SpellsRegistries.ADVANCEMENT_REQUIREMENT, "Advancement: %s");
        addRequirement(SpellsRegistries.ADVANCEMENT_REQUIREMENT, AdvancementRequirement.ERROR_SUFFIX, "Unknown Advancement (config error): %s");
        addRequirement(SpellsRegistries.ITEM_REQUIREMENT, "%s (Not Consumed)");
        addRequirement(SpellsRegistries.ITEM_REQUIREMENT, ItemRequirement.CONSUMED_SUFFIX, "%s (Consumed)");
        addRequirement(SpellsRegistries.ITEM_REQUIREMENT, ItemRequirement.MULTIPLE_SUFFIX, "%sx %s (Not Consumed)");
        addRequirement(SpellsRegistries.ITEM_REQUIREMENT, ItemRequirement.MULTIPLE_CONSUMED_SUFFIX, "%sx %s (Consumed)");
        
        add(SpellTrees.KEY_NETHER, "Nether");
        add(SpellTrees.KEY_OCEAN, "Ocean");
        add(SpellTrees.KEY_MINING, "Mining");
        add(SpellTrees.KEY_MOVEMENT, "Movement");
        add(SpellTrees.KEY_END, "End");
        
        add(SpellProgressionMenu.TITLE.getString(), "Spell Progression");
        
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_SINGLE, "%s has been learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_MULTIPLE, "%s has been learned by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_ALL_SINGLE, "%s spells have been learned by %s");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_ALL_SINGLE_FAILED, "%s already learned all spells");
        add(SpellCommand.SPELLS_PROGRESSION_LEARN_ALL_MULTIPLE, "All %s habe been learned by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_SINGLE, "%s has been forgotten by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_MULTIPLE, "%s has been forgotten by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_ALL_SINGLE, "%s spells have been forgotten by %s");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_ALL_SINGLE_FAILED, "%s has never learned a single spell");
        add(SpellCommand.SPELLS_PROGRESSION_FORGET_ALL_MULTIPLE, "All %s spells have been forgotten by %s players");
        add(SpellCommand.SPELLS_PROGRESSION_RESET_SINGLE, "Cleared all learned spells of %s");
        add(SpellCommand.SPELLS_PROGRESSION_RESET_MULTIPLE, "Cleared all learned spells of %s players");
        add(SpellCommand.SPELLS_SLOT_REMOVE_SINGLE, "Cleared active spell in slot %s of %s");
        add(SpellCommand.SPELLS_SLOT_REMOVE_MULTIPLE, "Cleared active spell in slot %s of %s players");
        add(SpellCommand.SPELLS_SLOT_SET_SINGLE, "Set active spell in slot %s of %s to %s");
        add(SpellCommand.SPELLS_SLOT_SET_MULTIPLE, "Set active spell in slot %s of %s players to %s");
        add(SpellCommand.SPELLS_SLOT_CLEAR_SINGLE, "Cleared active spells of %s");
        add(SpellCommand.SPELLS_SLOT_CLEAR_MULTIPLE, "Cleared active spells of %s players");
        
        add(SpellProgressionScreen.KEY_LEARN, "Learn");
        add(SpellProgressionScreen.KEY_EQUIP, "Equip");
        add(SpellProgressionScreen.KEY_UNAVAILABLE, "Unavailable");
        add(SpellProgressionScreen.KEY_CHOOSE_SLOT, "Choose a Slot");
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
