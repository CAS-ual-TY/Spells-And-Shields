package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LightningStrikeSpell extends BaseIngredientsSpell
{
    public final int defaultRange;
    protected int range;
    
    public LightningStrikeSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int range)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultRange = range;
    }
    
    public LightningStrikeSpell(float manaCost, ItemStack handIngredient, int range)
    {
        super(manaCost, handIngredient);
        defaultRange = range;
    }
    
    public LightningStrikeSpell(float manaCost, int range)
    {
        super(manaCost);
        defaultRange = range;
    }
    
    public LightningStrikeSpell()
    {
        this(8F, new ItemStack(Items.COPPER_INGOT), 20);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        Level level = manaHolder.getPlayer().level;
        
        LivingEntity entity = manaHolder.getPlayer();
        HitResult hit = SpellsUtil.rayTrace(level, entity, range, e -> true, 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY);
        
        Vec3 oldPos = entity.position();
        
        if(hit.getType() != HitResult.Type.MISS)
        {
            Vec3 pos = null;
            BlockPos blockPos = null;
            
            if(hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit)
            {
                pos = blockHit.getLocation();
                blockPos = blockHit.getBlockPos().offset(blockHit.getDirection().getNormal());
            }
            else if(hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult entityHit)
            {
                pos = entityHit.getEntity().position();
                blockPos = entityHit.getEntity().blockPosition();
            }
            
            if(pos != null && level.canSeeSky(blockPos))
            {
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                lightningBolt.moveTo(pos);
                level.addFreshEntity(lightningBolt);
            }
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("range", defaultRange);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        range = SpellsFileUtil.jsonInt(json, "range");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        range = defaultRange;
    }
}
