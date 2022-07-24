package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class SpellArgument implements ArgumentType<ISpell>
{
    public static final SimpleCommandExceptionType UNKNOWN_SPELL = new SimpleCommandExceptionType(Component.translatable("argument.spell.id.invalid"));
    
    //private final HolderLookup<ISpell> spells;
    
    public SpellArgument(CommandBuildContext cbx)
    {
        //TODO use this implementation if it gets fixed / you find the fix
        //spells = cbx.holderLookup(SpellsRegistries.SPELLS_REGISTRY.get().getRegistryKey());
    }
    
    public static SpellArgument spell(CommandBuildContext cbx)
    {
        return new SpellArgument(cbx);
    }
    
    @Override
    public ISpell parse(StringReader reader) throws CommandSyntaxException
    {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        //Optional<Holder<ISpell>> spell = spells.get(ResourceKey.create(SpellsRegistries.SPELLS_REGISTRY.get().getRegistryKey(), resourceLocation));
        ISpell spell = SpellsUtil.getSpell(resourceLocation);
        
        //if(spell.isEmpty())
        if(spell == null)
        {
            throw UNKNOWN_SPELL.create();
        }
        
        //return spell.get().get();
        return spell;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        String s = builder.getRemaining();
        
        SpellsUtil.forEachSpell((key, spell) ->
        {
            String spellStr = key.toString();
            
            if(spellStr.startsWith(s))
            {
                builder.suggest(spellStr);
            }
        });
        
        return builder.buildFuture();
    }
    
    public static ISpell getSpell(CommandContext<CommandSourceStack> context, String argument)
    {
        return context.getArgument(argument, ISpell.class);
    }
}
