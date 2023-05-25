package de.cas_ual_ty.spells.datagen;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        // downgrade 1.19.2 -> 1.18.2: Do not data gen unless lang stuff changes! Files should stay perfectly mirrored
        throw new RuntimeException("Bad developer! Bad!");
        
        // also: I can not get this to work properly. Throws issues for spell trees.
        // data gen'ing only partially results in all the leftovers being removed
        // so do all or nothing
        
        //event.getGenerator().addProvider(new LangGen(event.getGenerator(), "en_us"));
        //event.getGenerator().addProvider(new SpellsGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
        //event.getGenerator().addProvider(new SpellTreesGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
        //event.getGenerator().addProvider(new DocsGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
    }
}
