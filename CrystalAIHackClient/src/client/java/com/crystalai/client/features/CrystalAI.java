package com.crystalai.client.features;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.crystalai.client.utils.AICore;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CrystalAI {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;
    private PlayerEntity target;
    private int actionCooldown;
    private int comboCount;
    private boolean isAggressive;
    private int switchCooldown;
    private int placeDelay;
    private int breakDelay;
    private final Queue<Vec3d> predictedPositions = new LinkedList<>();
    private final Map<BlockPos, Long> placedCrystals = new HashMap<>();
    private boolean wTapEnabled = true;
    private boolean sTapEnabled = true;
    private int wTapCooldown;
    private int sTapCooldown;

    public void onTick() {
        if (mc.player == null || mc.world == null)
            return;
        if (actionCooldown > 0)
            actionCooldown--;
        if (switchCooldown > 0)
            switchCooldown--;
        if (placeDelay > 0)
            placeDelay--;
        if (breakDelay > 0)
            breakDelay--;
        if (wTapCooldown > 0)
            wTapCooldown--;
        if (sTapCooldown > 0)
            sTapCooldown--;

        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        target = findBestTarget();

        updatePredictedPositions();
        cleanupOldCrystals();

        if (health <= 8.0f) {
            activateDefensiveMode();
            return;
        }

        if (target != null && target.isAlive()) {
            double distance = mc.player.distanceTo(target);
            isAggressive = health > 15.0f && distance < 8.0f;

            if (isAggressive) {
                performAggressiveCombo();
            } else {
                performTacticalCombat();
            }
        } else {
            resetCombatState();
        }

        manageHotbar();
        manageArmor();
    }

    private void updatePredictedPositions() {
        if (target != null && target.isAlive()) {
            Vec3d currentPos = new Vec3d(target.getX(), target.getY(), target.getZ());
            Vec3d velocity = target.getVelocity();

            for (int i = 1; i <= 5; i++) {
                Vec3d predicted = currentPos.add(velocity.multiply(i * 0.1));
                predictedPositions.offer(predicted);
                if (predictedPositions.size() > 10)
                    predictedPositions.poll();
            }
        }
    }

    private void cleanupOldCrystals() {
        long currentTime = System.currentTimeMillis();
        placedCrystals.entrySet().removeIf(
                entry -> currentTime - entry.getValue() > 5000 || mc.world.getBlockState(entry.getKey()).isAir());
    }

    private void activateDefensiveMode() {
        ensureTotemInOffhand();
        eatGapIfPossible();

        if (target != null && mc.player.distanceTo(target) < 4.0f) {
            performEmergencyEscape();
        }
    }

    private void performAggressiveCombo() {
        if (wTapEnabled && wTapCooldown == 0) {
            performWTap();
            wTapCooldown = 10;
        }

        if (sTapEnabled && sTapCooldown == 0) {
            performSTap();
            sTapCooldown = 8;
        }

        doAdvancedCrystalCombat();
        performSprintReset();
    }

    private void performTacticalCombat() {
        if (mc.player.distanceTo(target) > 6.0f && actionCooldown == 0) {
            performSmartPearl();
            actionCooldown = 25;
        }

        doAdvancedCrystalCombat();
        maintainOptimalDistance();
    }

    private void performWTap() {
        if (!mc.player.isSprinting())
            return;
        mc.options.sprintKey.setPressed(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.options.sprintKey.setPressed(true);
    }

    private void performSTap() {
        if (!mc.player.isSprinting())
            return;
        mc.options.backKey.setPressed(true);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.options.backKey.setPressed(false);
    }

    private void performSprintReset() {
        if (mc.player.isSprinting() && actionCooldown == 0) {
            mc.options.sprintKey.setPressed(false);
            actionCooldown = 3;
            mc.options.sprintKey.setPressed(true);
        }
    }

    private void doAdvancedCrystalCombat() {
        EndCrystalEntity bestCrystal = findBestCrystalToBreak();
        if (bestCrystal != null && breakDelay == 0) {
            double predictedDamage = AICore.calculateDamage(
                    new Vec3d(bestCrystal.getX(), bestCrystal.getY(), bestCrystal.getZ()), target);

            if (predictedDamage >= 5.0 || comboCount >= 2) {
                breakCrystal(bestCrystal);
                breakDelay = 4;
                comboCount++;
            }
        }

        BlockPos bestPlace = findBestCrystalPlacement();
        if (bestPlace != null && placeDelay == 0) {
            placeCrystal(bestPlace);
            placeDelay = 6;
            placedCrystals.put(bestPlace, System.currentTimeMillis());
        }

        ensureObsidianPlacement();
    }

    private EndCrystalEntity findBestCrystalToBreak() {
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        return mc.world.getEntitiesByClass(EndCrystalEntity.class,
                Box.of(playerPos, 12, 12, 12),
                c -> mc.player.distanceTo(c) <= 6.0f).stream()
                .max(Comparator
                        .comparingDouble(c -> AICore.calculateDamage(new Vec3d(c.getX(), c.getY(), c.getZ()), target)))
                .orElse(null);
    }

    private BlockPos findBestCrystalPlacement() {
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos targetPos = target.getBlockPos();

        BlockPos bestPos = null;
        double maxScore = 0;

        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (!canPlaceCrystal(pos))
                        continue;

                    double damage = AICore.calculateDamage(Vec3d.ofCenter(pos), target);
                    double selfDamage = AICore.calculateDamage(Vec3d.ofCenter(pos), mc.player);
                    double distance = pos.getSquaredDistance(playerPos);

                    double score = damage - (selfDamage * 0.5) - (distance * 0.1);
                    if (isPredictedHit(pos))
                        score += 2.0;

                    if (score > maxScore) {
                        maxScore = score;
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private boolean isPredictedHit(BlockPos pos) {
        Vec3d crystalPos = Vec3d.ofCenter(pos);
        return predictedPositions.stream()
                .anyMatch(pred -> pred.distanceTo(crystalPos) < 1.5);
    }

    private void breakCrystal(EndCrystalEntity crystal) {
        AICore.rotate(new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ()));
        mc.interactionManager.attackEntity(mc.player, crystal);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void placeCrystal(BlockPos pos) {
        if (!switchToItem(Items.END_CRYSTAL))
            return;
        AICore.rotate(Vec3d.ofCenter(pos).add(0, 0.5, 0));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) &&
                !mc.world.getBlockState(pos).isOf(Blocks.BEDROCK))
            return false;
        return mc.world.isAir(pos.up()) && mc.world.isAir(pos.up(2)) &&
                mc.world.getOtherEntities(null, new Box(pos.up())).isEmpty() &&
                !placedCrystals.containsKey(pos);
    }

    private void ensureObsidianPlacement() {
        BlockPos targetFeet = target.getBlockPos().down();
        if (!mc.world.getBlockState(targetFeet).isOf(Blocks.OBSIDIAN) &&
                !mc.world.getBlockState(targetFeet).isOf(Blocks.BEDROCK)) {
            placeObsidian(targetFeet);
        }
    }

    private void placeObsidian(BlockPos pos) {
        if (!switchToItem(Items.OBSIDIAN))
            return;
        AICore.rotate(Vec3d.ofCenter(pos));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void performSmartPearl() {
        if (!switchToItem(Items.ENDER_PEARL))
            return;
        Vec3d bestPosition = calculateBestPearlPosition();
        AICore.rotate(bestPosition);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private Vec3d calculateBestPearlPosition() {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d velocity = target.getVelocity();
        double distance = mc.player.distanceTo(target);

        Vec3d predicted = targetPos.add(velocity.multiply(distance * 0.15));
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        return predicted.add(predicted.subtract(playerPos).normalize().multiply(3));
    }

    private void maintainOptimalDistance() {
        double distance = mc.player.distanceTo(target);
        if (distance > 5.0f) {
            mc.options.forwardKey.setPressed(true);
        } else if (distance < 3.0f) {
            mc.options.backKey.setPressed(true);
        }
    }

    private void performEmergencyEscape() {
        if (!switchToItem(Items.ENDER_PEARL))
            return;
        mc.player.setYaw(mc.player.getYaw() + 180);
        mc.player.setPitch(-10);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        actionCooldown = 20;
    }

    private void resetCombatState() {
        comboCount = 0;
        predictedPositions.clear();
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
    }

    private PlayerEntity findBestTarget() {
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && !p.isSpectator())
                .filter(p -> mc.player.distanceTo(p) <= 20.0f)
                .max(Comparator.comparingDouble(p -> {
                    double distance = mc.player.distanceTo(p);
                    double health = p.getHealth() + p.getAbsorptionAmount();
                    return (100.0 - health) - (distance * 2);
                })).orElse(null);
    }

    private boolean switchToItem(net.minecraft.item.Item item) {
        if (switchCooldown > 0)
            return false;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) {
                mc.player.getInventory().setSelectedSlot(i);
                switchCooldown = 2;
                return true;
            }
        }
        return false;
    }

    private void manageHotbar() {
        if (!mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) &&
                !mc.player.getMainHandStack().isOf(Items.OBSIDIAN)) {
            if (mc.player.getInventory().count(Items.END_CRYSTAL) > 0) {
                switchToItem(Items.END_CRYSTAL);
            } else if (mc.player.getInventory().count(Items.OBSIDIAN) > 0) {
                switchToItem(Items.OBSIDIAN);
            }
        }
    }

    private void manageArmor() {
        // Auto-armor disabled due to API changes in 1.21.11
    }

    private void ensureTotemInOffhand() {
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING))
            return;
        for (int i = 0; i < 45; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                int slot = i < 9 ? i + 36 : i;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP,
                        mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP,
                        mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP,
                        mc.player);
                return;
            }
        }
    }

    private void eatGapIfPossible() {
        if (switchToItem(Items.ENCHANTED_GOLDEN_APPLE) || switchToItem(Items.GOLDEN_APPLE)) {
            mc.options.useKey.setPressed(true);
        }
    }

    public void onDisable() {
        resetCombatState();
        mc.options.useKey.setPressed(false);
        mc.options.sprintKey.setPressed(false);
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
    }

    public void toggle() {
        enabled = !enabled;
        if (!enabled) {
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
