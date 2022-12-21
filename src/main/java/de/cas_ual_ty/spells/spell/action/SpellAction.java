package de.cas_ual_ty.spells.spell.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.network.RunActionOnClientMessage;
import de.cas_ual_ty.spells.spell.action.client.IClientAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraftforge.network.PacketDistributor;

public abstract class SpellAction
{
    public static <T extends SpellAction> RecordCodecBuilder<T, SpellActionType<?>> makeTypeCodec()
    {
        return SpellsCodecs.SPELL_ACTION_TYPE.fieldOf("type").forGetter(SpellAction::getType);
    }
    
    public static <T extends SpellAction> RecordCodecBuilder<T, String> activationCodec()
    {
        return Codec.STRING.fieldOf("activation").forGetter(SpellAction::getActivation);
    }
    
    public final SpellActionType<?> type;
    
    protected String activation;
    
    public SpellAction(SpellActionType<?> type)
    {
        this.type = type;
    }
    
    public SpellAction(SpellActionType<?> type, String activation)
    {
        this(type);
        this.activation = activation;
    }
    
    public SpellActionType<?> getType()
    {
        return type;
    }
    
    public String getActivation()
    {
        return activation;
    }
    
    public void doAction(SpellContext ctx)
    {
        if(doActivate(ctx))
        {
            wasActivated(ctx);
        }
    }
    
    protected boolean doActivate(SpellContext ctx)
    {
        return activation.isEmpty() || ctx.isActivated(activation);
    }
    
    protected abstract void wasActivated(SpellContext ctx);
    
    public void sendClientAction(PacketDistributor.PacketTarget packetTarget, IClientAction clientAction)
    {
        if(getType() instanceof SyncedSpellActionType<?, ?> syncedType)
        {
            SpellsAndShields.CHANNEL.send(packetTarget, new RunActionOnClientMessage(syncedType, clientAction));
        }
    }
}
