package de.cas_ual_ty.spells.util;

import com.google.common.collect.ImmutableList;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SpellsUtil
{
    public static final Optional<ItemStack> EMPTY_ITEMSTACK_OPTIONAL = Optional.of(ItemStack.EMPTY);
    public static final Optional<List<ItemStack>> EMPTY_ITEMSTACK_LIST_OPTIONAL = Optional.of(ImmutableList.of(ItemStack.EMPTY));
    
    public static final Field ACCESS_FIELD;
    public static final String ACCESS_FIELD_NAME = "f_39450_";
    
    static
    {
        Field access_field = null;
        
        try
        {
            // !mojf access
            // send this to the forge bot, "access" being the field name at the time of writing this
            access_field = ObfuscationReflectionHelper.findField(EnchantmentMenu.class, "f_39450_");
        }
        catch(ObfuscationReflectionHelper.UnableToFindFieldException e)
        {
            SpellsAndShields.LOGGER.warn("Field " + ACCESS_FIELD_NAME + " (EnchantmentMenu#access) could not be found!");
            e.printStackTrace();
        }
        
        ACCESS_FIELD = access_field;
    }
    
    public static ContainerLevelAccess getAccess(Player player, EnchantmentMenu menu)
    {
        if(ACCESS_FIELD != null)
        {
            try
            {
                ContainerLevelAccess access = (ContainerLevelAccess) ACCESS_FIELD.get(menu);
                
                if(access != null)
                {
                    return access;
                }
            }
            catch(IllegalAccessException e)
            {
                SpellsAndShields.LOGGER.warn("Field " + ACCESS_FIELD_NAME + " (EnchantmentMenu#access) could not be accessed!");
                e.printStackTrace();
            }
        }
        
        return ContainerLevelAccess.create(player.level, player.blockPosition());
    }
    
    @Nullable
    public static HitResult rayTrace(Level level, Entity source, double maxDist, Predicate<Entity> filter, float bbInflation, ClipContext.Block block, ClipContext.Fluid fluid)
    {
        BlockHitResult blockHitResult = rayTraceBlock(level, source, maxDist, block, fluid);
        EntityHitResult entityHitResult = rayTraceEntity(level, source, maxDist, filter, bbInflation);
        
        if(entityHitResult == null && blockHitResult.getType() == HitResult.Type.MISS)
        {
            return null;
        }
        else if(entityHitResult == null)
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
        Vec3 direction = source.getViewVector(1.0F).normalize();
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
        Vec3 direction = source.getViewVector(1.0F).normalize();
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
    
    public static ISpell getSpell(ResourceLocation key)
    {
        return SpellsRegistries.SPELLS_REGISTRY.get().getValue(key);
    }
    
    public static void forEachSpell(Consumer<ISpell> consumer)
    {
        SpellsRegistries.SPELLS_REGISTRY.get().forEach(consumer);
    }
    
    public static int getSpellsAmount()
    {
        return SpellsRegistries.SPELLS_REGISTRY.get().getValues().size();
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
        return generateUUIDFromName("slot_" + slot, attribute.getRegistryName().toString());
    }
    
    public static UUID generateUUIDForClassAttribute(Attribute attribute, String className)
    {
        return generateUUIDFromName("class_" + className, attribute.getRegistryName().toString());
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
        BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), from)), Ingredient.of(ingredient), PotionUtils.setPotion(new ItemStack(Items.POTION), to));
        BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), from)), Ingredient.of(ingredient), PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), to));
        BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), from)), Ingredient.of(ingredient), PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), to));
    }
    
    public static void addPotionVariants(@Nullable Potion potion)
    {
        if(potion != null)
        {
            BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), potion)), Ingredient.of(Items.GUNPOWDER), PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
            BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion)), Ingredient.of(Items.DRAGON_BREATH), PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));
        }
    }
}