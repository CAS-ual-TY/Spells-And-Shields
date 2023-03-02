package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellDataHolder;
import de.cas_ual_ty.spells.spell.ITickedDataSpell;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
            level.playSound(null, entity, SoundEvents.GHAST_WARN, SoundSource.PLAYERS, 10F, 1F);
        }
        else if(tickTime == 20)
        {
            Vec3 view = entity.getViewVector(1F).scale(2D);
            Fireball fireball = new LargeFireball(level, entity, view.x, view.y, view.z, 1);
            fireball.setPos(entity.getEyePosition()
                    .add(view)
                    .add(entity.getDeltaMovement()));
            
            level.playSound(null, entity, SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1F, 1F);
            
            level.addFreshEntity(fireball);
        }
    }
}