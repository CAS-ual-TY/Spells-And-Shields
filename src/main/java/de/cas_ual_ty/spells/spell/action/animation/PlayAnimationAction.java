package de.cas_ual_ty.spells.spell.action.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.animation.PlayerAnimation;
import de.cas_ual_ty.spells.client.animation.PlayerAnimationClient;
import de.cas_ual_ty.spells.registers.PlayerAnimations;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.SpellsCodecs;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Plays a {@link PlayerAnimation} on player targets, synced to every client tracking them (plus themselves).
 * A new animation on the same entity fully replaces whatever was already playing - no queueing, no blending.
 */
public class PlayAnimationAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<PlayAnimationAction> makeCodec(SpellActionType<PlayAnimationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                SpellsCodecs.PLAYER_ANIMATION.fieldOf("animation").forGetter(PlayAnimationAction::getAnimation)
        ).apply(instance, (activation, targets, animation) -> new PlayAnimationAction(type, activation, targets, animation)));
    }

    public static PlayAnimationAction make(Object activation, Object targets, Holder<PlayerAnimation> animation)
    {
        return new PlayAnimationAction(SpellActionTypes.PLAY_ANIMATION.get(), activation.toString(), targets.toString(), animation);
    }

    protected Holder<PlayerAnimation> animation;

    public PlayAnimationAction(SpellActionType<?> type)
    {
        super(type);
    }

    public PlayAnimationAction(SpellActionType<?> type, String activation, String targets, Holder<PlayerAnimation> animation)
    {
        super(type, activation, targets);
        this.animation = animation;
    }

    public Holder<PlayerAnimation> getAnimation()
    {
        return animation;
    }

    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget target)
    {
        if(target.getPlayer() instanceof ServerPlayer serverPlayer)
        {
            ResourceLocation animationId = PlayerAnimations.getRegistry(ctx.getLevel()).getKey(animation.value());

            if(animationId != null)
            {
                sendClientAction(serverPlayer, new ClientAction(serverPlayer.getId(), animationId));
            }
        }
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
            Entity e = clientLevel.getEntity(entityId);

            if(e != null)
            {
                Registry<PlayerAnimation> registry = PlayerAnimations.getRegistry(clientLevel);
                PlayerAnimation animation = registry.get(animationId);

                if(animation != null)
                {
                    PlayerAnimationClient.play(entityId, animation);
                }
            }
        }
    }
}
