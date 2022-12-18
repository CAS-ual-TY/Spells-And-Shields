package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.effect.DamageAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellActionTypes
{
    public static Supplier<IForgeRegistry<SpellActionType<?>>> REGISTRY;
    private static final DeferredRegister<SpellActionType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "spell_actions"), MOD_ID);
    
    public static final RegistryObject<SpellActionType<DamageAction>> DAMAGE = DEFERRED_REGISTER.register("damage", () -> new SpellActionType<>(DamageAction::new, DamageAction::makeCodec));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellActionTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<SpellActionType<?>>().setMaxID(1024).setName(new ResourceLocation(MOD_ID, "spell_actions")));
    }
}
