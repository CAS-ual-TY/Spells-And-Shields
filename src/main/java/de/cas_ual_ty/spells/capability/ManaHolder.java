package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.network.ManaSyncMessage;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.network.PacketDistributor;

public class ManaHolder implements INBTSerializable<ListTag>
{
    // Amount of ticks until 1 half mana bottle is restored naturally
    public static final int TICKS_UNTIL_REG = 50;
    
    protected float mana;
    protected float extraMana;
    
    protected float lastSentMana;
    protected float lastSentExtraMana;
    
    public int regenTime;
    public int ticksUntilNextReg;
    public int changeTime;
    
    protected final LivingEntity player;
    
    public ManaHolder(LivingEntity player)
    {
        mana = 0F;
        extraMana = 0F;
        lastSentMana = -1F;
        lastSentExtraMana = -1F;
        regenTime = 0;
        ticksUntilNextReg = calcTicksUntilReg();
        changeTime = -1;
        this.player = player;
    }
    
    public void setMana(float mana)
    {
        this.mana = mana;
        checkSyncStatus();
    }
    
    public float getMana()
    {
        return mana;
    }
    
    public void setExtraMana(float extraMana)
    {
        this.extraMana = extraMana;
        
        if(this.extraMana < 0F)
        {
            this.extraMana = 0F;
        }
        
        checkSyncStatus();
    }
    
    public float getExtraMana()
    {
        return extraMana;
    }
    
    public void replenish(float amount)
    {
        mana += amount;
        float maxMana = getMaxMana();
        
        if(mana > maxMana)
        {
            mana = maxMana;
        }
        
        checkSyncStatus();
    }
    
    public void burn(float amount)
    {
        boolean wasFull = mana >= getMaxMana();
        
        if(extraMana > 0F)
        {
            extraMana -= amount;
            
            if(extraMana < 0F)
            {
                amount = -extraMana;
                extraMana = 0F;
            }
            else
            {
                amount = 0;
            }
        }
        
        mana -= amount;
        
        if(mana < 0F)
        {
            mana = 0F;
        }
        
        if(wasFull)
        {
            regenTime = 0;
        }
        
        checkSyncStatus();
    }
    
    public LivingEntity getPlayer()
    {
        return player;
    }
    
    public float getMaxMana()
    {
        AttributeInstance attrMana = player.getAttribute(BuiltInRegisters.MAX_MANA_ATTRIBUTE.get());
        
        double attribute = attrMana == null ? 0F : attrMana.getValue();
        
        for(EquipmentSlot s : EquipmentSlot.values())
        {
            if(s.getType() == EquipmentSlot.Type.ARMOR)
            {
                ItemStack itemStack = player.getItemBySlot(s);
                int level = itemStack.getEnchantmentLevel(BuiltInRegisters.MAX_MANA_ENCHANTMENT.get());
                double increase = BuiltInRegisters.MAX_MANA_ENCHANTMENT.get().getAttributeIncrease(level, s);
                attribute += increase;
            }
        }
        
        return (float) attribute;
    }
    
    public void tick()
    {
        if(changeTime > 0)
        {
            --changeTime;
        }
        
        ++regenTime;
        
        if(regenTime >= ticksUntilNextReg || getMana() > getMaxMana())
        {
            replenish(1F);
            regenTime = 0;
            ticksUntilNextReg = calcTicksUntilReg();
        }
    }
    
    protected int calcTicksUntilReg()
    {
        if(!(player instanceof Player))
        {
            return TICKS_UNTIL_REG;
        }
        
        Player player = (Player) this.player;
        
        double attribute = player.getAttributeValue(BuiltInRegisters.MANA_REGENERATION_ATTRIBUTE.get());
        
        for(EquipmentSlot s : EquipmentSlot.values())
        {
            if(s.getType() == EquipmentSlot.Type.ARMOR)
            {
                ItemStack itemStack = player.getItemBySlot(s);
                int level = itemStack.getEnchantmentLevel(BuiltInRegisters.MANA_REGENERATION_ENCHANTMENT.get());
                double increase = BuiltInRegisters.MANA_REGENERATION_ENCHANTMENT.get().getAttributeIncrease(level, s);
                attribute += increase;
            }
        }
        
        if(attribute <= 0)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return Mth.ceil(TICKS_UNTIL_REG / attribute);
        }
    }
    
    public void checkSyncStatus()
    {
        if(mana != lastSentMana || extraMana != lastSentExtraMana)
        {
            changeTime = 20;
            
            lastSentMana = mana;
            lastSentExtraMana = extraMana;
            
            if(player.level().isClientSide)
            {
                return;
            }
            
            sendSync();
        }
    }
    
    public ManaSyncMessage makeSyncMessage()
    {
        return new ManaSyncMessage(player.getId(), mana, extraMana);
    }
    
    @Override
    public ListTag serializeNBT()
    {
        ListTag tag = new ListTag();
        tag.add(0, FloatTag.valueOf(mana));
        tag.add(1, FloatTag.valueOf(extraMana));
        return tag;
    }
    
    @Override
    public void deserializeNBT(ListTag tag)
    {
        if(tag.get(0).getId() == Tag.TAG_FLOAT && tag.get(1).getId() == Tag.TAG_FLOAT)
        {
            mana = tag.getFloat(0);
            extraMana = tag.getFloat(1);
        }
    }
    
    public void sendSync()
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), makeSyncMessage());
        }
    }
    
    public static LazyOptional<ManaHolder> getManaHolder(LivingEntity entity)
    {
        return entity.getCapability(SpellsCapabilities.MANA_CAPABILITY).cast();
    }
}
