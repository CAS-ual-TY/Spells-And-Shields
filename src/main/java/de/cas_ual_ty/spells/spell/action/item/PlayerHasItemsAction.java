package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerHasItemsAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<PlayerHasItemsAction> makeCodec(SpellActionType<PlayerHasItemsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("item")).forGetter(PlayerHasItemsAction::getItem),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(PlayerHasItemsAction::getAmount),
                CtxVarTypes.TAG.get().optionalRefCodec(ParamNames.paramCompoundTag("tag")).forGetter(PlayerHasItemsAction::getTag),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("must_be_in_hand")).forGetter(PlayerHasItemsAction::getMustBeInHand),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("creative_bypass")).forGetter(PlayerHasItemsAction::getCreativeBypass)
        ).apply(instance, (activation, source, item, amount, tag, mustBeInHand, creativeBypass) -> new PlayerHasItemsAction(type, activation, source, item, amount, tag, mustBeInHand, creativeBypass)));
    }
    
    public static PlayerHasItemsAction make(Object activation, Object source, DynamicCtxVar<String> item, DynamicCtxVar<Integer> amount, @Nullable DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<Boolean> mustBeInHand, DynamicCtxVar<Boolean> creativeBypass)
    {
        return new PlayerHasItemsAction(SpellActionTypes.PLAYER_HAS_ITEMS.get(), activation.toString(), source.toString(), item, amount, tag, mustBeInHand, creativeBypass);
    }
    
    protected DynamicCtxVar<String> item;
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<CompoundTag> tag;
    protected DynamicCtxVar<Boolean> mustBeInHand;
    protected DynamicCtxVar<Boolean> creativeBypass;
    
    public PlayerHasItemsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PlayerHasItemsAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<String> item, DynamicCtxVar<Integer> amount, DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<Boolean> mustBeInHand, DynamicCtxVar<Boolean> creativeBypass)
    {
        super(type, activation, source);
        this.item = item;
        this.amount = amount;
        this.tag = tag;
        this.mustBeInHand = mustBeInHand;
        this.creativeBypass = creativeBypass;
    }
    
    public DynamicCtxVar<String> getItem()
    {
        return item;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    public DynamicCtxVar<Boolean> getMustBeInHand()
    {
        return mustBeInHand;
    }
    
    public DynamicCtxVar<Boolean> getCreativeBypass()
    {
        return creativeBypass;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        SpellsUtil.stringToObject(ctx, item, BuiltInRegistries.ITEM).ifPresent(item ->
        {
            amount.getValue(ctx).ifPresent(amount ->
            {
                mustBeInHand.getValue(ctx).ifPresent(mustBeInHand ->
                {
                    creativeBypass.getValue(ctx).ifPresent(creativeBypass ->
                    {
                        Player player = playerTarget.getPlayer();
                        
                        if(creativeBypass && player.isCreative())
                        {
                            return;
                        }
                        
                        List<ItemStack> items;
                        if(mustBeInHand)
                        {
                            items = List.of(player.getMainHandItem(), player.getOffhandItem());
                        }
                        else
                        {
                            items = new ArrayList<>(player.getInventory().items.size());
                            items.addAll(player.getInventory().items);
                            items.addAll(player.getInventory().offhand);
                        }
                        
                        CompoundTag tag = this.tag.getValue(ctx).orElse(null);
                        
                        int count = 0;
                        
                        for(ItemStack i : items)
                        {
                            if(i.getItem() == item && (tag == null || tag.isEmpty() || (i.getTag() != null && tag.equals(i.getTag()))))
                            {
                                count += i.getCount();
                            }
                            
                            if(count >= amount)
                            {
                                return;
                            }
                        }
                        
                        ctx.deactivate(activation);
                    });
                });
            });
        });
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}
