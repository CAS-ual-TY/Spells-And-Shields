package de.cas_ual_ty.spells.registers;

import com.mojang.serialization.Codec;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class CtxVarTypes
{
    public static Supplier<IForgeRegistry<CtxVarType<?>>> REGISTRY;
    private static final DeferredRegister<CtxVarType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "context_variables"), MOD_ID);
    
    public static final RegistryObject<CtxVarType<Integer>> INT = DEFERRED_REGISTER.register("int", () -> new CtxVarType<>(Integer::intValue, Codec.INT));
    public static final RegistryObject<CtxVarType<Double>> DOUBLE = DEFERRED_REGISTER.register("double", () -> new CtxVarType<>(Double::doubleValue, Codec.DOUBLE));
    public static final RegistryObject<CtxVarType<Vec3>> VEC3 = DEFERRED_REGISTER.register("vec3", () -> new CtxVarType<>(vec3 -> vec3, Vec3.CODEC));
    public static final RegistryObject<CtxVarType<BlockPos>> BLOCK_POS = DEFERRED_REGISTER.register("block_pos", () -> new CtxVarType<>(blockPos -> blockPos, BlockPos.CODEC));
    public static final RegistryObject<CtxVarType<Boolean>> BOOLEAN = DEFERRED_REGISTER.register("boolean", () -> new CtxVarType<>(Boolean::booleanValue, Codec.BOOL));
    public static final RegistryObject<CtxVarType<CompoundTag>> TAG = DEFERRED_REGISTER.register("tag", () -> new CtxVarType<>(CompoundTag::copy, CompoundTag.CODEC));
    public static final RegistryObject<CtxVarType<String>> STRING = DEFERRED_REGISTER.register("string", () -> new CtxVarType<>(s -> s, Codec.STRING));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CtxVarTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CtxVarTypes::setup);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<CtxVarType<?>>().setMaxID(256).setName(new ResourceLocation(MOD_ID, "context_variables")));
    }
    
    private static void setup(FMLCommonSetupEvent event)
    {
        DOUBLE.get().addConverter(STRING.get(), double0 -> double0.toString());
        INT.get().addConverter(DOUBLE.get(), integer -> integer.doubleValue());
        INT.get().addConverter(STRING.get(), integer -> integer.toString());
        VEC3.get().addConverter(BLOCK_POS.get(), vec3 -> new BlockPos(vec3));
        BLOCK_POS.get().addConverter(VEC3.get(), blockPos -> Vec3.atCenterOf(blockPos));
        BOOLEAN.get().addConverter(STRING.get(), bool -> bool.toString());
    }
}
