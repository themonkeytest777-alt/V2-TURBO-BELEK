package com.turbominer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.Vec3d;

/**
 * Mode ghost : le joueur traverse les blocs (noclip) et vole librement dans la direction du regard.
 * Aucun paquet de mouvement n'est envoye au serveur pendant le ghost (voir ClientPlayerEntityMixin).
 * Au 2e appui, tu restes la ou tu es (nouvelle position) : le jeu envoie alors cette position au serveur.
 */
public final class Ghost {
    public static boolean active = false;

    private static Vec3d origin;
    private static ClientPlayerEntity owner;

    private Ghost() {}

    public static void toggle() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        if (!active) {
            origin = p.getPos();
            owner = p;
            active = true;
        } else {
            exit(p);
        }
    }

    private static void exit(ClientPlayerEntity p) {
        active = false;
        p.noClip = false;
        p.setVelocity(Vec3d.ZERO);
        p.fallDistance = 0;
        origin = null;
        owner = null;
    }

    public static void reset() {
        active = false;
        origin = null;
        owner = null;
    }

    /** Appele a la fin de chaque tick client. */
    public static void tick(ClientPlayerEntity p) {
        if (!active) return;
        if (owner != p || p.isDead()) {
            reset();
            return;
        }

        p.noClip = true;
        p.fallDistance = 0;
        PlayerAbilities ab = p.getAbilities();
        ab.allowFlying = true;
        ab.flying = true;

        Input in = p.input;
        double f = (in.pressingForward ? 1 : 0) - (in.pressingBack ? 1 : 0);
        double s = (in.pressingLeft ? 1 : 0) - (in.pressingRight ? 1 : 0);
        double vy = (in.jumping ? 1 : 0) - (in.sneaking ? 1 : 0);

        Vec3d look = p.getRotationVec(1.0F);
        double yaw = Math.toRadians(p.getYaw());
        Vec3d left = new Vec3d(Math.cos(yaw), 0, Math.sin(yaw));

        Vec3d v = look.multiply(f).add(left.multiply(s)).add(0, vy, 0);
        if (v.lengthSquared() > 1.0E-6) {
            if (v.lengthSquared() > 1.0) v = v.normalize();
            v = v.multiply(0.55 * ModConfig.flyMultiplier);
        } else {
            v = Vec3d.ZERO;
        }
        p.setVelocity(v);
    }
}
