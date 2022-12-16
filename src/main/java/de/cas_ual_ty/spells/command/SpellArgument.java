package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cas_ual_ty.spells.NewSpells;
import de.cas_ual_ty.spells.spell.NewSpell;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpellArgument implements ArgumentType<NewSpell>
{
    public static final SimpleCommandExceptionType UNKNOWN_SPELL = new SimpleCommandExceptionType(Component.translatable("argument.spell.id.invalid"));
    
    private final HolderLookup<NewSpell> spells;
    
    public SpellArgument(CommandBuildContext cbx)
    {
        spells = cbx.holderLookup(NewSpells.SPELLS_REGISTRY_KEY);
    }
    
    public static SpellArgument spell(CommandBuildContext cbx)
    {
        return new SpellArgument(cbx);
    }
    
    @Override
    public NewSpell parse(StringReader reader) throws CommandSyntaxException
    {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        Optional<Holder<NewSpell>> spell = spells.get(ResourceKey.create(NewSpells.SPELLS_REGISTRY_KEY, resourceLocation));
        
        return spell.orElseThrow(UNKNOWN_SPELL::create).get();
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        String s = builder.getRemaining();
        
        spells.listElements().forEach(resourceKey ->
        {
            String spellStr = resourceKey.location().toString();
            
            if(spellStr.startsWith(s))
            {
                builder.suggest(spellStr);
            }
        });
        
        return builder.buildFuture();
    }
    
    public static NewSpell getSpell(CommandContext<CommandSourceStack> context, String argument)
    {
        return context.getArgument(argument, NewSpell.class);
    }
}
