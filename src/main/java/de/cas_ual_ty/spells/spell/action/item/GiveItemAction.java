package de.cas_ual_ty.spells.spell.action.item;

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
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class GiveItemAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<GiveItemAction> makeCodec(SpellActionType<GiveItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(GiveItemAction::getAmount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(GiveItemAction::getDamage),
                CtxVarTypes.TAG.get().optionalRefCodec(ParamNames.paramCompoundTag("tag")).forGetter(GiveItemAction::getTag),
                CtxVarTypes.STRING.get().refCodec().fieldOf("item").forGetter(GiveItemAction::getItem)
        ).apply(instance, (activation, multiTargets, amount, damage, tag, item) -> new GiveItemAction(type, activation, multiTargets, amount, damage, tag, item)));
    }
    
    public static GiveItemAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> amount, DynamicCtxVar<Integer> damage, @Nullable DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<String> item)
    {
        return new GiveItemAction(SpellActionTypes.GIVE_ITEM.get(), activation.toString(), multiTargets.toString(), amount, damage, tag, item);
    }
    
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<Integer> damage;
    protected DynamicCtxVar<CompoundTag> tag;
    protected DynamicCtxVar<String> item;
    
    public GiveItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GiveItemAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> amount, DynamicCtxVar<Integer> damage, DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<String> item)
    {
        super(type, activation, multiTargets);
        this.amount = amount;
        this.damage = damage;
        this.tag = tag;
        this.item = item;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public DynamicCtxVar<Integer> getDamage()
    {
        return damage;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    public DynamicCtxVar<String> getItem()
    {
        return item;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        SpellsUtil.stringToObject(ctx, item, ForgeRegistries.ITEMS).ifPresent(item ->
        {
            ItemStack itemStack = new ItemStack(item);
            
            amount.getValue(ctx).ifPresent(amount ->
            {
                if(amount >= 0)
                {
                    itemStack.setCount(amount);
                }
            });
            
            if(itemStack.isEmpty())
            {
                return;
            }
            
            damage.getValue(ctx).ifPresent(damage ->
            {
                if(damage >= 0)
                {
                    itemStack.setDamageValue(damage);
                }
            });
            
            tag.getValue(ctx).ifPresent(itemStack::setTag);
            
            playerTarget.getPlayer().getInventory().add(itemStack);
        });
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}
