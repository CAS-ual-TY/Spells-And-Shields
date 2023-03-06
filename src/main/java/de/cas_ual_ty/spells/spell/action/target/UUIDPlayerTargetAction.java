package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.DstTargetAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class UUIDPlayerTargetAction extends DstTargetAction
{
    public static Codec<UUIDPlayerTargetAction> makeCodec(SpellActionType<UUIDPlayerTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(UUIDPlayerTargetAction::getUuid)
        ).apply(instance, (activation, dst, uuid) -> new UUIDPlayerTargetAction(type, activation, dst, uuid)));
    }
    
    public static UUIDPlayerTargetAction make(String activation, String dst, DynamicCtxVar<String> uuid)
    {
        return new UUIDPlayerTargetAction(SpellActionTypes.UUID_PLAYER_TARGET.get(), activation, dst, uuid);
    }
    
    protected DynamicCtxVar<String> uuid;
    
    public UUIDPlayerTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public UUIDPlayerTargetAction(SpellActionType<?> type, String activation, String dst, DynamicCtxVar<String> uuid)
    {
        super(type, activation, dst);
        this.uuid = uuid;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup destination)
    {
        uuid.getValue(ctx).ifPresent(uuidS ->
        {
            try
            {
                UUID uuid = UUID.fromString(uuidS);
                Player e = ctx.level.getPlayerByUUID(uuid);
                if(e != null && e.level == ctx.level)
                {
                    destination.addTargets(Target.of(e));
                }
            }
            catch(IllegalArgumentException ignored)
            {
            
            }
        });
    }
}
