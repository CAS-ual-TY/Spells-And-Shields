package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.progression.FullSpellNodeId;
import de.cas_ual_ty.spells.progression.SpellTree;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
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

/**
 * A spell a player can run. Either a {@link Direct} reference straight to a {@link Spell} registry entry,
 * or a {@link TreeNode} reference to a {@link de.cas_ual_ty.spells.progression.SpellNode} - which overrides
 * the referenced spell's mana cost/parameters with whatever that node bakes in. There is no third layer:
 * a {@link TreeNode} is a pure reference to a node's own values, it does not carry further overrides itself.
 */
public abstract class SpellInstance
{
    private Optional<TooltipComponent> tooltipComponent;

    protected SpellInstance()
    {
        tooltipComponent = null;
    }

    public abstract Holder<Spell> getSpell();

    public abstract float getManaCost();

    public abstract List<CtxVar<?>> getParameters();

    @Nullable
    public abstract FullSpellNodeId getNodeId();

    public abstract SpellInstance copy();

    public abstract void toNbt(CompoundTag nbt, Registry<Spell> spellRegistry);

    public float getAppliedManaCost()
    {
        float manaCost = getManaCost();
        return manaCost >= 0 ? manaCost : getSpell().value().getManaCost();
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

    public boolean run(Player owner, String event)
    {
        return run(owner.level(), owner, event);
    }

    public boolean run(Level level, @Nullable Player owner, String event)
    {
        return run(level, owner, event, false, (ctx) -> {}, (ctx) -> {});
    }

    public boolean run(Level level, @Nullable Player owner, String event, Consumer<SpellContext> preRun)
    {
        return run(level, owner, event, false, preRun, (ctx) -> {});
    }

    /**
     * Runs the spell starting directly at {@code activation}, skipping the spell's own event map entirely.
     * For callers that already resolved their own activation locally and just need the spell entered at that
     * exact point - currently {@link de.cas_ual_ty.spells.spell.projectile.SpellProjectile}'s hit/timeout
     * callbacks (resolved per-projectile-instance from {@code ShootAction}/{@code HomeAction} params) and
     * {@link de.cas_ual_ty.spells.capability.DelayedSpellHolder} (resolved per-delayed-spell via its own
     * {@code eventsMap}). Everything else - direct casts, admin commands, all {@code SpellsEvents} dispatch -
     * goes through the regular {@code run} overloads below and always passes through the spell's event map.
     */
    public boolean forceRun(Level level, @Nullable Player owner, String activation, Consumer<SpellContext> preRun)
    {
        return run(level, owner, activation, true, preRun, (ctx) -> {});
    }

    public boolean run(Player owner, String event, Consumer<SpellContext> toContext, Consumer<SpellContext> fromContext)
    {
        return run(owner.level(), owner, event, false, toContext, fromContext);
    }

    public boolean run(Level level, @Nullable Player owner, String event, boolean preResolved, Consumer<SpellContext> preRun, Consumer<SpellContext> postRun)
    {
        // when not preResolved, "event" is the raw event id coming from the dispatcher (eg. SpellsEvents) -
        // the spell's own event map decides which activation that turns into, so different event ids can be
        // configured to fire the same activation. A preResolved run (see forceRun above) already passes a
        // real activation directly via this same parameter and skips this lookup entirely.
        String activation = preResolved ? event : getSpell().value().getEventActivation(event);

        if(activation != null && !level.isClientSide)
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

        getSpell().value().getParameters().forEach(ctx::initCtxVar);
        getParameters().forEach(ctx::initCtxVar);

        return ctx;
    }

    @Nullable
    public static SpellInstance fromNbt(CompoundTag nbt, Registry<SpellTree> spellTreeRegistry, Registry<Spell> spellRegistry)
    {
        FullSpellNodeId nodeId = FullSpellNodeId.fromNbt(nbt);

        if(nodeId != null)
        {
            return nodeId.getSpellInstance(spellTreeRegistry);
        }
        else if(nbt.contains("spellId", Tag.TAG_STRING))
        {
            return spellRegistry.getHolder(ResourceKey.create(Spells.REGISTRY_KEY, ResourceLocation.parse(nbt.getString("spellId"))))
                    .<SpellInstance>map(SpellInstance::direct)
                    .orElse(null);
        }

        return null;
    }

    public static Direct direct(Holder<Spell> spell, float manaCost, List<CtxVar<?>> parameters)
    {
        return new Direct(spell, manaCost, parameters);
    }

    public static Direct direct(Holder<Spell> spell, float manaCost)
    {
        return direct(spell, manaCost, new LinkedList<>());
    }

    public static Direct direct(Holder<Spell> spell)
    {
        return direct(spell, -1, new LinkedList<>());
    }

    public static TreeNode treeNode(FullSpellNodeId nodeId, Direct resolved)
    {
        return new TreeNode(nodeId, resolved);
    }

    /**
     * References a {@link Spell} registry entry directly. Owns its own mana cost/parameter overrides.
     * This is also what every {@link de.cas_ual_ty.spells.progression.SpellNode} stores internally.
     */
    public static final class Direct extends SpellInstance
    {
        private final Holder<Spell> spell;
        private float manaCost;
        private final List<CtxVar<?>> parameters;

        private Direct(Holder<Spell> spell, float manaCost, List<CtxVar<?>> parameters)
        {
            this.spell = spell;
            this.manaCost = manaCost;
            this.parameters = parameters;
        }

        public Direct addParameter(CtxVar<?> ctxVar)
        {
            parameters.add(ctxVar);
            return this;
        }

        public void setManaCost(float manaCost)
        {
            this.manaCost = manaCost;
        }

        @Override
        public Holder<Spell> getSpell()
        {
            return spell;
        }

        @Override
        public float getManaCost()
        {
            return manaCost;
        }

        @Override
        public List<CtxVar<?>> getParameters()
        {
            return parameters;
        }

        @Override
        @Nullable
        public FullSpellNodeId getNodeId()
        {
            return null;
        }

        @Override
        public Direct copy()
        {
            return new Direct(spell, manaCost, new LinkedList<>(parameters));
        }

        @Override
        public void toNbt(CompoundTag nbt, Registry<Spell> spellRegistry)
        {
            nbt.putString("spellId", spell.unwrap().map(ResourceKey::location, spellRegistry::getKey).toString());
        }
    }

    /**
     * References a {@link de.cas_ual_ty.spells.progression.SpellNode} by its {@link FullSpellNodeId}, delegating
     * every value to that node's own {@link Direct} instance. Pure reference - no override of its own.
     */
    public static final class TreeNode extends SpellInstance
    {
        private final FullSpellNodeId nodeId;
        private final Direct resolved;

        private TreeNode(FullSpellNodeId nodeId, Direct resolved)
        {
            this.nodeId = nodeId;
            this.resolved = resolved;
        }

        @Override
        public Holder<Spell> getSpell()
        {
            return resolved.getSpell();
        }

        @Override
        public float getManaCost()
        {
            return resolved.getManaCost();
        }

        @Override
        public List<CtxVar<?>> getParameters()
        {
            return resolved.getParameters();
        }

        @Override
        public FullSpellNodeId getNodeId()
        {
            return nodeId;
        }

        @Override
        public TreeNode copy()
        {
            return new TreeNode(nodeId, resolved);
        }

        @Override
        public void toNbt(CompoundTag nbt, Registry<Spell> spellRegistry)
        {
            nodeId.toNbt(nbt);
        }
    }
}
