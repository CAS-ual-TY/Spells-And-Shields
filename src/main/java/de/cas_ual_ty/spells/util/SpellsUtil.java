package de.cas_ual_ty.spells.util;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.*;
import net.neoforged.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class SpellsUtil
{
    public static final Optional<ItemStack> EMPTY_ITEMSTACK_OPTIONAL = Optional.of(ItemStack.EMPTY);
    public static final Optional<List<ItemStack>> EMPTY_ITEMSTACK_LIST_OPTIONAL = Optional.of(ImmutableList.of(ItemStack.EMPTY));
    
    public static final Container EMPTY_CONTAINER = new SimpleContainer(0);
    
    public static final RandomSource RANDOM = RandomSource.create();
    
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
    
    public static void addPotionRecipes(Holder<Potion> base, Holder<Potion> p, @Nullable Holder<Potion> strongP, @Nullable Holder<Potion> longP, Item ingredient, @Nullable Holder<Potion> badP, @Nullable Holder<Potion> badStrongP, @Nullable Holder<Potion> badLongP, @Nullable Item badIngredient, PotionBrewing.Builder builder)
    {
        addPotionRecipe(ingredient, base, p, builder);

        if(badP != null && badIngredient != null)
        {
            addPotionRecipe(badIngredient, p, badP, builder);

            if(badStrongP != null)
            {
                addPotionRecipe(Items.GLOWSTONE_DUST, badP, badStrongP, builder);
            }

            if(badLongP != null)
            {
                addPotionRecipe(Items.REDSTONE, badP, badLongP, builder);
            }
        }

        if(strongP != null)
        {
            addPotionRecipe(Items.GLOWSTONE_DUST, p, strongP, builder);

            if(badStrongP != null && badIngredient != null)
            {
                addPotionRecipe(badIngredient, strongP, badStrongP, builder);
            }
        }

        if(longP != null)
        {
            addPotionRecipe(Items.REDSTONE, p, longP, builder);

            if(badLongP != null && badIngredient != null)
            {
                addPotionRecipe(badIngredient, longP, badLongP, builder);
            }
        }
    }

    public static void addPotionRecipe(Item ingredient, Holder<Potion> from, Holder<Potion> to, PotionBrewing.Builder builder)
    {
        builder.addMix(from, ingredient, to);
    }
    
    public static boolean isEnchantingTable(Block block)
    {
        return block != null && isEnchantingTable(BuiltInRegistries.BLOCK.getKey(block));
    }
    
    public static boolean isEnchantingTable(ResourceLocation key)
    {
        return isTrueEnchantingTable(key) || isAltEnchantingTable(key);
    }
    
    public static boolean isTrueEnchantingTable(Block block)
    {
        return block != null && isTrueEnchantingTable(BuiltInRegistries.BLOCK.getKey(block));
    }
    
    public static boolean isTrueEnchantingTable(ResourceLocation key)
    {
        return key != null && SpellsConfig.ENCHANTING_TABLE.get().stream().anyMatch(rl -> rl.equals(key.toString()));
    }
    
    public static boolean isAltEnchantingTable(Block block)
    {
        return block != null && isAltEnchantingTable(BuiltInRegistries.BLOCK.getKey(block));
    }
    
    public static boolean isAltEnchantingTable(ResourceLocation key)
    {
        return key != null && SpellsConfig.PROGRESSION_BLOCK.get().stream().anyMatch(rl -> rl.equals(key.toString()));
    }
    
    public static Level getClientLevel()
    {
        return FMLEnvironment.dist.isClient() ? de.cas_ual_ty.spells.client.SpellsClientUtil.getClientLevel() : null;
    }
    
    public static <E extends Enum<E>> Codec<E> namedEnumCodec(Function<String, E> stringToEnum, Function<E, String> enumToString)
    {
        return Codec.STRING.xmap(stringToEnum, enumToString);
    }
    
    public static <E extends Enum<E>> Codec<E> idEnumCodec(Function<Integer, E> idToEnum)
    {
        return Codec.INT.xmap(idToEnum, Enum::ordinal);
    }
    
    public static UUID generateUUIDFromName(String name)
    {
        // prefixing to make sure this does not clash in case another mod does the same
        return UUID.nameUUIDFromBytes((SpellsAndShields.MOD_ID + "_" + name).getBytes(StandardCharsets.UTF_8));
    }
    
    public static UUID uuidFromString(String s)
    {
        try
        {
            return UUID.fromString(s);
        }
        catch(IllegalArgumentException e)
        {
            return null;
        }
    }
    
    public static String operationToString(AttributeModifier.Operation op)
    {
        return switch(op)
                {
                    case ADD_VALUE -> "addition";
                    case ADD_MULTIPLIED_BASE -> "multiply_base";
                    case ADD_MULTIPLIED_TOTAL -> "multiply_total";
                    default -> null;
                };
    }

    public static AttributeModifier.Operation operationFromString(String s)
    {
        return switch(s)
                {
                    case "addition" -> AttributeModifier.Operation.ADD_VALUE;
                    case "multiply_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                    case "multiply_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                    default -> null;
                };
    }
    
    public static String blockToString(ClipContext.Block op)
    {
        return switch(op)
                {
                    case COLLIDER -> "collider";
                    case OUTLINE -> "outline";
                    case VISUAL -> "visual";
                    case FALLDAMAGE_RESETTING -> "falldamage_resetting";
                    default -> null;
                };
    }
    
    public static ClipContext.Block blockFromString(String s)
    {
        return switch(s)
                {
                    case "collider" -> ClipContext.Block.COLLIDER;
                    case "outline" -> ClipContext.Block.OUTLINE;
                    case "visual" -> ClipContext.Block.VISUAL;
                    case "falldamage_resetting" -> ClipContext.Block.FALLDAMAGE_RESETTING;
                    default -> null;
                };
    }
    
    public static String fluidToString(ClipContext.Fluid op)
    {
        return switch(op)
                {
                    case NONE -> "none";
                    case SOURCE_ONLY -> "source_only";
                    case ANY -> "any";
                    case WATER -> "water";
                    default -> null;
                };
    }
    
    public static ClipContext.Fluid fluidFromString(String s)
    {
        return switch(s)
                {
                    case "none" -> ClipContext.Fluid.NONE;
                    case "source_only" -> ClipContext.Fluid.SOURCE_ONLY;
                    case "any" -> ClipContext.Fluid.ANY;
                    case "water" -> ClipContext.Fluid.WATER;
                    default -> null;
                };
    }
    
    public static <T> DynamicCtxVar<String> objectToString(T object, Registry<T> registry)
    {
        return CtxVarTypes.STRING.get().immediate(registry.getKey(object).toString());
    }
    
    public static <T> Optional<T> stringToObject(SpellContext ctx, DynamicCtxVar<String> s, Registry<T> registry)
    {
        return s.getValue(ctx).map(id -> registry.get(ResourceLocation.parse(id)));
    }
    
    public static BlockState tagToState(Block block, CompoundTag tag)
    {
        BlockState blockState = block.defaultBlockState();
        for(Property<?> p : blockState.getProperties())
        {
            addPropertyFromTag(blockState, p, tag);
        }
        return blockState;
    }
    
    private static <X extends Comparable<X>> void addPropertyFromTag(BlockState blockState, Property<X> p, CompoundTag tag)
    {
        p.valueCodec().decode(NbtOps.INSTANCE, tag).result().ifPresent(pair ->
        {
            blockState.setValue(p, pair.getFirst().value());
        });
    }
    
    public static CompoundTag stateToTag(StateHolder<?, ?> stateHolder)
    {
        CompoundTag tag = new CompoundTag();
        for(Property<?> p : stateHolder.getProperties())
        {
            addPropertyToTag(stateHolder, tag, p);
        }
        return tag;
    }
    
    private static <X extends Comparable<X>> void addPropertyToTag(StateHolder<?, ?> stateHolder, CompoundTag tag, Property<X> p)
    {
        p.valueCodec().encodeStart(NbtOps.INSTANCE, p.value(stateHolder)).result().ifPresent(element ->
        {
            tag.put(p.getName(), element);
        });
    }
}
