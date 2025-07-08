/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicates
 */
package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public final class EntitySelector {
    public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
    public static final Predicate<Entity> LIVING_ENTITY_STILL_ALIVE = entity -> entity.isAlive() && entity instanceof LivingEntity;
    public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = entity -> entity.isAlive() && !entity.isVehicle() && !entity.isPassenger();
    public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = entity -> entity instanceof Container && entity.isAlive();
    public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = entity -> {
        if (!(entity instanceof Player)) return true;
        Player player = (Player)entity;
        if (entity.isSpectator()) return false;
        if (player.isCreative()) return false;
        return true;
    };
    public static final Predicate<Entity> NO_SPECTATORS = entity -> !entity.isSpectator();
    public static final Predicate<Entity> CAN_BE_COLLIDED_WITH = NO_SPECTATORS.and(entity -> entity.canBeCollidedWith(null));
    public static final Predicate<Entity> CAN_BE_PICKED = NO_SPECTATORS.and(Entity::isPickable);

    private EntitySelector() {
    }

    public static Predicate<Entity> withinDistance(double d, double d2, double d3, double d4) {
        double d5 = d4 * d4;
        return entity -> entity != null && entity.distanceToSqr(d, d2, d3) <= d5;
    }

    public static Predicate<Entity> pushableBy(Entity entity) {
        Team.CollisionRule collisionRule;
        PlayerTeam playerTeam = entity.getTeam();
        Team.CollisionRule collisionRule2 = collisionRule = playerTeam == null ? Team.CollisionRule.ALWAYS : ((Team)playerTeam).getCollisionRule();
        if (collisionRule == Team.CollisionRule.NEVER) {
            return Predicates.alwaysFalse();
        }
        return NO_SPECTATORS.and(entity2 -> {
            boolean bl;
            Team.CollisionRule collisionRule2;
            Object object;
            if (!entity2.isPushable()) {
                return false;
            }
            if (!(!entity.level().isClientSide || entity2 instanceof Player && ((Player)(object = (Player)entity2)).isLocalPlayer())) {
                return false;
            }
            object = entity2.getTeam();
            Team.CollisionRule collisionRule3 = collisionRule2 = object == null ? Team.CollisionRule.ALWAYS : ((Team)object).getCollisionRule();
            if (collisionRule2 == Team.CollisionRule.NEVER) {
                return false;
            }
            boolean bl2 = bl = playerTeam != null && playerTeam.isAlliedTo((Team)object);
            if ((collisionRule == Team.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == Team.CollisionRule.PUSH_OWN_TEAM) && bl) {
                return false;
            }
            return collisionRule != Team.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != Team.CollisionRule.PUSH_OTHER_TEAMS || bl;
        });
    }

    public static Predicate<Entity> notRiding(Entity entity) {
        return entity2 -> {
            while (entity2.isPassenger()) {
                if ((entity2 = entity2.getVehicle()) != entity) continue;
                return false;
            }
            return true;
        };
    }
}

