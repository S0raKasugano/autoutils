package com.autoutils.module.general;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.FreeCamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Auto-Attack (Cooldown-Aware) Module for Minecraft 26.3:
 * Strikes eligible entities within reach strictly when the attack indicator reaches 100%.
 */
public class AutoAttackModule extends Module {

    public AutoAttackModule() {
        super("Auto-Attack");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().autoAttackEnabled;
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        // Do not attack if player is actively using an item, busy, or using FreeCam
        if (client.player.isHandsBusy() || client.player.isUsingItem() || FreeCamUtils.isFreeCamActive(client)) {
            return;
        }

        ModConfig config = ModConfig.get();

        // 1. Verify Attack Cooldown is 100% full if required
        if (config.attackOnlyFullCooldown) {
            float cooldownProgress = client.player.getAttackStrengthScale(0.0f);
            if (cooldownProgress < 0.95f) {
                return;
            }
        }

        // Check weapon charge prerequisites if applicable (e.g. MINIMUM_ATTACK_CHARGE)
        if (client.player.cannotAttackWithItem(client.player.getMainHandItem(), 0)) {
            return;
        }

        Entity target = null;
        Vec3 eyePos = client.player.getEyePosition();
        double range = config.attackRange;
        double rangeSq = range * range;

        // 2. Prioritize entity currently targeted by crosshair (vanilla hitResult or extended raycast)
        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity hit = ((EntityHitResult) client.hitResult).getEntity();
            if (isValidTarget(hit, config, client.player)) {
                double distSq = Math.min(hit.getBoundingBox().distanceToSqr(eyePos), client.player.distanceToSqr(hit));
                if (distSq <= rangeSq) {
                    target = hit;
                }
            }
        }

        // Raycast extended reach along player view vector if vanilla hitResult didn't yield an entity
        if (target == null) {
            Vec3 viewVec = client.player.getViewVector(1.0f);
            Vec3 reachVec = eyePos.add(viewVec.scale(range));
            AABB searchBox = client.player.getBoundingBox().expandTowards(viewVec.scale(range)).inflate(1.0);
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                    client.player,
                    eyePos,
                    reachVec,
                    searchBox,
                    entity -> isValidTarget(entity, config, client.player),
                    rangeSq
            );
            if (hit != null && hit.getEntity() != null) {
                target = hit.getEntity();
            }
        }

        // 3. Fallback: scan for closest valid entity within configured range having line of sight
        if (target == null) {
            AABB scanBox = client.player.getBoundingBox().inflate(range);
            List<Entity> nearby = client.level.getEntities(
                    client.player,
                    scanBox,
                    entity -> isValidTarget(entity, config, client.player)
            );

            target = nearby.stream()
                    .filter(e -> Math.min(e.getBoundingBox().distanceToSqr(eyePos), client.player.distanceToSqr(e)) <= rangeSq)
                    .filter(e -> client.player.hasLineOfSight(e))
                    .min(Comparator.comparingDouble(e -> Math.min(e.getBoundingBox().distanceToSqr(eyePos), client.player.distanceToSqr(e))))
                    .orElse(null);
        }

        // 4. Perform attack
        if (target != null) {
            client.gameMode.attack(client.player, target);
            client.player.resetAttackStrengthTicker();
            client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
        }
    }

    private boolean isValidTarget(Entity entity, ModConfig config, Player self) {
        if (entity == null || entity == self || !entity.isAlive() || FreeCamUtils.isFreeCamOrSelf(entity, Minecraft.getInstance())) {
            return false;
        }

        if (!entity.isAttackable() && !(entity instanceof LivingEntity)) {
            return false;
        }

        // Boss check
        if (config.targetBosses && isBoss(entity)) {
            return true;
        }

        // Never target players (PvP targeting completely disabled)
        if (entity instanceof Player) {
            return false;
        }

        // Hostile mob check: matches any Monster or Enemy (Slime, MagmaCube, Phantom, Ghast, Shulker, Breeze, etc.)
        if (config.targetHostile && (entity instanceof Enemy || entity instanceof Monster)) {
            return true;
        }

        // Passive mob check: matches any living non-enemy, non-player entity
        if (config.targetPassive && entity instanceof LivingEntity && !(entity instanceof Enemy)) {
            return true;
        }

        return false;
    }

    private boolean isBoss(Entity entity) {
        return entity instanceof EnderDragon
                || entity instanceof WitherBoss
                || entity instanceof Warden
                || entity instanceof ElderGuardian;
    }
}
