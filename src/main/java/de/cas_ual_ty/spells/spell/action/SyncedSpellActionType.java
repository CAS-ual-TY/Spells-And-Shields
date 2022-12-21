package de.cas_ual_ty.spells.spell.action;

import com.mojang.serialization.Codec;
import de.cas_ual_ty.spells.spell.action.client.IClientAction;

import java.util.function.Function;
import java.util.function.Supplier;

public class SyncedSpellActionType<A extends SpellAction, C extends IClientAction> extends SpellActionType<A>
{
    protected Supplier<C> clientAction;
    
    public SyncedSpellActionType(Function<SpellActionType<A>, A> constructor, Function<SpellActionType<A>, Codec<A>> codec, Supplier<C> clientAction)
    {
        super(constructor, codec);
        this.clientAction = clientAction;
    }
    
    public C makeClientInstance()
    {
        return clientAction.get();
    }
}
