package de.cas_ual_ty.spells;

import de.cas_ual_ty.spells.requirement.AdvancementRequirement;
import de.cas_ual_ty.spells.requirement.BookshelvesRequirement;
import de.cas_ual_ty.spells.requirement.ItemRequirement;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.NewSpell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SpellTrees
{
    private static Supplier<IForgeRegistry<SpellTree>> SPELL_TREES_REGISTRY;
    public static ResourceKey<Registry<SpellTree>> SPELL_TREES_REGISTRY_KEY;
    
    public static Registry<SpellTree> getRegistry(Level level)
    {
        return level.registryAccess().registryOrThrow(SPELL_TREES_REGISTRY_KEY);
    }
    
    public static final String KEY_NETHER = "spell_tree." + SpellsAndShields.MOD_ID + ".nether";
    public static final String KEY_OCEAN = "spell_tree." + SpellsAndShields.MOD_ID + ".ocean";
    public static final String KEY_MINING = "spell_tree." + SpellsAndShields.MOD_ID + ".mining";
    public static final String KEY_MOVEMENT = "spell_tree." + SpellsAndShields.MOD_ID + ".movement";
    public static final String KEY_END = "spell_tree." + SpellsAndShields.MOD_ID + ".end";
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellTrees::newRegistry);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        SPELL_TREES_REGISTRY = event.create(new RegistryBuilder<SpellTree>().setMaxID(1024).dataPackRegistry(SpellsCodecs.SPELL_TREE_CONTENTS).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spell_trees")).onCreate((registry, stage) -> SPELL_TREES_REGISTRY_KEY = registry.getRegistryKey()));
    }
    
    public static SpellTree debugTree(Holder<NewSpell> debugSpell)
    {
        return SpellTree.builder(Component.literal("Debug Tree"), debugSpell, 15, bookshelves(28))
                .finish();
    }
    
    /*
    public static SpellTree fireTree()
    {
        return SpellTree.builder(Component.translatable(KEY_NETHER), Spells.FIRE_BALL, 15, bookshelves(28))
                .icon(Spells.TEMPORARY_FIRE_RESISTANCE.get())
                .add(Spells.LAVA_WALKER, 20, bookshelves(19))
                .add(Spells.TEMPORARY_FIRE_RESISTANCE, 30, bookshelves(30))
                .leaf()
                .add(Spells.DRAIN_FLAME, 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(Spells.GHAST, 10, bookshelves(20), item(Items.GHAST_TEAR, 1, true))
                .add(Spells.FLAMETHROWER, 20, bookshelves(24))
                .finish();
    }
    
    public static SpellTree waterTree()
    {
        return SpellTree.builder(Component.translatable(KEY_OCEAN), Spells.TEMPORARY_WATER_BREATHING, 10)
                .icon(Spells.TEMPORARY_DOLPHINS_GRACE.get())
                .add(Spells.TEMPORARY_REGENERATION, 20, bookshelves(20))
                .add(Spells.GROWTH, 20, bookshelves(20))
                .leaf()
                .add(Spells.AQUA_AFFINITY, 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(Spells.WATER_LEAP, 5, bookshelves(10))
                .add(Spells.TEMPORARY_DOLPHINS_GRACE, 30, bookshelves(30))
                .leaf()
                .add(Spells.FROST_WALKER, 10, bookshelves(14))
                .leaf()
                .leaf()
                .add(Spells.WATER_WHIP, 10, bookshelves(10))
                .add(Spells.POTION_SHOT, 10, bookshelves(10))
                .leaf()
                .leaf()
                .add(Spells.LIGHTNING_STRIKE, 25, bookshelves(24), advancement("adventure/lightning_rod_with_villager_no_fire"))
                .leaf()
                .finish();
    }
    
    public static SpellTree earthTree()
    {
        return SpellTree.builder(Component.translatable(KEY_MINING), Spells.BLAST_SMELT, 5, bookshelves(8))
                .icon(Spells.TEMPORARY_HASTE.get())
                .add(Spells.SILENCE_TARGET, 25, bookshelves(26))
                .add(Spells.TEMPORARY_MAGIC_IMMUNE, 25, bookshelves(26))
                .leaf()
                .leaf()
                .add(Spells.INSTANT_MINE, 15, bookshelves(18))
                .add(Spells.TEMPORARY_HASTE, 25, bookshelves(24))
                .leaf()
                .leaf()
                .add(Spells.SPIT_METAL, 10, bookshelves(12))
                .finish();
    }
    
    public static SpellTree airTree()
    {
        return SpellTree.builder(Component.translatable(KEY_MOVEMENT), Spells.TEMPORARY_JUMP_BOOST, 15, bookshelves(12))
                .icon(Spells.TEMPORARY_JUMP_BOOST.get())
                .add(Spells.LEAP, 10, bookshelves(14))
                .add(Spells.TEMPORARY_SPEED, 20, bookshelves(20))
                .leaf()
                .add(Spells.JUMP, 14, bookshelves(14))
                .leaf()
                .add(Spells.MANA_SOLES, 15, bookshelves(12))
                .add(Spells.TEMPORARY_SLOW_FALLING, 15, bookshelves(16))
                .leaf()
                .leaf()
                .add(Spells.BLOW_ARROW, 10, bookshelves(16))
                .add(Spells.PRESSURIZE, 20, bookshelves(18))
                .leaf()
                .leaf()
                .finish();
    }
    
    public static SpellTree enderTree()
    {
        return SpellTree.builder(Component.translatable(KEY_END), Spells.RANDOM_TELEPORT, 20, bookshelves(28))
                .icon(Spells.TELEPORT.get())
                .requirement(advancement("end/root"))
                .add(Spells.FORCED_TELEPORT.get(), 30, bookshelves(28))
                .leaf()
                .add(Spells.TELEPORT.get(), 30, bookshelves(28), advancement("end/respawn_dragon"))
                .add(Spells.ENDER_ARMY.get(), 50, bookshelves(30), item(Items.DRAGON_EGG, 1, false))
                .finish();
    }
    */
    
    public static Requirement bookshelves(int bookshelves)
    {
        return new BookshelvesRequirement(SpellsRegistries.BOOKSHELVES_REQUIREMENT.get(), bookshelves);
    }
    
    public static Requirement advancement(String advancementRL)
    {
        return new AdvancementRequirement(SpellsRegistries.ADVANCEMENT_REQUIREMENT.get(), new ResourceLocation(advancementRL));
    }
    
    public static Requirement item(Item item, int count, boolean consume)
    {
        return new ItemRequirement(SpellsRegistries.ITEM_REQUIREMENT.get(), new ItemStack(item, count), consume);
    }
}
