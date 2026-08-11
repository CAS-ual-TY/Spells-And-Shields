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
 * Takes two separate animation ids, one per view - {@code thirdPerson} for the real third-person camera,
 * {@code firstPerson} for the up-close first-person render. They're independently authored files, not one
 * shared bone track - {@link PlayerAnimatorHooks} plays both simultaneously and routes between them per-frame
 * based on which view is actually rendering, since a single animation has no way to vary its own data by view.
 * Whether the off hand/its item shows in first person is likewise decided entirely by the {@code firstPerson}
 * animation's own data (does it declare a {@code left_arm}/{@code left_item} bone track or not) - see
 * {@link PlayerAnimatorHooks#play}, not a parameter of this action.
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
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("third_person_animation")).forGetter(PlayAnimationAction::getThirdPersonAnimation),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("first_person_animation")).forGetter(PlayAnimationAction::getFirstPersonAnimation)
        ).apply(instance, (activation, multiTargets, thirdPerson, firstPerson) -> new PlayAnimationAction(type, activation, multiTargets, thirdPerson, firstPerson)));
    }

    public static PlayAnimationAction make(Object activation, Object multiTargets, DynamicCtxVar<String> thirdPersonAnimation, DynamicCtxVar<String> firstPersonAnimation)
    {
        return new PlayAnimationAction(SpellActionTypes.PLAY_ANIMATION.get(), activation.toString(), multiTargets.toString(), thirdPersonAnimation, firstPersonAnimation);
    }

    protected DynamicCtxVar<String> thirdPersonAnimation;
    protected DynamicCtxVar<String> firstPersonAnimation;

    public PlayAnimationAction(SpellActionType<?> type)
    {
        super(type);
    }

    public PlayAnimationAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> thirdPersonAnimation, DynamicCtxVar<String> firstPersonAnimation)
    {
        super(type, activation, multiTargets);
        this.thirdPersonAnimation = thirdPersonAnimation;
        this.firstPersonAnimation = firstPersonAnimation;
    }

    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }

    public DynamicCtxVar<String> getThirdPersonAnimation()
    {
        return thirdPersonAnimation;
    }

    public DynamicCtxVar<String> getFirstPersonAnimation()
    {
        return firstPersonAnimation;
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        thirdPersonAnimation.getValue(ctx).ifPresent(thirdPersonName -> firstPersonAnimation.getValue(ctx).ifPresent(firstPersonName ->
        {
            if(playerTarget.getPlayer() instanceof ServerPlayer serverPlayer)
            {
                sendClientAction(serverPlayer, new ClientAction(serverPlayer.getId(), ResourceLocation.parse(thirdPersonName), ResourceLocation.parse(firstPersonName)));
            }
        }));
    }

    public static class ClientAction implements IClientAction
    {
        protected int entityId;
        protected ResourceLocation thirdPersonAnimationId;
        protected ResourceLocation firstPersonAnimationId;

        public ClientAction(int entityId, ResourceLocation thirdPersonAnimationId, ResourceLocation firstPersonAnimationId)
        {
            this.entityId = entityId;
            this.thirdPersonAnimationId = thirdPersonAnimationId;
            this.firstPersonAnimationId = firstPersonAnimationId;
        }

        public ClientAction()
        {
            this(0, null, null);
        }

        @Override
        public void writeToBuf(RegistryFriendlyByteBuf buf)
        {
            buf.writeInt(entityId);
            buf.writeResourceLocation(thirdPersonAnimationId);
            buf.writeResourceLocation(firstPersonAnimationId);
        }

        @Override
        public void readFromBuf(RegistryFriendlyByteBuf buf)
        {
            entityId = buf.readInt();
            thirdPersonAnimationId = buf.readResourceLocation();
            firstPersonAnimationId = buf.readResourceLocation();
        }

        @Override
        public void execute(Level clientLevel, Player clientPlayer)
        {
            if(ModCompat.PLAYER_ANIMATOR)
            {
                PlayerAnimatorHooks.play(clientLevel, entityId, thirdPersonAnimationId, firstPersonAnimationId);
            }
        }
    }
}
