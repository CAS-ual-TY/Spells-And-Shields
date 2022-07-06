package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spell.base.ISpell;
import de.cas_ual_ty.spells.spell.base.MultiIngredientSpell;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.function.Supplier;

public class LangGen extends LanguageProvider
{
    public LangGen(DataGenerator dataGen, String locale)
    {
        super(dataGen, SpellsAndShields.MOD_ID, locale);
    }
    
    @Override
    protected void addTranslations()
    {
        addAttribute(SpellsRegistries.MANA_BOOST, "Mana Boost");
        addEnchantment(SpellsRegistries.MAGIC_PROTECTION, "Magic Protection");
        addEnchantment(SpellsRegistries.MANA_BLADE, "Mana Blade");
        addEnchantment(SpellsRegistries.MANA_SHIELD, "Mana Shield");
        
        addEffect(SpellsRegistries.INSTANT_MANA_EFFECT, "Instant Mana");
        addEffect(SpellsRegistries.MANA_BOMB_EFFECT, "Mana Bomb");
        addEffect(SpellsRegistries.REPLENISHMENT_EFFECT, "Replenishment");
        addEffect(SpellsRegistries.LEAKING_MOB_EFFECT, "Leaking");
        addEffect(SpellsRegistries.MANA_BOOST_EFFECT, "Mana Boost");
        addEffect(SpellsRegistries.EXTRA_MANA_EFFECT, "Extra Mana");
        
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
        for(int i = 0; i < SpellsAndShields.SPELL_SLOTS; ++i)
        {
            add(SpellKeyBindings.key(i), "Spell Slot " + (i + 1));
        }
        
        addSpell(SpellsRegistries.FIRE_BALL, "Fire Ball", "Shoot a fire ball forward.");
        addSpell(SpellsRegistries.LEAP, "Leap", "Leap forward.");
        addSpell(SpellsRegistries.SUMMON_ANIMAL, "Summon Animal", "Create life based on the item in your hand.");
        addSpell(SpellsRegistries.POCKET_BOW, "Pocket Bow", "Shoot a projectile from your hand without a bow.");
        addSpell(SpellsRegistries.SMELT, "Smelt", "Works like an instant blast furnace on the item in your hand.");
        
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
        
        add(MultiIngredientSpell.KEY_REQUIRED_HAND, "Requirement (Hand):");
        add(MultiIngredientSpell.KEY_REQUIRED_INVENTORY, "Requirement (Inventory):");
    }
    
    public void addAttribute(Supplier<? extends Attribute> key, String name)
    {
        add(key.get().getDescriptionId(), name);
    }
    
    public void addPotion(Supplier<? extends Potion> key, String name)
    {
        add(PotionUtils.setPotion(new ItemStack(Items.POTION), key.get()), "Potion of " + name);
        add(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), key.get()), "Splash Potion of " + name);
        add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), key.get()), "Lingering Potion of " + name);
        add(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), key.get()), "Arrow of " + name);
    }
    
    public void addSpell(Supplier<? extends ISpell> key, String name, String desc)
    {
        add(key.get().getNameKey(), name);
        add(key.get().getDescKey(), desc);
    }
}