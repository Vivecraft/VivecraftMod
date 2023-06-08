package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.provider.openvr_lwjgl.OpenVRUtil;
import org.vivecraft.client_vr.gameplay.VRMovementStyle;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.math.Angle;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import java.util.Random;

public class TeleportTracker extends Tracker
{
    private float teleportEnergy;
    private Vec3 movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);
    private Direction movementTeleportDestinationSideHit;
    public double movementTeleportProgress;
    public double movementTeleportDistance;
    private Vec3[] movementTeleportArc = new Vec3[50];
    public int movementTeleportArcSteps = 0;
    public double lastTeleportArcDisplayOffset = 0.0D;
    public VRMovementStyle vrMovementStyle;

    public TeleportTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
        this.vrMovementStyle = new VRMovementStyle(dh);
    }

    public float getTeleportEnergy()
    {
        return this.teleportEnergy;
    }

    public boolean isAiming()
    {
        return this.movementTeleportProgress > 0.0D;
    }

    public Vec3 getDestination()
    {
        return this.movementTeleportDestination;
    }

    public boolean isActive(LocalPlayer p)
    {
        if (p == null)
        {
            return false;
        }
        else if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (!p.isAlive())
        {
            return false;
        }
        else
        {
            return !p.isSleeping();
        }
    }

    public void reset(LocalPlayer player)
    {
        this.movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);
        this.movementTeleportArcSteps = 0;
        this.movementTeleportProgress = 0.0D;
    }

    public void doProcess(LocalPlayer player)
    {
        Random random = new Random();

        if (this.teleportEnergy < 100.0F)
        {
            ++this.teleportEnergy;
        }

        boolean flag = false;
        Vec3 vec3 = null;
        boolean flag1 = VivecraftVRMod.INSTANCE.keyTeleport.isDown() && this.dh.vrPlayer.isTeleportEnabled();
        boolean flag2 = this.dh.vrSettings.seated && !this.dh.vrPlayer.getFreeMove() && (player.input.forwardImpulse != 0.0F || player.input.leftImpulse != 0.0F);

        if ((flag1 || flag2) && !player.isPassenger())
        {
            vec3 = this.movementTeleportDestination;

            if (this.vrMovementStyle.teleportOnRelease)
            {
                if (((PlayerExtension) player).getMovementTeleportTimer() == 0)
                {
                    String playCustomTeleportSound = this.vrMovementStyle.startTeleportingSound;
                }

                ((PlayerExtension) player).setMovementTeleportTimer(((PlayerExtension) player).getMovementTeleportTimer() +1);

                if (((PlayerExtension) player).getMovementTeleportTimer() > 0)
                {
                    this.movementTeleportProgress = (double)((float)((PlayerExtension) player).getMovementTeleportTimer() / 1.0F);

                    if (this.movementTeleportProgress >= 1.0D)
                    {
                        this.movementTeleportProgress = 1.0D;
                    }

                    if (vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D)
                    {
                        Vec3 vec38 = this.dh.vrPlayer.vrdata_world_pre.hmd.getPosition();
                        Vec3 vec31 = vec3.add(-vec38.x, -vec38.y, -vec38.z).normalize();
                        Vec3 vec32 = player.getLookAngle();
                        Vec3 vec33 = vec32.cross(new Vec3(0.0D, 1.0D, 0.0D));
                        Vec3 vec34 = vec33.cross(vec32);

                        if (this.vrMovementStyle.airSparkles)
                        {
                            for (int i = 0; i < 3; ++i)
                            {
                                double d0 = random.nextDouble() * 1.0D + 3.5D;
                                double d1 = random.nextDouble() * 2.5D;
                                double d2 = random.nextDouble() * 4.0D - 2.0D;
                                Vec3 vec36 = new Vec3(vec38.x + vec32.x * d0, vec38.y + vec32.y * d0, vec38.z + vec32.z * d0);
                                vec36 = vec36.add(vec33.x * d2, vec33.y * d2, vec33.z * d2);
                                vec36.add(vec34.x * d1, vec34.y * d1, vec34.z * d1);
                                double d3 = -0.6D;
                            }
                        }
                    }
                }
            }
            else if (((PlayerExtension) player).getMovementTeleportTimer() >= 0 && (vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D))
            {
                if (((PlayerExtension) player).getMovementTeleportTimer() == 0)
                {
                }

                ((PlayerExtension) player).setMovementTeleportTimer(((PlayerExtension) player).getMovementTeleportTimer() + 1);
                Vec3 vec39 = player.position();
                double d6 = vec3.distanceTo(vec39);
                double d7 = (double)((PlayerExtension) player).getMovementTeleportTimer() * 1.0D / (d6 + 3.0D);

                if (((PlayerExtension) player).getMovementTeleportTimer() > 0)
                {
                    this.movementTeleportProgress = d7;

                    if (this.vrMovementStyle.destinationSparkles)
                    {
                    }

                    Vec3 vec310 = vec3.add(-player.getX(), -player.getY(), -player.getZ()).normalize();
                    Vec3 vec311 = player.getLookAngle();
                    Vec3 vec35 = vec311.cross(new Vec3(0.0D, 1.0D, 0.0D));
                    Vec3 vec312 = vec35.cross(vec311);

                    if (this.vrMovementStyle.airSparkles)
                    {
                        for (int j = 0; j < 3; ++j)
                        {
                            double d8 = random.nextDouble() * 1.0D + 3.5D;
                            double d9 = random.nextDouble() * 2.5D;
                            double d4 = random.nextDouble() * 4.0D - 2.0D;
                            Vec3 vec37 = new Vec3(player.getX() + vec311.x * d8, player.getY() + vec311.y * d8, player.getZ() + vec311.z * d8);
                            vec37 = vec37.add(vec35.x * d4, vec35.y * d4, vec35.z * d4);
                            vec37.add(vec312.x * d9, vec312.y * d9, vec312.z * d9);
                            double d5 = -0.6D;
                        }
                    }
                }
                else
                {
                    this.movementTeleportProgress = 0.0D;
                }

                if (d7 >= 1.0D)
                {
                    flag = true;
                }
            }
        }
        else
        {
            if (this.vrMovementStyle.teleportOnRelease && this.movementTeleportProgress >= 1.0D)
            {
                vec3 = this.movementTeleportDestination;
                flag = true;
            }

            ((PlayerExtension) player).setMovementTeleportTimer(0);
            this.movementTeleportProgress = 0.0D;
        }

        if (flag && vec3 != null && (vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D))
        {
            this.movementTeleportDistance = vec3.distanceTo(player.position());

            if (this.movementTeleportDistance > 0.0D && this.vrMovementStyle.endTeleportingSound != null)
            {
                boolean flag3 = true;
            }
            else
            {
                boolean flag4 = false;
            }

            Block block = null;

            if (!this.dh.vrPlayer.isTeleportSupported())
            {
                String s1 = "tp " + vec3.x + " " + vec3.y + " " + vec3.z;
                this.mc.player.connection.sendCommand(s1);
            }
            else
            {
                if (ClientNetworking.serverSupportsDirectTeleport)
                {
                	((PlayerExtension) player).setTeleported(true);
                }

                player.moveTo(vec3.x, vec3.y, vec3.z);
            }

            this.doTeleportCallback();
            ((PlayerExtension) this.mc.player).stepSound(BlockPos.containing(vec3), vec3);
        }
    }

    public void updateTeleportDestinations(GameRenderer renderer, Minecraft mc, LocalPlayer player)
    {
        mc.getProfiler().push("updateTeleportDestinations");

        if (this.vrMovementStyle.arcAiming)
        {
            this.movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);

            if (this.movementTeleportProgress > 0.0D)
            {
                this.updateTeleportArc(mc, player);
            }
        }

        mc.getProfiler().pop();
    }

    private void updateTeleportArc(Minecraft mc, LocalPlayer player)
    {
        Vec3 vec3 = dh.vrPlayer.vrdata_world_render.getController(1).getPosition();
        Vec3 vec31 = dh.vrPlayer.vrdata_world_render.getController(1).getDirection();
        Matrix4f matrix4f = dh.vr.getAimRotation(1);

        if (dh.vrSettings.seated)
        {
            vec3 = ((GameRendererExtension) mc.gameRenderer).getControllerRenderPos(0);
            vec31 = dh.vrPlayer.vrdata_world_render.getController(0).getDirection();
            matrix4f = dh.vr.getAimRotation(0);
        }

        Matrix4f matrix4f1 = Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
        matrix4f = Matrix4f.multiply(matrix4f1, matrix4f);
        Quaternion quaternion = OpenVRUtil.convertMatrix4ftoRotationQuat(matrix4f);
        Angle angle = quaternion.toEuler();
        int i = 50;
        this.movementTeleportArc[0] = new Vec3(vec3.x, vec3.y, vec3.z);
        this.movementTeleportArcSteps = 1;
        float f = 0.098F;
        Matrix4f matrix4f2 = Utils.rotationZMatrix((float)Math.toRadians((double)(-angle.getRoll())));
        Matrix4f matrix4f3 = Utils.rotationXMatrix(-2.5132742F);
        Matrix4f matrix4f4 = Matrix4f.multiply(matrix4f, matrix4f2);
        Vector3 vector3 = new Vector3(0.0F, 1.0F, 0.0F);
        Vector3 vector31 = matrix4f4.transform(vector3);
        Vec3 vec32 = vector31.negate().toVector3d();
        vec32 = vec32.scale((double)f);
        float f1 = 0.5F;
        Vec3 vec33 = new Vec3(vec31.x * (double)f1, vec31.y * (double)f1, vec31.z * (double)f1);
        Vec3 vec34 = new Vec3(vec3.x, vec3.y, vec3.z);

        for (int j = this.movementTeleportArcSteps; j < i && !((float)(j * 4) > this.teleportEnergy); ++j)
        {
            Vec3 vec35 = new Vec3(vec34.x + vec33.x, vec34.y + vec33.y, vec34.z + vec33.z);
            boolean flag = false;

            if (dh.vrSettings.seated)
            {
                flag = ((GameRendererExtension) mc.gameRenderer).isInWater();
            }
            else
            {
                flag = !mc.level.getFluidState(BlockPos.containing(vec3)).isEmpty();
            }

            BlockHitResult blockhitresult = mc.level.clip(new ClipContext(vec34, vec35, ClipContext.Block.COLLIDER, flag ? ClipContext.Fluid.ANY : ClipContext.Fluid.ANY, mc.player));

            if (blockhitresult != null && blockhitresult.getType() != HitResult.Type.MISS)
            {
                this.movementTeleportArc[j] = blockhitresult.getLocation();
                this.movementTeleportArcSteps = j + 1;
                Vec3 vec36 = vec34.subtract(vec35).normalize();
                Vec3 vec37 = new Vec3(-vec36.x * 0.02D, -vec36.y * 0.02D, -vec36.z * 0.02D);
                this.checkAndSetTeleportDestination(mc, player, vec3, blockhitresult, vec37);
                Vec3 vec38 = mc.player.position().subtract(this.movementTeleportDestination);
                double d0 = vec38.y;
                this.movementTeleportDistance = vec38.length();
                double d1 = Math.sqrt(vec38.x * vec38.x + vec38.z * vec38.z);
                boolean flag1 = true;

                if (mc.player.isShiftKeyDown() && d0 > 0.2D)
                {
                    flag1 = false;
                }

                if (!mc.player.getAbilities().mayfly && ClientNetworking.isLimitedSurvivalTeleport())
                {
                    if (ClientNetworking.getTeleportDownLimit() > 0 && d0 > (double) ClientNetworking.getTeleportDownLimit() + 0.2D)
                    {
                        flag1 = false;
                    }
                    else if (ClientNetworking.getTeleportUpLimit() > 0 && -d0 > (double) ClientNetworking.getTeleportUpLimit() * (double)((PlayerExtension) player).getMuhJumpFactor() + 0.2D)
                    {
                        flag1 = false;
                    }
                    else if (ClientNetworking.getTeleportHorizLimit() > 0 && d1 > (double) ClientNetworking.getTeleportHorizLimit() * (double)((PlayerExtension) player).getMuhSpeedFactor() + 0.2D)
                    {
                        flag1 = false;
                    }
                }

                if (!flag1)
                {
                    this.movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);
                    this.movementTeleportDistance = 0.0D;
                }

                break;
            }

            vec34 = new Vec3(vec35.x, vec35.y, vec35.z);
            this.movementTeleportArc[j] = new Vec3(vec35.x, vec35.y, vec35.z);
            this.movementTeleportArcSteps = j + 1;
            vec33 = vec33.add(vec32);
        }
    }

    private void doTeleportCallback()
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        dataholder.swingTracker.disableSwing = 3;

        if (ClientNetworking.isLimitedSurvivalTeleport())
        {
            minecraft.player.causeFoodExhaustion((float)(this.movementTeleportDistance / 16.0D * (double)1.2F));

            if (minecraft.gameMode.hasMissTime() && this.vrMovementStyle.arcAiming)
            {
                this.teleportEnergy = (float)((double)this.teleportEnergy - this.movementTeleportDistance * 4.0D);
            }
        }

        minecraft.player.fallDistance = 0.0F;
        ((PlayerExtension) minecraft.player).setMovementTeleportTimer(-1);
    }

    private boolean checkAndSetTeleportDestination(Minecraft mc, LocalPlayer player, Vec3 start, BlockHitResult collision, Vec3 reverseEpsilon)
    {
        BlockPos blockpos = collision.getBlockPos();
        BlockState blockstate = player.level().getBlockState(blockpos);

        if (!mc.level.getFluidState(blockpos).isEmpty())
        {
            Vec3 vec3 = new Vec3(collision.getLocation().x, (double)blockpos.getY(), collision.getLocation().z);
            Vec3 vec31 = vec3.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
            AABB aabb = player.getBoundingBox().move(vec31.x, vec31.y, vec31.z);
            boolean flag = mc.level.noCollision(player, aabb);

            if (!flag)
            {
                Vec3 vec32 = Vec3.atBottomCenterOf(blockpos);
                vec31 = vec32.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                aabb = player.getBoundingBox().move(vec31.x, vec31.y, vec31.z);
                flag = mc.level.noCollision(player, aabb);
            }

            float f = 0.0F;

            if (dh.vrSettings.seated)
            {
                f = 0.5F;
            }

            if (flag)
            {
                this.movementTeleportDestination = new Vec3(aabb.getCenter().x, aabb.minY + (double)f, aabb.getCenter().z);
                this.movementTeleportDestinationSideHit = collision.getDirection();
                return true;
            }
        }
        else if (collision.getDirection() != Direction.UP)
        {
            if (blockstate.getBlock() instanceof LadderBlock || blockstate.getBlock() instanceof VineBlock || blockstate.is(BlockTags.VIVECRAFT_CLIMBABLE))
            {
                Vec3 vec36 = new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
                Block block = mc.level.getBlockState(blockpos.below()).getBlock();

                if (block == blockstate.getBlock())
                {
                    vec36 = vec36.add(0.0D, -1.0D, 0.0D);
                }

                this.movementTeleportDestination = vec36.scale(1.0D);
                this.movementTeleportDestinationSideHit = collision.getDirection();
                return true;
            }

            if (!mc.player.getAbilities().mayfly && ClientNetworking.isLimitedSurvivalTeleport())
            {
                return false;
            }
        }

        double d1 = 0.0D;
        BlockPos blockpos1 = collision.getBlockPos().below();

        for (int i = 0; i < 2; ++i)
        {
            blockstate = player.level().getBlockState(blockpos1);

            if (blockstate.getCollisionShape(mc.level, blockpos1).isEmpty())
            {
                blockpos1 = blockpos1.above();
            }
            else
            {
                double d2 = blockstate.getCollisionShape(mc.level, blockpos1).max(Direction.Axis.Y);
                Vec3 vec33 = new Vec3(collision.getLocation().x, (double)blockpos1.getY() + d2, collision.getLocation().z);
                Vec3 vec34 = vec33.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                AABB aabb1 = player.getBoundingBox().move(vec34.x, vec34.y, vec34.z);
                double d0 = 0.0D;

                if (blockstate.getBlock() == Blocks.SOUL_SAND || blockstate.getBlock() == Blocks.HONEY_BLOCK)
                {
                    d0 = 0.05D;
                }

                boolean flag1 = mc.level.noCollision(player, aabb1) && !mc.level.noCollision(player, aabb1.inflate(0.0D, 0.125D + d0, 0.0D));

                if (!flag1)
                {
                    Vec3 vec35 = Vec3.upFromBottomCenterOf(blockpos1, d2);
                    vec34 = vec35.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                    aabb1 = player.getBoundingBox().move(vec34.x, vec34.y, vec34.z);
                    flag1 = mc.level.noCollision(player, aabb1) && !mc.level.noCollision(player, aabb1.inflate(0.0D, 0.125D + d0, 0.0D));
                }

                if (flag1)
                {
                    Vec3 vec37 = new Vec3(aabb1.getCenter().x, (double)blockpos1.getY() + d2, aabb1.getCenter().z);
                    this.movementTeleportDestination = vec37.scale(1.0D);
                    return true;
                }

                blockpos1 = blockpos1.above();
            }
        }

        return false;
    }

    public Vec3 getInterpolatedArcPosition(float progress)
    {
        if (this.movementTeleportArcSteps != 1 && !(progress <= 0.0F))
        {
            if (progress >= 1.0F)
            {
                return new Vec3(this.movementTeleportArc[this.movementTeleportArcSteps - 1].x, this.movementTeleportArc[this.movementTeleportArcSteps - 1].y, this.movementTeleportArc[this.movementTeleportArcSteps - 1].z);
            }
            else
            {
                float f = progress * (float)(this.movementTeleportArcSteps - 1);
                int i = (int)Math.floor((double)f);
                double d0 = this.movementTeleportArc[i + 1].x - this.movementTeleportArc[i].x;
                double d1 = this.movementTeleportArc[i + 1].y - this.movementTeleportArc[i].y;
                double d2 = this.movementTeleportArc[i + 1].z - this.movementTeleportArc[i].z;
                float f1 = f - (float)i;
                return new Vec3(this.movementTeleportArc[i].x + d0 * (double)f1, this.movementTeleportArc[i].y + d1 * (double)f1, this.movementTeleportArc[i].z + d2 * (double)f1);
            }
        }
        else
        {
            return new Vec3(this.movementTeleportArc[0].x, this.movementTeleportArc[0].y, this.movementTeleportArc[0].z);
        }
    }
}
