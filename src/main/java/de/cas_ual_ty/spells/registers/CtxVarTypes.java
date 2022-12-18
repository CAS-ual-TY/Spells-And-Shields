package de.cas_ual_ty.spells.registers;

import com.mojang.serialization.Codec;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class CtxVarTypes
{
    public static Supplier<IForgeRegistry<CtxVarType<?>>> REGISTRY;
    private static final DeferredRegister<CtxVarType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "context_variables"), MOD_ID);
    
    public static final RegistryObject<CtxVarType<Integer>> INT = DEFERRED_REGISTER.register("int", () -> new CtxVarType<>(Codec.INT));
    public static final RegistryObject<CtxVarType<Double>> DOUBLE = DEFERRED_REGISTER.register("double", () -> new CtxVarType<>(Codec.DOUBLE));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CtxVarTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<CtxVarType<?>>().setMaxID(256).setName(new ResourceLocation(MOD_ID, "context_variables")));
    }
}
