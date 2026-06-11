package org.dpdns.pisekpiskovec.combatupdated.capability.sanity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.InflictEntry;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;

import java.util.List;

public class MobSanityCapability implements INBTSerializable<CompoundTag> {

    public static final int MIN_SANITY = -45;
    public static final int MAX_SANITY = 45;

    private int sanity = 0;
    private boolean hasPanicked = false; // true after first panic, for turn-end reset
    private long lastKnownDay = -1;

    // --- Static accessors ---

    public static LazyOptional<MobSanityCapability> get(LivingEntity entity) {
        return entity.getCapability(MobSanityCapabilityProvider.CAPABILITY);
    }

    public static void ifPresent(LivingEntity entity, java.util.function.Consumer<MobSanityCapability> action) {
        get(entity).ifPresent(action::accept);
    }

    // --- Sanity modification ---

    public void reduce(int amount) {
        setSanity(this.sanity - amount);
    }

    public void increase(int amount) {
        setSanity(this.sanity + amount);
    }

    public void setSanity(int value) {
        this.sanity = CUMath.clamp(MIN_SANITY, value, MAX_SANITY);
    }

    public int getSanity() {
        return sanity;
    }

    public boolean hasPanicked() {
        return hasPanicked;
    }

    // --- Panic trigger ---

    /**
     * Called when sanity hits -45.
     * Check for PanicGoal; if absent, applies panic-gains to the entity itself.
     * Resets sanity to 0 in both cases.
     */
    public void triggerPanic(LivingEntity entity) {
        boolean triggeredVanillaPanic = tryVanillaPanic(entity);

        if (!triggeredVanillaPanic) {
            MobDataManager.MobData data = MobDataManager.get(entity);
            if (data.hasSanity()) {
                List<InflictEntry> panicGains = data.sanity().panicGains();
                StatusEffectCapability.ifPresent(entity, cap -> {
                    for (InflictEntry entry : panicGains) {
                        cap.apply(entry.effect(), entry.count(), entry.potency());
                    }
                });
            }
        }

        this.hasPanicked = true;
    }

    /**
     * Attempts to trigger vanilla PanicGoal via reflection.
     *
     * @return true if PanicGoal was found and triggered.
     */
    private boolean tryVanillaPanic(LivingEntity entity) {
        if (!(entity instanceof PathfinderMob pathfinder)) return false;

        try {
            // Walk the goal selector's available goal looking for PanicGoal
            var goalSelectorField = pathfinder.goalSelector.getClass().getDeclaredField("availableGoals");
            goalSelectorField.setAccessible(true);

            @SuppressWarnings("unchecked") var goals = (Iterable<?>) goalSelectorField.get(pathfinder.goalSelector);

            for (Object wrappedGoal : goals) {
                var goalField = wrappedGoal.getClass().getDeclaredField("goal");
                goalField.setAccessible(true);
                Object goal = goalField.get(wrappedGoal);

                if (goal instanceof PanicGoal) {
                    // PanicGoal.canUse() checks getLastHurtByMob() != null
                    // and getLastHurtByMobTimestanp is recent.
                    // Use self-reference as dummy attacker to both conditions
                    pathfinder.setLastHurtByMob(pathfinder);

                    // Force-start the panic goal by simulating a hurt source.
                    // PanicGoal.canUse() checks lastHurtByMob - we set a dummy
                    // hurt timestamp so it activates on next AI tick.
                    entity.invulnerableTime = 0;

                    // The goal will self-activate on the next AI tick
                    // since the entity's recentlyHurt flag is set by damage events
                    return true;
                }
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    // --- Turn End ---

    /**
     * Called on TURN_END. If mob has panicked since last turn, reset sanity,
     */
    public void onTurnEnd() {
        if (hasPanicked) {
            sanity = 0;
            hasPanicked = false;
        }
    }

    public void tick(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            long currentDay = level.getDayTime() / 24000L;
            if (lastKnownDay < 0) {
                lastKnownDay = currentDay;
                return;
            }
            if (currentDay > lastKnownDay) {
                lastKnownDay = currentDay;
                onTurnEnd();
            }
        }
    }

    // --- NBT ---


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sanity", sanity);
        tag.putBoolean("has_panicked", hasPanicked);
        tag.putLong("last_known_day", lastKnownDay);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        sanity = nbt.getInt("sanity");
        hasPanicked = nbt.getBoolean("has_panicked");
        lastKnownDay = nbt.getLong("last_known_day");
    }
}
