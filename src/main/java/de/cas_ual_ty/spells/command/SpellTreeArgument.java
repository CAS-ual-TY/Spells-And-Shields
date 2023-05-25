package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

/**
 * Changed significantly for the 1.19.2 -> 1.18.2 downgrade
 */
public class SpellTreeArgument
{
    public static final SimpleCommandExceptionType UNKNOWN_SPELL_TREE = new SimpleCommandExceptionType(SpellsDowngrade.translatable("argument.spell_tree.id.invalid"));
    
    public static ResourceKeyArgument<SpellTree> spellTree()
    {
        return ResourceKeyArgument.key(SpellTrees.REGISTRY_KEY);
    }
    
    private static <T> ResourceKey<T> getRegistryType(CommandContext<CommandSourceStack> css, String name, ResourceKey<Registry<T>> registryKey, SimpleCommandExceptionType exception) throws CommandSyntaxException
    {
        ResourceKey<?> resourceKey = css.getArgument(name, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourceKey.cast(registryKey);
        return optional.orElseThrow(exception::create);
    }
    
    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> pContext, ResourceKey<? extends Registry<T>> pRegistryKey)
    {
        return pContext.getSource().registryAccess().registryOrThrow(pRegistryKey);
    }
    
    public static SpellTree getSpellTree(CommandContext<CommandSourceStack> css, String pName) throws CommandSyntaxException
    {
        ResourceKey<SpellTree> resourceKey = getRegistryType(css, pName, SpellTrees.REGISTRY_KEY, UNKNOWN_SPELL_TREE);
        return SpellTrees.getRegistry(css.getSource().registryAccess()).getHolder(resourceKey).orElseThrow(UNKNOWN_SPELL_TREE::create).value();
    }
}
