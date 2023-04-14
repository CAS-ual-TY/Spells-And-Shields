package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SpellInstance
{
    private Holder<Spell> spell;
    private float manaCost;
    private List<CtxVar<?>> parameters;
    
    private SpellNodeId nodeId;
    
    private Optional<TooltipComponent> tooltipComponent;
    
    public SpellInstance(Holder<Spell> spell, float manaCost, List<CtxVar<?>> parameters)
    {
        this.spell = spell;
        this.manaCost = manaCost;
        this.parameters = parameters;
        nodeId = null;
        tooltipComponent = null;
    }
    
    public SpellInstance(Holder<Spell> spell, float manaCost)
    {
        this(spell, manaCost, new LinkedList<>());
    }
    
    public SpellInstance(Holder<Spell> spell)
    {
        this(spell, -1, new LinkedList<>());
    }
    
    public <T> SpellInstance addParameter(CtxVar<T> ctxVar)
    {
        parameters.add(ctxVar);
        return this;
    }
    
    public void setManaCost(float manaCost)
    {
        this.manaCost = manaCost;
    }
    
    public void initId(SpellNodeId nodeId)
    {
        this.nodeId = nodeId;
    }
    
    public Holder<Spell> getSpell()
    {
        return spell;
    }
    
    public float getManaCost()
    {
        return manaCost;
    }
    
    public List<CtxVar<?>> getParameters()
    {
        return parameters;
    }
    
    public SpellNodeId getNodeId()
    {
        return nodeId;
    }
    
    public float getAppliedManaCost()
    {
        return manaCost >= 0 ? manaCost : spell.get().getManaCost();
    }
    
    public Optional<TooltipComponent> getTooltipComponent()
    {
        if(tooltipComponent == null)
        {
            float applied = getAppliedManaCost();
            tooltipComponent = applied != 0 ? Optional.of(new ManaTooltipComponent(applied)) : Optional.empty();
        }
        return tooltipComponent;
    }
    
    public boolean run(Player owner, String activation)
    {
        return run(owner.level, owner, activation);
    }
    
    public boolean run(Level level, @Nullable Player owner, String activation)
    {
        return run(level, owner, activation, false, (ctx) -> {}, (ctx) -> {});
    }
    
    public boolean run(Level level, @Nullable Player owner, String activation, Consumer<SpellContext> preRun)
    {
        return run(level, owner, activation, false, preRun, (ctx) -> {});
    }
    
    public boolean forceRun(Level level, @Nullable Player owner, String activation, Consumer<SpellContext> preRun)
    {
        return run(level, owner, activation, true, preRun, (ctx) -> {});
    }
    
    public boolean run(Player owner, String event, Consumer<SpellContext> toContext, Consumer<SpellContext> fromContext)
    {
        return run(owner.level, owner, event, false, toContext, fromContext);
    }
    
    public boolean run(Level level, @Nullable Player owner, String activation, boolean force, Consumer<SpellContext> preRun, Consumer<SpellContext> postRun)
    {
        if(spell.get().getEventsList().contains(activation) || force)
        {
            SpellContext ctx = initializeContext(level, owner, activation);
            preRun.accept(ctx);
            ctx.run();
            postRun.accept(ctx);
            return true;
        }
        return false;
    }
    
    public SpellContext initializeContext(Level level, @Nullable Player owner, String activation)
    {
        SpellContext ctx = new SpellContext(level, owner, this);
        
        ctx.activate(activation);
        ctx.initCtxVar(new CtxVar<>(CtxVarTypes.DOUBLE.get(), BuiltinVariables.MANA_COST.name, (double) getAppliedManaCost()));
        ctx.initCtxVar(new CtxVar<>(CtxVarTypes.INT.get(), BuiltinVariables.MIN_BLOCK_HEIGHT.name, level.getMinBuildHeight()));
        ctx.initCtxVar(new CtxVar<>(CtxVarTypes.INT.get(), BuiltinVariables.MAX_BLOCK_HEIGHT.name, level.getMaxBuildHeight() - 1));
        
        if(owner != null)
        {
            ctx.getOrCreateTargetGroup(BuiltinTargetGroups.OWNER.targetGroup).addTargets(Target.of(owner));
        }
        
        spell.get().getParameters().forEach(ctx::initCtxVar);
        parameters.forEach(ctx::initCtxVar);
        
        return ctx;
    }
    
    public SpellInstance copy()
    {
        return new SpellInstance(spell, manaCost, new LinkedList<>(parameters));
    }
    
    public void toNbt(CompoundTag nbt, Registry<Spell> spellRegistry)
    {
        if(nodeId != null)
        {
            nodeId.toNbt(nbt);
        }
        else
        {
            nbt.putString("spellId", spell.unwrap().map(ResourceKey::location, spellRegistry::getKey).toString());
        }
    }
    
    @javax.annotation.Nullable
    public static SpellInstance fromNbt(CompoundTag nbt, Registry<SpellTree> spellTreeRegistry, Registry<Spell> spellRegistry)
    {
        SpellNodeId nodeId = SpellNodeId.fromNbt(nbt);
        
        if(nodeId != null)
        {
            return nodeId.getSpellInstance(spellTreeRegistry);
        }
        else if(nbt.contains("spellId", Tag.TAG_STRING))
        {
            return new SpellInstance(spellRegistry.getHolderOrThrow(ResourceKey.create(Spells.REGISTRY_KEY, new ResourceLocation(nbt.getString("spellId")))));
        }
        else
        {
            return null;
        }
    }
}
