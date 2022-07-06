package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.network.ManaSyncMessage;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

public class ManaHolder implements IManaHolder
{
    // Amount of ticks until 1 half mana bottle is restored naturally
    public static final int TICKS_UNTIL_REG = 50;
    
    protected float mana;
    protected float extraMana;
    
    protected float lastSentMana;
    protected float lastSentExtraMana;
    
    public int regenTime;
    public int changeTime;
    
    protected final LivingEntity player;
    
    public ManaHolder(LivingEntity player)
    {
        mana = 0F;
        extraMana = 0F;
        lastSentMana = -1F;
        lastSentExtraMana = -1F;
        regenTime = 0;
        changeTime = -1;
        this.player = player;
    }
    
    @Override
    public void setMana(float mana)
    {
        this.mana = mana;
        this.checkSyncStatus();
    }
    
    @Override
    public float getMana()
    {
        return mana;
    }
    
    @Override
    public void setExtraMana(float extraMana)
    {
        this.extraMana = extraMana;
        
        if(this.extraMana < 0F)
        {
            this.extraMana = 0F;
        }
        
        this.checkSyncStatus();
    }
    
    @Override
    public float getExtraMana()
    {
        return extraMana;
    }
    
    @Override
    public void replenish(float amount)
    {
        mana += amount;
        float maxMana = getMaxMana();
        
        if(mana > maxMana)
        {
            mana = maxMana;
        }
        
        this.checkSyncStatus();
    }
    
    @Override
    public void burn(float amount)
    {
        boolean wasFull = this.mana >= this.getMaxMana();
        
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
            this.regenTime = 0;
        }
        
        this.checkSyncStatus();
    }
    
    @Override
    public LivingEntity getPlayer()
    {
        return player;
    }
    
    public float getMaxMana()
    {
        AttributeInstance attrMana = player.getAttribute(SpellsRegistries.MAX_MANA.get());
        
        if(attrMana != null)
        {
            return (float) attrMana.getValue();
        }
        else
        {
            return 0F;
        }
    }
    
    public void tick()
    {
        if(changeTime > 0)
        {
            --changeTime;
        }
        
        ++regenTime;
        
        if(regenTime >= TICKS_UNTIL_REG || this.getMana() > this.getMaxMana())
        {
            replenish(1.0F);
            regenTime = 0;
        }
    }
    
    public void checkSyncStatus()
    {
        if(mana != lastSentMana || extraMana != lastSentExtraMana)
        {
            changeTime = 20;
            
            lastSentMana = mana;
            lastSentExtraMana = extraMana;
            
            if(player.level.isClientSide)
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
    
    @Override
    public void sendSync()
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), this.makeSyncMessage());
        }
    }
    
    public static LazyOptional<ManaHolder> getManaHolder(LivingEntity entity)
    {
        return entity.getCapability(SpellsCapabilities.MANA_CAPABILITY).cast();
    }
}
