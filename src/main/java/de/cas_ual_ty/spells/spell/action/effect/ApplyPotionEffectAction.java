package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;

public class ApplyPotionEffectAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ApplyPotionEffectAction> makeCodec(SpellActionType<ApplyPotionEffectAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("potion")).forGetter(ApplyPotionEffectAction::getPotion)
        ).apply(instance, (activation, multiTargets, potion) -> new ApplyPotionEffectAction(type, activation, multiTargets, potion)));
    }
    
    public static ApplyPotionEffectAction make(String activation, String multiTargets, DynamicCtxVar<String> potion)
    {
        return new ApplyPotionEffectAction(SpellActionTypes.APPLY_POTION_EFFECT.get(), activation, multiTargets, potion);
    }
    
    protected DynamicCtxVar<String> potion;
    
    public ApplyPotionEffectAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyPotionEffectAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> potion)
    {
        super(type, activation, multiTargets);
        this.potion = potion;
    }
    
    public DynamicCtxVar<String> getPotion()
    {
        return potion;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        SpellsUtil.stringToObject(ctx, potion, ForgeRegistries.POTIONS).ifPresent(potion ->
        {
            PotionUtils.getAllEffects(potion, new LinkedList<>()).forEach(effect ->
            {
                target.getLivingEntity().addEffect(effect);
            });
        });
    }
}
