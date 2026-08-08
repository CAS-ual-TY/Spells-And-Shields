package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsUtil;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.concurrent.atomic.AtomicBoolean;

public class HasAttributeModifierAction extends GetTargetAttributeAction<LivingEntityTarget>
{
    public static Codec<HasAttributeModifierAction> makeCodec(SpellActionType<HasAttributeModifierAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("attribute")).forGetter(HasAttributeModifierAction::getAttribute),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("id")).forGetter(HasAttributeModifierAction::getId),
                Codec.STRING.fieldOf(ParamNames.var("is_present")).forGetter(HasAttributeModifierAction::getIsPresent)
        ).apply(instance, (activation, multiTargets, attribute, id, isPresent) -> new HasAttributeModifierAction(type, activation, multiTargets, attribute, id, isPresent)));
    }

    public static HasAttributeModifierAction make(Object activation, Object multiTargets, DynamicCtxVar<String> attribute, DynamicCtxVar<String> id, String isPresent)
    {
        return new HasAttributeModifierAction(SpellActionTypes.HAS_ATTRIBUTE_MODIFIER.get(), activation.toString(), multiTargets.toString(), attribute, id, isPresent);
    }

    protected DynamicCtxVar<String> attribute;
    protected DynamicCtxVar<String> id;
    protected String isPresent;

    public HasAttributeModifierAction(SpellActionType<?> type)
    {
        super(type);
    }

    public HasAttributeModifierAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> attribute, DynamicCtxVar<String> id, String isPresent)
    {
        super(type, activation, multiTargets);
        this.attribute = attribute;
        this.id = id;
        this.isPresent = isPresent;

        if(!isPresent.isEmpty())
        {
            AtomicBoolean ret = new AtomicBoolean(false);

            addVariableAttribute((ctx, e) ->
            {
                SpellsUtil.stringToHolder(ctx, attribute, BuiltInRegistries.ATTRIBUTE).ifPresent(attribute1 ->
                {
                    AttributeInstance a = e.getLivingEntity().getAttribute(attribute1);

                    if(a != null)
                    {
                        id.getValue(ctx).ifPresent(id1 ->
                        {
                            ret.set(a.hasModifier(ResourceLocation.parse(id1)));
                        });
                    }
                });
                return ret.get();
            }, CtxVarTypes.BOOLEAN.get(), isPresent);
        }
    }

    public DynamicCtxVar<String> getAttribute()
    {
        return attribute;
    }

    public DynamicCtxVar<String> getId()
    {
        return id;
    }

    public String getIsPresent()
    {
        return isPresent;
    }

    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
}
