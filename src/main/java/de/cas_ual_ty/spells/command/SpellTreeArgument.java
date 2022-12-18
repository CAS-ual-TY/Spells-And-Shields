package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cas_ual_ty.spells.SpellTrees;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpellTreeArgument implements ArgumentType<SpellTree>
{
    public static final SimpleCommandExceptionType UNKNOWN_SPELL_TREE = new SimpleCommandExceptionType(Component.translatable("argument.spell_tree.id.invalid"));
    
    private final HolderLookup<SpellTree> spellTrees;
    
    public SpellTreeArgument(CommandBuildContext cbx)
    {
        spellTrees = cbx.holderLookup(SpellTrees.SPELL_TREES_REGISTRY_KEY);
    }
    
    public static SpellTreeArgument spellTree(CommandBuildContext cbx)
    {
        return new SpellTreeArgument(cbx);
    }
    
    @Override
    public SpellTree parse(StringReader reader) throws CommandSyntaxException
    {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        Optional<Holder<SpellTree>> spell = spellTrees.get(ResourceKey.create(SpellTrees.SPELL_TREES_REGISTRY_KEY, resourceLocation));
        
        return spell.orElseThrow(UNKNOWN_SPELL_TREE::create).get();
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        String s = builder.getRemaining();
        
        spellTrees.listElements().forEach(resourceKey ->
        {
            String spellStr = resourceKey.location().toString();
            
            if(spellStr.startsWith(s))
            {
                builder.suggest(spellStr);
            }
        });
        
        return builder.buildFuture();
    }
    
    public static SpellTree getSpellTree(CommandContext<CommandSourceStack> context, String argument)
    {
        return context.getArgument(argument, SpellTree.class);
    }
}
