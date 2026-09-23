package com.autoutils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Utility for detecting FreeCamera / FreeCam / FreeLook dummy entities and states.
 */
public class FreeCamUtils {

    /**
     * Checks if the camera is currently detached (FreeCam, FreeLook, or custom camera mode).
     */
    public static boolean isFreeCamActive(Minecraft client) {
        if (client == null || client.player == null) {
            return false;
        }
        Entity camera = client.getCameraEntity();
        return camera != null && camera != client.player;
    }

    /**
     * Checks if an entity is the local player, a FreeCam clone/dummy, or the camera entity.
     */
    public static boolean isFreeCamOrSelf(Entity entity, Minecraft client) {
        if (entity == null || client == null || client.player == null) {
            return true;
        }

        // 1. Direct object equality with local player
        if (entity == client.player) {
            return true;
        }

        // 2. Direct object equality with active camera entity
        if (client.getCameraEntity() != null && entity == client.getCameraEntity()) {
            return true;
        }

        // 3. Entity ID match
        if (entity.getId() == client.player.getId()) {
            return true;
        }

        // 4. UUID match (FreeCam clones share the player's UUID)
        UUID entityUuid = entity.getUUID();
        UUID playerUuid = client.player.getUUID();
        if (entityUuid != null && entityUuid.equals(playerUuid)) {
            return true;
        }

        // 5. Username match (FreeCam clones use the local player's GameProfile name)
        String localName = client.player.getGameProfile() != null ? client.player.getGameProfile().name() : null;
        if (entity instanceof Player otherPlayer && otherPlayer.getGameProfile() != null) {
            String otherName = otherPlayer.getGameProfile().name();
            if (localName != null && localName.equalsIgnoreCase(otherName)) {
                return true;
            }
        }

        String entityScoreboardName = entity.getScoreboardName();
        if (localName != null && localName.equalsIgnoreCase(entityScoreboardName)) {
            return true;
        }

        // 6. Class name detection for FreeCam / Camera / Dummy / FakePlayer entities
        String className = entity.getClass().getName().toLowerCase();
        if (className.contains("freecam") ||
            className.contains("freecamera") ||
            className.contains("freelook") ||
            className.contains("dummy") ||
            className.contains("fakeplayer") ||
            className.contains("cameraentity")) {
            return true;
        }

        // 7. Display / Scoreboard name detection
        String entityName = entity.getName().getString().toLowerCase();
        if (entityName.contains("freecam") ||
            entityName.contains("camera") ||
            entityName.contains("dummy")) {
            return true;
        }

        // 8. Server PlayerInfo verification (in multiplayer, real players always exist on tab list)
        if (!client.hasSingleplayerServer() && client.getConnection() != null) {
            if (entity instanceof Player && entityUuid != null) {
                if (client.getConnection().getPlayerInfo(entityUuid) == null) {
                    // This entity was created purely client-side by a mod (e.g. FreeCam clone)
                    return true;
                }
            }
        }

        return false;
    }
}
