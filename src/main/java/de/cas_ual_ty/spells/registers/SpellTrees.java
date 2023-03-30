package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.requirement.AdvancementRequirement;
import de.cas_ual_ty.spells.requirement.BookshelvesRequirement;
import de.cas_ual_ty.spells.requirement.ItemRequirement;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Function;
import java.util.function.Supplier;

public class SpellTrees
{
    private static Supplier<IForgeRegistry<SpellTree>> REGISTRY;
    public static ResourceKey<Registry<SpellTree>> REGISTRY_KEY;
    
    public static Registry<SpellTree> getRegistry(LevelAccessor level)
    {
        return level.registryAccess().registryOrThrow(REGISTRY_KEY);
    }
    
    public static final String KEY_NETHER = "spell_tree." + SpellsAndShields.MOD_ID + ".nether";
    public static final String KEY_OCEAN = "spell_tree." + SpellsAndShields.MOD_ID + ".ocean";
    public static final String KEY_MINING = "spell_tree." + SpellsAndShields.MOD_ID + ".mining";
    public static final String KEY_MOVEMENT = "spell_tree." + SpellsAndShields.MOD_ID + ".movement";
    public static final String KEY_END = "spell_tree." + SpellsAndShields.MOD_ID + ".end";
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellTrees::newRegistry);
        MinecraftForge.EVENT_BUS.addListener(SpellTrees::levelLoad);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<SpellTree>().setMaxID(1024).dataPackRegistry(SpellsCodecs.SPELL_TREE_CONTENTS).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spell_trees"))
                .onCreate((registry, stage) -> REGISTRY_KEY = registry.getRegistryKey())
        );
    }
    
    private static void levelLoad(LevelEvent.Load event)
    {
        if(event.getLevel().isClientSide())
        {
            return;
        }
        
        Registry<SpellTree> registry = getRegistry(event.getLevel());
        registry.forEach(spellTree -> spellTree.assignNodeIds(registry.getKey(spellTree)));
    }
    
    public static SpellTree fireTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        int nodeId = 100;
        return SpellTree.builder(Component.translatable(KEY_NETHER), nodeId++, spellGetter.apply(Spells.FIRE_BALL), 15, bookshelves(28))
                .icon(spellGetter.apply(Spells.TOGGLE_FIRE_RESISTANCE))
                .add(nodeId++, spellGetter.apply(Spells.LAVA_WALKER), 20, bookshelves(19))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_FIRE_RESISTANCE), 30, bookshelves(30))
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.DRAIN_FLAME), 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.GHAST), 10, bookshelves(20), item(Items.GHAST_TEAR, 1, true))
                .add(nodeId++, spellGetter.apply(Spells.FLAMETHROWER), 20, bookshelves(24))
                .finish();
    }
    
    public static SpellTree waterTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        int nodeId = 100;
        return SpellTree.builder(Component.translatable(KEY_OCEAN), nodeId++, spellGetter.apply(Spells.TOGGLE_WATER_BREATHING), 10)
                .icon(spellGetter.apply(Spells.TOGGLE_DOLPHINS_GRACE))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_REGENERATION), 20, bookshelves(20))
                .add(nodeId++, spellGetter.apply(Spells.GROWTH), 20, bookshelves(20))
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.AQUA_AFFINITY), 20, bookshelves(20))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.WATER_LEAP), 5, bookshelves(10))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_DOLPHINS_GRACE), 30, bookshelves(30))
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.FROST_WALKER), 10, bookshelves(14))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.WATER_WHIP), 10, bookshelves(10))
                .add(nodeId++, spellGetter.apply(Spells.POTION_SHOT), 10, bookshelves(10))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.LIGHTNING_STRIKE), 25, bookshelves(24), advancement("adventure/lightning_rod_with_villager_no_fire"))
                .leaf()
                .finish();
    }
    
    public static SpellTree earthTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        int nodeId = 100;
        return SpellTree.builder(Component.translatable(KEY_MINING), nodeId++, spellGetter.apply(Spells.BLAST_SMELT), 5, bookshelves(8))
                .icon(spellGetter.apply(Spells.TOGGLE_HASTE))
                .add(nodeId++, spellGetter.apply(Spells.SILENCE_TARGET), 25, bookshelves(26))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_MAGIC_IMMUNE), 25, bookshelves(26))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.INSTANT_MINE), 15, bookshelves(18))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_HASTE), 25, bookshelves(24))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.SPIT_METAL), 10, bookshelves(12))
                .finish();
    }
    
    public static SpellTree airTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        int nodeId = 100;
        return SpellTree.builder(Component.translatable(KEY_MOVEMENT), nodeId++, spellGetter.apply(Spells.TOGGLE_JUMP_BOOST), 15, bookshelves(12))
                .icon(spellGetter.apply(Spells.TOGGLE_JUMP_BOOST))
                .add(nodeId++, spellGetter.apply(Spells.LEAP), 10, bookshelves(14))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_SPEED), 20, bookshelves(20))
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.JUMP), 14, bookshelves(14))
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.MANA_SOLES), 15, bookshelves(12))
                .add(nodeId++, spellGetter.apply(Spells.TOGGLE_SLOW_FALLING), 15, bookshelves(16))
                .leaf()
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.BLOW_ARROW), 10, bookshelves(16))
                .add(nodeId++, spellGetter.apply(Spells.PRESSURIZE), 20, bookshelves(18))
                .leaf()
                .leaf()
                .finish();
    }
    
    public static SpellTree enderTree(Function<ResourceLocation, Holder<Spell>> spellGetter)
    {
        int nodeId = 100;
        return SpellTree.builder(Component.translatable(KEY_END), nodeId++, spellGetter.apply(Spells.RANDOM_TELEPORT), 20, bookshelves(28))
                .icon(spellGetter.apply(Spells.TELEPORT))
                .requirement(advancement("end/root"))
                .add(nodeId++, spellGetter.apply(Spells.FORCED_TELEPORT), 30, bookshelves(28))
                .leaf()
                .add(nodeId++, spellGetter.apply(Spells.TELEPORT), 30, bookshelves(28), advancement("end/respawn_dragon"))
                .add(nodeId++, spellGetter.apply(Spells.ENDER_ARMY), 50, bookshelves(30), item(Items.DRAGON_EGG, 1, false))
                .finish();
    }
    
    public static Requirement bookshelves(int bookshelves)
    {
        return new BookshelvesRequirement(RequirementTypes.BOOKSHELVES.get(), bookshelves);
    }
    
    public static Requirement advancement(String advancementRL)
    {
        return new AdvancementRequirement(RequirementTypes.ADVANCEMENT.get(), new ResourceLocation(advancementRL));
    }
    
    public static Requirement item(Item item, int count, boolean consume)
    {
        return new ItemRequirement(RequirementTypes.ITEM.get(), new ItemStack(item, count), consume);
    }
}
