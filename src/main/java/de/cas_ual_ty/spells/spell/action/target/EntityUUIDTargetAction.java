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
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class EntityUUIDTargetAction extends DstTargetAction
{
    public static Codec<EntityUUIDTargetAction> makeCodec(SpellActionType<EntityUUIDTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(EntityUUIDTargetAction::getUuid)
        ).apply(instance, (activation, dst, uuid) -> new EntityUUIDTargetAction(type, activation, dst, uuid)));
    }
    
    public static EntityUUIDTargetAction make(Object activation, Object dst, DynamicCtxVar<String> uuid)
    {
        return new EntityUUIDTargetAction(SpellActionTypes.ENTITY_UUID_TARGET.get(), activation.toString(), dst.toString(), uuid);
    }
    
    protected DynamicCtxVar<String> uuid;
    
    public EntityUUIDTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public EntityUUIDTargetAction(SpellActionType<?> type, String activation, String dst, DynamicCtxVar<String> uuid)
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
        if(ctx.level instanceof ServerLevel level)
        {
            uuid.getValue(ctx).map(SpellsUtil::uuidFromString).ifPresent(uuid ->
            {
                Entity e = level.getEntity(uuid);
                if(e != null && e.level() == ctx.level)
                {
                    destination.addTargets(Target.of(e));
                }
            });
        }
    }
}
