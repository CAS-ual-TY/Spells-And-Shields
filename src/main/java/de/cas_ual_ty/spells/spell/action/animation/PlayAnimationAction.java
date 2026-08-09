package de.cas_ual_ty.spells.spell.action.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.compat.ModCompat;
import de.cas_ual_ty.spells.compat.playeranimator.PlayerAnimatorHooks;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Plays a Player Animator animation (see {@link PlayerAnimatorHooks}) on a player target, synced to every
 * client. If a new animation comes in while the old one plays, the old one is discarded outright - no fade.
 * <p>
 * This action exists whether or not Player Animator is installed - the animation just silently doesn't play if
 * it isn't (see {@link ModCompat#PLAYER_ANIMATOR}), so spells using it degrade gracefully rather than crashing
 * or erroring for players who don't have the optional dependency.
 */
public class PlayAnimationAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<PlayAnimationAction> makeCodec(SpellActionType<PlayAnimationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("animation")).forGetter(PlayAnimationAction::getAnimation)
        ).apply(instance, (activation, multiTargets, animation) -> new PlayAnimationAction(type, activation, multiTargets, animation)));
    }

    public static PlayAnimationAction make(Object activation, Object multiTargets, DynamicCtxVar<String> animation)
    {
        return new PlayAnimationAction(SpellActionTypes.PLAY_ANIMATION.get(), activation.toString(), multiTargets.toString(), animation);
    }

    protected DynamicCtxVar<String> animation;

    public PlayAnimationAction(SpellActionType<?> type)
    {
        super(type);
    }

    public PlayAnimationAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> animation)
    {
        super(type, activation, multiTargets);
        this.animation = animation;
    }

    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }

    public DynamicCtxVar<String> getAnimation()
    {
        return animation;
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        animation.getValue(ctx).ifPresent(animationName ->
        {
            if(playerTarget.getPlayer() instanceof ServerPlayer serverPlayer)
            {
                sendClientAction(serverPlayer, new ClientAction(serverPlayer.getId(), ResourceLocation.parse(animationName)));
            }
        });
    }

    public static class ClientAction implements IClientAction
    {
        protected int entityId;
        protected ResourceLocation animationId;

        public ClientAction(int entityId, ResourceLocation animationId)
        {
            this.entityId = entityId;
            this.animationId = animationId;
        }

        public ClientAction()
        {
            this(0, null);
        }

        @Override
        public void writeToBuf(RegistryFriendlyByteBuf buf)
        {
            buf.writeInt(entityId);
            buf.writeResourceLocation(animationId);
        }

        @Override
        public void readFromBuf(RegistryFriendlyByteBuf buf)
        {
            entityId = buf.readInt();
            animationId = buf.readResourceLocation();
        }

        @Override
        public void execute(Level clientLevel, Player clientPlayer)
        {
            if(ModCompat.PLAYER_ANIMATOR)
            {
                PlayerAnimatorHooks.play(clientLevel, entityId, animationId);
            }
        }
    }
}
