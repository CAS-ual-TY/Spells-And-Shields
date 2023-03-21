package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class AddAttributeModifierAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<AddAttributeModifierAction> makeCodec(SpellActionType<AddAttributeModifierAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(AddAttributeModifierAction::getAttribute),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramInt("uuid")).forGetter(AddAttributeModifierAction::getUuid),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramInt("name")).forGetter(AddAttributeModifierAction::getName),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramBoolean("amount")).forGetter(AddAttributeModifierAction::getAmount),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramBoolean("operation")).forGetter(AddAttributeModifierAction::getOperation)
        ).apply(instance, (activation, targets, attribute, uuid, name, amount, operation) -> new AddAttributeModifierAction(type, activation, targets, attribute, uuid, name, amount, operation)));
    }
    
    public static AddAttributeModifierAction make(String activation, String targets, Attribute attribute, DynamicCtxVar<String> uuid, DynamicCtxVar<String> name, DynamicCtxVar<Double> amount, DynamicCtxVar<String> operation)
    {
        return new AddAttributeModifierAction(SpellActionTypes.ADD_ATTRIBUTE_MODIFIER.get(), activation, targets, attribute, uuid, name, amount, operation);
    }
    
    protected Attribute attribute;
    protected DynamicCtxVar<String> uuid;
    protected DynamicCtxVar<String> name;
    protected DynamicCtxVar<Double> amount;
    protected DynamicCtxVar<String> operation;
    
    public AddAttributeModifierAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AddAttributeModifierAction(SpellActionType<?> type, String activation, String targets, Attribute attribute, DynamicCtxVar<String> uuid, DynamicCtxVar<String> name, DynamicCtxVar<Double> amount, DynamicCtxVar<String> operation)
    {
        super(type, activation, targets);
        this.attribute = attribute;
        this.uuid = uuid;
        this.name = name;
        this.amount = amount;
        this.operation = operation;
    }
    
    public Attribute getAttribute()
    {
        return attribute;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    public DynamicCtxVar<String> getName()
    {
        return name;
    }
    
    public DynamicCtxVar<Double> getAmount()
    {
        return amount;
    }
    
    public DynamicCtxVar<String> getOperation()
    {
        return operation;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        if(attribute != null)
        {
            AttributeInstance a = target.getLivingEntity().getAttribute(attribute);
            
            if(a != null)
            {
                name.getValue(ctx).ifPresent(name ->
                {
                    amount.getValue(ctx).ifPresent(amount ->
                    {
                        operation.getValue(ctx).map(SpellsUtil::operationFromString).ifPresent(op ->
                        {
                            this.uuid.getValue(ctx).map(UUID::fromString).ifPresentOrElse(uuid ->
                            {
                                a.addPermanentModifier(new AttributeModifier(uuid, name, amount, op));
                            }, () ->
                            {
                                a.addPermanentModifier(new AttributeModifier(name, amount, op));
                            });
                        });
                    });
                });
            }
        }
    }
}
