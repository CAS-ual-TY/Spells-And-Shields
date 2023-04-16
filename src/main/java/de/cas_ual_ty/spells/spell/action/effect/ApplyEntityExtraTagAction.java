package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ExtraTagHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;

public class ApplyEntityExtraTagAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<ApplyEntityExtraTagAction> makeCodec(SpellActionType<ApplyEntityExtraTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("tag")).forGetter(ApplyEntityExtraTagAction::getTag)
        ).apply(instance, (activation, targets, tag) -> new ApplyEntityExtraTagAction(type, activation, targets, tag)));
    }
    
    public static ApplyEntityExtraTagAction make(String activation, String targets, DynamicCtxVar<CompoundTag> tag)
    {
        return new ApplyEntityExtraTagAction(SpellActionTypes.APPLY_ENTITY_EXTRA_TAG.get(), activation, targets, tag);
    }
    
    protected DynamicCtxVar<CompoundTag> tag;
    
    public ApplyEntityExtraTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyEntityExtraTagAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<CompoundTag> tag)
    {
        super(type, activation, targets);
        this.tag = tag;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        this.tag.getValue(ctx).ifPresent(tag0 ->
        {
            ExtraTagHolder.getHolder(target.getEntity()).ifPresent(extraTagHolder ->
            {
                extraTagHolder.applyExtraTag(tag0);
            });
        });
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
}
