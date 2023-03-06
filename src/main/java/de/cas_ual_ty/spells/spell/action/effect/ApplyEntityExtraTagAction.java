package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ExtraTagHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;

public class ApplyEntityExtraTagAction extends SpellAction
{
    public static Codec<ApplyEntityExtraTagAction> makeCodec(SpellActionType<ApplyEntityExtraTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entity")).forGetter(ApplyEntityExtraTagAction::getEntity),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf("tag").forGetter(ApplyEntityExtraTagAction::getTag)
        ).apply(instance, (activation, entity, tag) -> new ApplyEntityExtraTagAction(type, activation, entity, tag)));
    }
    
    public static ApplyEntityExtraTagAction make(String activation, String entity, DynamicCtxVar<CompoundTag> tag)
    {
        return new ApplyEntityExtraTagAction(SpellActionTypes.APPLY_ENTITY_EXTRA_TAG.get(), activation, entity, tag);
    }
    
    protected String entity;
    protected DynamicCtxVar<CompoundTag> tag;
    
    public ApplyEntityExtraTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyEntityExtraTagAction(SpellActionType<?> type, String activation, String entity, DynamicCtxVar<CompoundTag> tag)
    {
        super(type, activation);
        this.entity = entity;
        this.tag = tag;
    }
    
    public String getEntity()
    {
        return entity;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.getOrCreateTargetGroup(entity).getSingleTarget(target ->
        {
            if(entity != null)
            {
                TargetTypes.ENTITY.get().ifType(target, entityTarget ->
                {
                    this.tag.getValue(ctx).ifPresent(tag0 ->
                    {
                        ExtraTagHolder.getHolder(entityTarget.getEntity()).ifPresent(extraTagHolder ->
                        {
                            extraTagHolder.applyExtraTag(tag0);
                        });
                    });
                });
            }
        });
    }
}
