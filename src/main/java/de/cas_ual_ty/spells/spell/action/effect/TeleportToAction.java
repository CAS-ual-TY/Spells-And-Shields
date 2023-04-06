package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.phys.Vec3;

public class TeleportToAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<TeleportToAction> makeCodec(SpellActionType<TeleportToAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                Codec.STRING.fieldOf(ParamNames.singleTarget("position")).forGetter(TeleportToAction::getPosition)
        ).apply(instance, (activation, multiTargets, position) -> new TeleportToAction(type, activation, multiTargets, position)));
    }
    
    public static TeleportToAction make(String activation, String multiTargets, String position)
    {
        return new TeleportToAction(SpellActionTypes.TELEPORT_TO.get(), activation, multiTargets, position);
    }
    
    protected String position;
    
    public TeleportToAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TeleportToAction(SpellActionType<?> type, String activation, String multiTargets, String position)
    {
        super(type, activation, multiTargets);
        this.position = position;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public String getPosition()
    {
        return position;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        ctx.getTargetGroup(position).getSingleTarget(t -> TargetTypes.POSITION.get().ifType(t, position ->
        {
            Vec3 pos = position.getPosition();
            target.getEntity().teleportTo(pos.x, pos.y, pos.z);
        }));
    }
}
