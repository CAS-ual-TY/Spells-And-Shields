package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellDataHolder;
import de.cas_ual_ty.spells.spell.ITickedDataSpell;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GhastSpell extends BaseIngredientsSpell implements ITickedDataSpell
{
    public GhastSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public GhastSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    public GhastSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public GhastSpell()
    {
        this(4F, new ItemStack(Items.FIRE_CHARGE));
    }
    
    @Override
    public boolean canActivate(ManaHolder manaHolder)
    {
        LazyOptional<SpellDataHolder> spellDataHolder = SpellDataHolder.getSpellDataHolder(manaHolder.getPlayer());
        
        if(super.canActivate(manaHolder) && spellDataHolder.isPresent())
        {
            AtomicBoolean activate = new AtomicBoolean(true);
            
            spellDataHolder.ifPresent(spellDataHolder1 ->
            {
                activate.set(!spellDataHolder1.hasOfType(SpellsRegistries.GHAST_DATA.get()));
            });
            
            return activate.get();
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        SpellDataHolder.getSpellDataHolder(manaHolder.getPlayer()).ifPresent(spellDataHolder ->
        {
            spellDataHolder.add(ITickedDataSpell.makeData(SpellsRegistries.GHAST_DATA.get()));
        });
    }
    
    @Override
    public int getMaxTime(SpellDataHolder spellDataHolder)
    {
        return 20;
    }
    
    @Override
    public void dataTick(SpellDataHolder spellDataHolder, int tickTime)
    {
        Level level = spellDataHolder.getEntity().level;
        LivingEntity entity = spellDataHolder.getEntity();
        
        if(tickTime == 10)
        {
            level.playSound(null, entity, SoundEvents.GHAST_WARN, SoundSource.PLAYERS, 10.0F, 1.0F);
        }
        else if(tickTime == 20)
        {
            Fireball fireball = new LargeFireball(EntityType.FIREBALL, level);
            fireball.setPos(entity.getEyePosition()
                    .add(entity.getViewVector(1.0F).scale(2D))
                    .add(entity.getDeltaMovement()));
            fireball.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 3.0F, 1.0F);
            fireball.setOwner(entity);
            
            level.playSound(null, entity, SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            level.addFreshEntity(fireball);
        }
    }
}