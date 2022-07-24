package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spell.base.MobEffectSpell;
import de.cas_ual_ty.spells.spell.base.MultiIngredientSpell;
import de.cas_ual_ty.spells.spelltree.SpellTrees;
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
    
    public static final String PASSIVE_STRING = "A passive effect.";
    
    @Override
    protected void addTranslations()
    {
        addAttribute(SpellsRegistries.MAX_MANA, "Max Mana");
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
        for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
        {
            add(SpellKeyBindings.key(i), "Spell Slot " + (i + 1));
        }
        
        addSpell(Spells.FIRE_BALL, "Fire Ball", "Shoot a fire ball forward.");
        addSpell(Spells.LEAP, "Leap", "Leap forward.");
        addSpell(Spells.SUMMON_ANIMAL, "Summon Animal", "Create life based on the item in your hand.");
        addSpell(Spells.BLOW_ARROW, "Blow Arrow", "Shoot a projectile from your hand without a bow.");
        addSpell(Spells.BLAST_SMELT, "Blast Smelt", "Works like an instant blast furnace on the item in your hand.");
        addSpell(Spells.HEALTH_BOOST, "Health Boost", "Increases your maximum health.");
        addSpell(Spells.MANA_BOOST, "Mana Boost", "Increases your maximum mana.");
        addSpell(Spells.SPEED, "Speed", PASSIVE_STRING);
        addSpell(Spells.JUMP_BOOST, "Jump Boost", PASSIVE_STRING);
        addSpell(Spells.DOLPHINS_GRACE, "Dolphin's Grace", PASSIVE_STRING);
        addSpell(Spells.WATER_LEAP, "Water Leap", "Leap forward like a dolphin (must be underwater).");
        addSpell(Spells.AQUA_AFFINITY, "Aqua Affinity", PASSIVE_STRING);
        addSpell(Spells.WATER_BREATHING, "Water Breathing", PASSIVE_STRING);
        addSpell(Spells.SLOW_FALLING, "Slow Falling", PASSIVE_STRING);
        addSpell(Spells.HASTE, "Haste", PASSIVE_STRING);
        addSpell(Spells.REGENERATION, "Regeneration", PASSIVE_STRING);
        addSpell(Spells.REPLENISHMENT, "Replenishment", PASSIVE_STRING);
        addSpell(Spells.WATER_WHIP, "Water Whip", "Shoots water out of the water bucket in your hand. The water returns and the bucket refills if you hold it on return.");
        addSpell(Spells.POTION_SHOT, "Potion Shot", "Shots the contents of the potion in your hand forward.");
        addSpell(Spells.FROST_WALKER, "Frost Walker", PASSIVE_STRING);
        addSpell(Spells.JUMP, "Jump", "High jump. Be aware of fall damage.");
        addSpell(Spells.MANA_SOLES, "Mana Soles", "Consumes mana to reduce or cancel fall damage.");
        addSpell(Spells.FIRE_CHARGE, "Fire Charge", "Shoot a fire charge forward, like a Ghast.");
        addSpell(Spells.PRESSURIZE, "Pressurize", "Knock back every entity around you and remove any fluid.");
        addSpell(Spells.INSTANT_MINE, "Instant Mine", "Breaks the block your are looking at using the tool in your hand.");
        addSpell(Spells.FIRE_RESISTANCE, "Fire Resistance", PASSIVE_STRING);
        addSpell(Spells.SPIT_METAL, "Spit Metal", "Spit a nugget that deals damage (from your hand).");
        
        add(SpellTrees.KEY_NETHER, "Nether");
        add(SpellTrees.KEY_OCEAN, "Ocean");
        add(SpellTrees.KEY_MINING, "Mining");
        add(SpellTrees.KEY_MOVEMENT, "Movement");
        
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
        add(MobEffectSpell.KEY_WHEN_APPLIED, "When Applied:");
        add(SpellProgressionScreen.KEY_LEARN, "Learn");
        add(SpellProgressionScreen.KEY_EQUIP, "Equip");
        add(SpellProgressionScreen.KEY_UNAVAILABLE, "Unavailable");
        add(SpellProgressionScreen.KEY_CHOOSE_SLOT, "Choose a Slot");
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
