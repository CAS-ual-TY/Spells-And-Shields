package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsUtil;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class GetAttributeModifierAction extends AffectSingleTypeAction<LivingEntityTarget>
{
    public static Codec<GetAttributeModifierAction> makeCodec(SpellActionType<GetAttributeModifierAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("attribute")).forGetter(GetAttributeModifierAction::getAttribute),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("id")).forGetter(GetAttributeModifierAction::getId),
                Codec.STRING.fieldOf(ParamNames.var("is_present")).forGetter(GetAttributeModifierAction::getIsPresent),
                Codec.STRING.fieldOf(ParamNames.var("amount")).forGetter(GetAttributeModifierAction::getAmount),
                Codec.STRING.fieldOf(ParamNames.var("operation")).forGetter(GetAttributeModifierAction::getOperation)
        ).apply(instance, (activation, source, attribute, id, position, amount, operation) -> new GetAttributeModifierAction(type, activation, source, attribute, id, position, amount, operation)));
    }

    public static GetAttributeModifierAction make(Object activation, Object source, DynamicCtxVar<String> attribute, DynamicCtxVar<String> id, String isPresent, String amount, String operation)
    {
        return new GetAttributeModifierAction(SpellActionTypes.GET_ATTRIBUTE_MODIFIER.get(), activation.toString(), source.toString(), attribute, id, isPresent, amount, operation);
    }

    protected DynamicCtxVar<String> attribute;
    protected DynamicCtxVar<String> id;
    protected String isPresent;
    protected String amount;
    protected String operation;

    public GetAttributeModifierAction(SpellActionType<?> type)
    {
        super(type);
    }

    public GetAttributeModifierAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<String> attribute, DynamicCtxVar<String> id, String isPresent, String amount, String operation)
    {
        super(type, activation, source);
        this.attribute = attribute;
        this.id = id;
        this.isPresent = isPresent;
        this.amount = amount;
        this.operation = operation;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
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

    public String getAmount()
    {
        return amount;
    }
    
    public String getOperation()
    {
        return operation;
    }

    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget t)
    {
        SpellsUtil.stringToHolder(ctx, attribute, BuiltInRegistries.ATTRIBUTE).ifPresent(attribute1 ->
        {
            AttributeInstance a = t.getLivingEntity().getAttribute(attribute1);

            if(a != null)
            {
                id.getValue(ctx).ifPresent(id1 ->
                {
                    ResourceLocation rl = ResourceLocation.parse(id1);

                    if(a.hasModifier(rl))
                    {
                        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isPresent, true);

                        AttributeModifier m = a.getModifier(rl);
                        if(m != null)
                        {
                            ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), amount, m.amount());
                            ctx.setCtxVar(CtxVarTypes.STRING.get(), operation, SpellsUtil.operationToString(m.operation()));
                        }
                    }
                    else
                    {
                        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isPresent, false);
                    }
                });
            }
        });
    }
}
