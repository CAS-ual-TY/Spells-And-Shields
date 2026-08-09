package de.cas_ual_ty.spells.mixin;

import de.cas_ual_ty.spells.animation.AnimationSample;
import de.cas_ual_ty.spells.client.animation.PlayerAnimationClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the active third-person {@link de.cas_ual_ty.spells.animation.PlayerAnimation} (if any) on top of
 * vanilla's own procedural pose. Injected at the tail of {@code setupAnim} (after vanilla, and after
 * {@link PlayerModel}'s own cape/cloak logic, both of which already ran by then) so this fully overwrites
 * whichever parts the active animation actually touches, for its own duration - see
 * {@link PlayerAnimationClient}.
 * <p>
 * Targets {@link PlayerModel} specifically (not the more general {@link HumanoidModel}), so this never affects
 * any other humanoid mob (zombies, villagers, etc.) - only the player render pipeline actually uses
 * {@code PlayerModel}. Extends {@code HumanoidModel} (the target's own superclass) rather than {@code PlayerModel}
 * itself - a mixin class must never extend its own {@code @Mixin} target, that's a self-referential merge Mixin
 * doesn't support correctly. The sleeve/pants/jacket fields ({@code PlayerModel}-only, not on
 * {@code HumanoidModel}) are reached via {@code @Shadow} instead - see {@link #spellsAndShields$applyAnimation}.
 * <p>
 * {@code setupAnim} isn't only called for the real third-person render: {@code PlayerRenderer.renderHand}
 * (backing the first-person bare-arm mesh, via {@code renderRightHand}/{@code renderLeftHand}) calls the exact
 * same method with all five animation parameters hardcoded to {@code 0.0F}, purely to reset the model to a
 * neutral pose before positioning that arm entirely via {@code PoseStack} instead. A real third-person render
 * essentially never has {@code ageInTicks} (let alone all five params) land on exactly {@code 0.0F}, so that's
 * used below as a cheap, reliable signal to skip applying the third-person animation on that call - otherwise
 * it contaminates the same arm mesh {@link de.cas_ual_ty.spells.client.animation.PlayerAnimationRenderHooks}
 * separately (and correctly) animates for first person, compounding into visible garbage.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T>
{
    @Shadow
    @Final
    private ModelPart leftSleeve;

    @Shadow
    @Final
    private ModelPart rightSleeve;

    @Shadow
    @Final
    private ModelPart leftPants;

    @Shadow
    @Final
    private ModelPart rightPants;

    @Shadow
    @Final
    private ModelPart jacket;

    private PlayerModelMixin(ModelPart root)
    {
        super(root);
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void spellsAndShields$applyAnimation(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci)
    {
        if(!(entity instanceof Player))
        {
            return;
        }

        if(limbSwing == 0F && limbSwingAmount == 0F && ageInTicks == 0F && netHeadYaw == 0F && headPitch == 0F)
        {
            // the first-person bare-arm reset call - see the class doc above
            return;
        }

        int entityId = entity.getId();
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        apply(this.head, PlayerAnimationClient.sampleThirdPerson(entityId, "head", partialTick));
        apply(this.body, PlayerAnimationClient.sampleThirdPerson(entityId, "body", partialTick));
        apply(this.rightArm, PlayerAnimationClient.sampleThirdPerson(entityId, "right_arm", partialTick));
        apply(this.leftArm, PlayerAnimationClient.sampleThirdPerson(entityId, "left_arm", partialTick));
        apply(this.rightLeg, PlayerAnimationClient.sampleThirdPerson(entityId, "right_leg", partialTick));
        apply(this.leftLeg, PlayerAnimationClient.sampleThirdPerson(entityId, "left_leg", partialTick));

        // PlayerModel's own setupAnim already copied the pre-animation pose into these overlay parts earlier
        // in the same method call (rightSleeve.copyFrom(rightArm) etc.) - that snapshot is now stale since we
        // just changed the base parts above, so it has to happen again with our changes included, or the
        // sleeves/pants/jacket visibly lag behind the limb they're supposed to follow.
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
    }

    /**
     * {@code part.x/y/z} is the part's fixed pivot offset from the model's own mesh definition (eg. where the
     * shoulder sits relative to the body) - vanilla's own {@code setupAnim} never touches it, only rotations.
     * So the sample has to be composed on top of whatever's already there (added for translate/rotate,
     * multiplied for scale) rather than assigned outright, or it would snap the part's pivot to the parent's
     * origin instead of animating from its actual rest position.
     * <p>
     * {@code ModelPart} only has separate {@code xRot}/{@code yRot}/{@code zRot} fields, not a quaternion, so
     * {@link AnimationSample#rotate()} has to be decomposed into Euler angles here - {@code sample.rotate().x()}
     * etc. would be the quaternion's own raw imaginary components, not Euler angles, hence
     * {@code getEulerAnglesXYZ} instead.
     */
    private static void apply(ModelPart part, @Nullable AnimationSample sample)
    {
        if(sample == null)
        {
            return;
        }

        Vector3f euler = sample.rotate().getEulerAnglesXYZ(new Vector3f());

        part.x += (float) sample.translate().x();
        part.y += (float) sample.translate().y();
        part.z += (float) sample.translate().z();
        part.xRot += euler.x();
        part.yRot += euler.y();
        part.zRot += euler.z();
        part.xScale *= (float) sample.scale().x();
        part.yScale *= (float) sample.scale().y();
        part.zScale *= (float) sample.scale().z();
    }
}
