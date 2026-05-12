package com.crystalai.client.utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AICore {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static float[] serverRotation = new float[] { 0, 0 };
    private static boolean rotationSmooth = true;
    private static final float[] previousRotation = new float[] { 0, 0 };

    public static double calculateDamage(Vec3d crystalPos, PlayerEntity entity) {
        if (entity == null || mc.world == null)
            return 0;
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        double distance = entityPos.distanceTo(crystalPos);

        if (distance > 12.0)
            return 0;

        double impact = (1.0 - (distance / 12.0));
        double exposure = calculateExposure(crystalPos, entity);
        double damage = ((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0) * exposure;

        if (entity.getAbsorptionAmount() > 0) {
            damage = Math.max(0, damage - entity.getAbsorptionAmount() * 0.5);
        }

        return damage;
    }

    private static double calculateExposure(Vec3d explosionPos, Entity entity) {
        if (mc.world == null)
            return 0;
        Box box = entity.getBoundingBox();

        // Tạo các điểm mẫu trên bounding box của người chơi
        Vec3d[] samplePoints = {
                new Vec3d(box.minX, box.minY, box.minZ), // góc dưới trước trái
                new Vec3d(box.maxX, box.minY, box.minZ), // góc dưới trước phải
                new Vec3d(box.minX, box.minY, box.maxZ), // góc dưới sau trái
                new Vec3d(box.maxX, box.minY, box.maxZ), // góc dưới sau phải
                new Vec3d(box.minX, box.maxY, box.minZ), // góc trên trước trái
                new Vec3d(box.maxX, box.maxY, box.minZ), // góc trên trước phải
                new Vec3d(box.minX, box.maxY, box.maxZ), // góc trên sau trái
                new Vec3d(box.maxX, box.maxY, box.maxZ), // góc trên sau phải
                new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2) // trung tâm
        };

        int visiblePoints = 0;
        for (Vec3d point : samplePoints) {
            if (!isRayBlocked(explosionPos, point)) {
                visiblePoints++;
            }
        }

        return (double) visiblePoints / samplePoints.length;
    }

    private static boolean isRayBlocked(Vec3d from, Vec3d to) {
        if (mc.world == null)
            return true;
        RaycastContext context = new RaycastContext(
                from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player);
        HitResult result = mc.world.raycast(context);
        return result.getType() == HitResult.Type.BLOCK;
    }

    public static void rotate(Vec3d pos) {
        if (mc.player == null)
            return;

        double dx = pos.x - mc.player.getX();
        double dy = pos.y - mc.player.getEyeY();
        double dz = pos.z - mc.player.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        if (rotationSmooth) {
            smoothRotate(yaw, pitch);
        } else {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }

        serverRotation[0] = yaw;
        serverRotation[1] = pitch;
    }

    private static void smoothRotate(float targetYaw, float targetPitch) {
        float yawDiff = targetYaw - previousRotation[0];
        float pitchDiff = targetPitch - previousRotation[1];

        yawDiff = normalizeAngle(yawDiff);
        pitchDiff = normalizeAngle(pitchDiff);

        float speed = 15.0f;
        float newYaw = previousRotation[0] + yawDiff * (speed / 180.0f);
        float newPitch = previousRotation[1] + pitchDiff * (speed / 180.0f);

        // Clamp pitch để tránh bug camera giật
        newPitch = Math.max(-90, Math.min(90, newPitch));

        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);

        previousRotation[0] = newYaw;
        previousRotation[1] = newPitch;
    }

    private static float normalizeAngle(float angle) {
        while (angle <= -180)
            angle += 360;
        while (angle > 180)
            angle -= 360;
        return angle;
    }

    public static float[] getServerRotation() {
        return serverRotation;
    }

    public static void setRotationSmooth(boolean smooth) {
        rotationSmooth = smooth;
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        if (mc.world == null)
            return false;

        RaycastContext context = new RaycastContext(
                from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player);

        HitResult result = mc.world.raycast(context);
        return result.getType() != HitResult.Type.BLOCK;
    }

    public static boolean isPlaceable(BlockPos pos) {
        if (mc.world == null || mc.player == null)
            return false;

        // Chỉ cho phép đặt trên Obsidian hoặc Bedrock
        if (!mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) &&
                !mc.world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
            return false;
        }

        BlockPos up = pos.up();
        if (!mc.world.isAir(up) && !mc.world.getBlockState(up).isReplaceable()) {
            return false;
        }

        BlockPos up2 = pos.up(2);
        if (!mc.world.isAir(up2) && !mc.world.getBlockState(up2).isReplaceable()) {
            return false;
        }

        // Chỉ cho đặt khi KHÔNG có entity nào đứng ở đó
        return mc.world.getOtherEntities(null, new Box(up)).isEmpty();
    }

    public static BlockPos getPlacePosition(BlockPos targetPos) {
        if (mc.world == null || mc.player == null)
            return null;

        BlockPos[] positions = {
                targetPos.north(), targetPos.south(), targetPos.east(), targetPos.west(),
                targetPos.up().north(), targetPos.up().south(), targetPos.up().east(), targetPos.up().west(),
                targetPos.down()
        };

        BlockPos bestPos = null;
        double bestScore = -1;

        for (BlockPos pos : positions) {
            if (isPlaceable(pos)) {
                double distance = mc.player.getBlockPos().getSquaredDistance(pos);
                double score = 1000 - distance;
                if (score > bestScore) {
                    bestScore = score;
                    bestPos = pos;
                }
            }
        }

        return bestPos;
    }

    public static Direction getPlaceSide(BlockPos pos) {
        if (mc.world == null || mc.player == null)
            return Direction.UP;

        Direction[] sides = { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

        for (Direction side : sides) {
            BlockPos neighbor = pos.offset(side);
            if (!mc.world.getBlockState(neighbor).isAir()) {
                return side.getOpposite();
            }
        }

        return Direction.UP;
    }

    public static BlockHitResult getHitResult(BlockPos pos) {
        Direction side = getPlaceSide(pos);
        Vec3d hitPos = Vec3d.ofCenter(pos).add(Vec3d.of(side.getVector()).multiply(0.5));
        return new BlockHitResult(hitPos, side, pos, false);
    }

    public static int getArmorValue(ItemStack stack) {
        if (stack.isEmpty())
            return 0;
        return 0; // ArmorItem not available in 1.21.11
    }

    public static double getSpeed(PlayerEntity entity) {
        if (entity == null)
            return 0;
        Vec3d velocity = entity.getVelocity();
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    public static Vec3d predictPosition(Entity entity, int ticks) {
        if (entity == null)
            return Vec3d.ZERO;
        Vec3d pos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        Vec3d velocity = entity.getVelocity();

        return pos.add(velocity.multiply(ticks));
    }

    public static boolean isMoving(Entity entity) {
        if (entity == null)
            return false;
        Vec3d velocity = entity.getVelocity();
        return Math.abs(velocity.x) > 0.01 || Math.abs(velocity.z) > 0.01;
    }

    public static double getDistanceToEntity(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null)
            return Double.MAX_VALUE;
        Vec3d pos1 = new Vec3d(entity1.getX(), entity1.getY(), entity1.getZ());
        Vec3d pos2 = new Vec3d(entity2.getX(), entity2.getY(), entity2.getZ());
        return pos1.distanceTo(pos2);
    }

    public static boolean isInAttackRange(Entity target) {
        if (mc.player == null || target == null)
            return false;
        return mc.player.distanceTo(target) <= 6.0;
    }

    public static boolean canReach(BlockPos pos) {
        if (mc.player == null)
            return false;
        double distance = mc.player.getBlockPos().getSquaredDistance(pos);
        return distance <= 36.0; // 6 blocks squared
    }

    public static float getHealth(PlayerEntity entity) {
        if (entity == null)
            return 0;
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static boolean isLowHealth(PlayerEntity entity) {
        return getHealth(entity) < 10.0f;
    }

    public static boolean isCriticalHit(PlayerEntity player) {
        if (player == null || mc.world == null)
            return false;
        return !player.isOnGround() &&
                player.getVelocity().y < 0 &&
                !mc.world.getBlockState(player.getBlockPos().down()).isAir();
    }

    public static void resetRotation() {
        serverRotation[0] = mc.player != null ? mc.player.getYaw() : 0;
        serverRotation[1] = mc.player != null ? mc.player.getPitch() : 0;
        previousRotation[0] = serverRotation[0];
        previousRotation[1] = serverRotation[1];
    }

    public static boolean hasLineOfSight(Vec3d from, Vec3d to) {
        if (mc.world == null)
            return false;
        RaycastContext context = new RaycastContext(
                from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player);
        HitResult result = mc.world.raycast(context);
        return result.getType() == HitResult.Type.MISS;
    }

    public static Vec3d getBestAttackPosition(Entity target) {
        if (target == null || mc.player == null)
            return Vec3d.ZERO;
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());

        Vec3d direction = targetPos.subtract(playerPos).normalize();
        return targetPos.subtract(direction.multiply(2.5));
    }

    public static boolean isSafeToPlaceCrystal(BlockPos pos) {
        if (mc.world == null || mc.player == null)
            return false;

        Vec3d crystalPos = Vec3d.ofCenter(pos);
        double selfDamage = calculateDamage(crystalPos, mc.player);
        return selfDamage < 4.0;
    }

    public static BlockPos findBestObsidianPlace(BlockPos targetPos) {
        if (mc.world == null || mc.player == null)
            return null;

        BlockPos[] positions = {
                targetPos.down(), targetPos.north(), targetPos.south(),
                targetPos.east(), targetPos.west()
        };

        for (BlockPos pos : positions) {
            if (mc.world.getBlockState(pos).isAir() ||
                    mc.world.getBlockState(pos).isReplaceable()) {
                return pos;
            }
        }

        return null;
    }

    public static double calculateComboPotential(Entity target) {
        if (target == null || !(target instanceof PlayerEntity))
            return 0;
        PlayerEntity player = (PlayerEntity) target;

        double health = getHealth(player);
        double speed = getSpeed(player);
        boolean moving = isMoving(target);
        boolean lowHealth = isLowHealth(player);

        double potential = 0;
        potential += (100 - health) * 0.5;
        potential += speed * 10;
        potential += moving ? 20 : 0;
        potential += lowHealth ? 30 : 0;

        return potential;
    }

    public static boolean shouldSwitchTarget(Entity currentTarget, Entity newTarget) {
        if (currentTarget == null)
            return true;
        if (newTarget == null)
            return false;

        double currentPotential = calculateComboPotential(currentTarget);
        double newPotential = calculateComboPotential(newTarget);

        double currentDist = mc.player.distanceTo(currentTarget);
        double newDist = mc.player.distanceTo(newTarget);

        return newPotential > currentPotential + 10 && newDist < currentDist + 2;
    }
}
