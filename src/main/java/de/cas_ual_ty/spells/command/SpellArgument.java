package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cas_ual_ty.spells.SpellsUtil;
import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class SpellArgument implements ArgumentType<ISpell>
{
    public static final SimpleCommandExceptionType UNKNOWN_SPELL = new SimpleCommandExceptionType(new TranslatableComponent("argument.spell.id.invalid"));
    
    public static SpellArgument spell()
    {
        return new SpellArgument();
    }
    
    @Override
    public ISpell parse(StringReader reader) throws CommandSyntaxException
    {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        ISpell spell = SpellsUtil.getSpell(resourceLocation);
        
        if(spell == null)
        {
            throw UNKNOWN_SPELL.create();
        }
        
        return spell;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        String s = builder.getRemaining();
        
        SpellsUtil.forEachSpell(spell ->
        {
            String spellStr = spell.getRegistryName().toString();
            
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
