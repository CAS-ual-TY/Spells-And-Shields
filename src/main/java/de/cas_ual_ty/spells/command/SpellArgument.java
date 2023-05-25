package de.cas_ual_ty.spells.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class SpellArgument
{
    public static final SimpleCommandExceptionType UNKNOWN_SPELL = new SimpleCommandExceptionType(SpellsDowngrade.translatable("argument.spell.id.invalid"));
    
    public static ResourceKeyArgument<Spell> spell()
    {
        return ResourceKeyArgument.key(Spells.REGISTRY_KEY);
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
    
    public static Spell getSpell(CommandContext<CommandSourceStack> css, String pName) throws CommandSyntaxException
    {
        ResourceKey<Spell> resourceKey = getRegistryType(css, pName, Spells.REGISTRY_KEY, UNKNOWN_SPELL);
        return Spells.getRegistry(css.getSource().registryAccess()).getHolder(resourceKey).orElseThrow(UNKNOWN_SPELL::create).value();
    }
}
