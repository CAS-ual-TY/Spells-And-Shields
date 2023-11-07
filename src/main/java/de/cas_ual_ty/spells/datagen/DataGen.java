package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        event.getGenerator().addProvider(event.includeClient(), new LangGen(event.getGenerator(), "en_us"));
        event.getGenerator().addProvider(event.includeServer(), new DocsGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
        
        event.getGenerator().addProvider(
                event.includeServer(),
                (DataProvider.Factory<DatapackBuiltinEntriesProvider>) (PackOutput output) -> new DatapackBuiltinEntriesProvider(
                        output,
                        event.getLookupProvider(),
                        // The objects to generate
                        new RegistrySetBuilder()
                                .add(Spells.REGISTRY_KEY, context -> {
                                    new SpellsGen(SpellsAndShields.MOD_ID, context);
                                })
                                .add(SpellTrees.REGISTRY_KEY, context -> {
                                    new SpellTreesGen(SpellsAndShields.MOD_ID, context);
                                }),
                        // Generate dynamic registry objects for this mod
                        Set.of(SpellsAndShields.MOD_ID)
                )
        );
    }
}
