package de.cas_ual_ty.spells.util;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class SpellsUtil
{
    public static final Optional<ItemStack> EMPTY_ITEMSTACK_OPTIONAL = Optional.of(ItemStack.EMPTY);
    public static final Optional<List<ItemStack>> EMPTY_ITEMSTACK_LIST_OPTIONAL = Optional.of(ImmutableList.of(ItemStack.EMPTY));
    
    public static final Container EMPTY_CONTAINER = new SimpleContainer(0);
    
    public static HitResult rayTrace(Level level, Entity source, double maxDist, Predicate<Entity> filter, float bbInflation, ClipContext.Block block, ClipContext.Fluid fluid)
    {
        BlockHitResult blockHitResult = rayTraceBlock(level, source, maxDist, block, fluid);
        EntityHitResult entityHitResult = rayTraceEntity(level, source, maxDist, filter, bbInflation);
        
        if(entityHitResult == null)
        {
            return blockHitResult;
        }
        else if(blockHitResult.getType() == HitResult.Type.MISS)
        {
            return entityHitResult;
        }
        else
        {
            Vec3 position = source.position();
            
            double blockDist = blockHitResult.distanceTo(source);
            double entityDist = entityHitResult.distanceTo(source);
            
            if(entityDist <= blockDist)
            {
                return entityHitResult;
            }
            else
            {
                return blockHitResult;
            }
        }
    }
    
    public static BlockHitResult rayTraceBlock(Level level, Entity source, double maxDist, ClipContext.Block block, ClipContext.Fluid fluid)
    {
        final double maxDistSqr = maxDist * maxDist;
        
        Vec3 start = source.getEyePosition();
        Vec3 direction = source.getViewVector(1F).normalize();
        Vec3 end = start.add(direction.scale(maxDist));
        
        return level.clip(new ClipContext(start, end, block, fluid, source));
    }
    
    @Nullable
    public static EntityHitResult rayTraceEntity(Level level, Entity source, double maxDist, Predicate<Entity> filter, float bbInflation)
    {
        final double maxDistSqr = maxDist * maxDist;
        
        double currentDistance = Double.MAX_VALUE;
        Entity currentEntity = null;
        
        Vec3 start = source.getEyePosition();
        Vec3 direction = source.getViewVector(1F).normalize();
        Vec3 end = start.add(direction.scale(maxDist));
        
        for(Entity entity : level.getEntities(source, AABB.ofSize(start, maxDistSqr, maxDistSqr, maxDistSqr)))
        {
            if(!filter.test(entity))
            {
                continue;
            }
            
            AABB aabb = entity.getBoundingBox().inflate(bbInflation);
            Optional<Vec3> optional = aabb.clip(start, end);
            
            if(optional.isPresent())
            {
                double distance = start.distanceToSqr(optional.get());
                
                if(distance < currentDistance)
                {
                    currentEntity = entity;
                    currentDistance = distance;
                }
            }
        }
        
        return currentEntity == null ? null : new EntityHitResult(currentEntity);
    }
    
    public static Registry<Spell> getSpellRegistry(Level level)
    {
        return level.registryAccess().registryOrThrow(Spells.SPELLS_REGISTRY_KEY);
    }
    
    public static void forEachSpell(Registry<Spell> registry, BiConsumer<ResourceLocation, Spell> consumer)
    {
        registry.entrySet().forEach(e -> consumer.accept(e.getKey().location(), e.getValue()));
    }
    
    public static int getSpellsAmount(Registry<Spell> registry)
    {
        return registry.size();
    }
    
    public static UUID generateUUIDFromName(String purpose, String name)
    {
        // prefixing with very specific string to make sure this does not clash in case another mod does the same
        return UUID.nameUUIDFromBytes((SpellsAndShields.MOD_ID + "_" + purpose + "_" + name).getBytes(StandardCharsets.UTF_8));
    }
    
    public static UUID generateUUIDForTree(String name)
    {
        return generateUUIDFromName("tree", name);
    }
    
    public static UUID generateUUIDForSlotAttribute(Attribute attribute, int slot)
    {
        return generateUUIDFromName("slot_" + slot, ForgeRegistries.ATTRIBUTES.getKey(attribute).toString());
    }
    
    public static UUID generateUUIDForClassAttribute(Attribute attribute, String className)
    {
        return generateUUIDFromName("class_" + className, ForgeRegistries.ATTRIBUTES.getKey(attribute).toString());
    }
    
    public static void addPotionRecipes(Potion base, Potion p, @Nullable Potion strongP, @Nullable Potion longP, Item ingredient, @Nullable Potion badP, @Nullable Potion badStrongP, @Nullable Potion badLongP, @Nullable Item badIngredient)
    {
        addPotionRecipe(ingredient, base, p);
        
        if(badP != null && badIngredient != null)
        {
            addPotionRecipe(badIngredient, p, badP);
            
            if(badStrongP != null)
            {
                addPotionRecipe(Items.GLOWSTONE_DUST, badP, badStrongP);
            }
            
            if(badLongP != null)
            {
                addPotionRecipe(Items.REDSTONE, badP, badLongP);
            }
        }
        
        if(strongP != null)
        {
            addPotionRecipe(Items.GLOWSTONE_DUST, p, strongP);
            
            if(badStrongP != null && badIngredient != null)
            {
                addPotionRecipe(badIngredient, strongP, badStrongP);
            }
        }
        
        if(longP != null)
        {
            addPotionRecipe(Items.REDSTONE, p, longP);
            
            if(badLongP != null && badIngredient != null)
            {
                addPotionRecipe(badIngredient, longP, badLongP);
            }
        }
    }
    
    public static void addPotionRecipe(Item ingredient, Potion from, Potion to)
    {
        PotionBrewing.addMix(from, ingredient, to);
    }
    
    public static boolean isEnchantingTable(Block block)
    {
        return block != null && isEnchantingTable(ForgeRegistries.BLOCKS.getKey(block));
    }
    
    public static boolean isEnchantingTable(ResourceLocation key)
    {
        return key != null && SpellsConfig.ENCHANTING_TABLE.get().stream().anyMatch(rl -> rl.equals(key.toString()));
    }
    
    public static Level getClientLevel()
    {
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().level);
    }
}
