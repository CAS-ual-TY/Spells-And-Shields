package de.cas_ual_ty.spells.spell.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.Map;

public class SpitMetalSpell extends HandIngredientSpell implements IProjectileSpell
{
    public final float defaultBaseDamage;
    public final Map<Item, Float> defaultIngredientMap;
    
    protected float baseDamage;
    protected Map<Item, Float> ingredientMap;
    
    public SpitMetalSpell(float manaCost, float baseDamage, Map<Item, Float> ingredientMap)
    {
        super(manaCost);
        this.defaultBaseDamage = baseDamage;
        this.defaultIngredientMap = ingredientMap;
        this.ingredientMap = new HashMap<>();
    }
    
    public SpitMetalSpell(float manaCost)
    {
        super(manaCost);
        this.defaultBaseDamage = 4F;
        
        this.ingredientMap = new HashMap<>();
        ingredientMap.put(Items.IRON_NUGGET, Tiers.IRON.getAttackDamageBonus());
        ingredientMap.put(Items.GOLD_NUGGET, Tiers.GOLD.getAttackDamageBonus());
        this.defaultIngredientMap = ImmutableMap.copyOf(ingredientMap);
        this.ingredientMap.clear();
    }
    
    @Override
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
    {
        float damage = ingredientMap.getOrDefault(itemStack.getItem(), 0F);
        shootStraight(manaHolder, (projectile, level) ->
        {
            level.playSound(null, manaHolder.getPlayer(), SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            projectile.getSpellDataTag().putFloat("materialDamage", damage);
        });
    }
    
    @Override
    public void onEntityHit(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        Entity hit = entityHitResult.getEntity();
        if(hit instanceof LivingEntity livingEntity)
        {
            livingEntity.hurt(DamageSource.indirectMagic(entity, entity.getOwner()), entity.getSpellDataTag().getFloat("materialDamage"));
        }
        IProjectileSpell.super.onEntityHit(entity, entityHitResult);
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return ingredientMap.containsKey(itemStack.getItem());
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        itemStack.shrink(1);
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("baseDamage", this.defaultBaseDamage);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        this.baseDamage = SpellsFileUtil.jsonFloat(json, "baseDamage");
        this.ingredientMap.putAll(this.defaultIngredientMap);
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        this.baseDamage = defaultBaseDamage;
        this.ingredientMap.putAll(this.defaultIngredientMap);
    }
}
