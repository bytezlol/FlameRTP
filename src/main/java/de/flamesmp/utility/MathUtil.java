package de.flamesmp.utility;

import de.flamesmp.config.WorldProfile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {

    private MathUtil() {
        throw new AssertionError("No instances.");
    }

    public static int @NotNull [] randomPoint(final @NotNull WorldProfile profile) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        if (profile.getShape() == WorldProfile.Shape.CIRCLE) {
            final double angle = random.nextDouble(0, Math.PI * 2);
            final double radius = Math.sqrt(random.nextDouble()) * (profile.getMaxRadius() - profile.getMinRadius()) + profile.getMinRadius();

            final int x = (int) (profile.getCenterX() + Math.cos(angle) * radius);
            final int z = (int) (profile.getCenterZ() + Math.sin(angle) * radius);
            return new int[]{x, z};
        }

        // random sign biased to outer ring via min radius
        final int min = profile.getMinRadius();
        final int max = profile.getMaxRadius();

        int x = random.nextInt(min, max + 1);
        int z = random.nextInt(min, max + 1);
        if (random.nextBoolean()) x = -x;
        if (random.nextBoolean()) z = -z;

        return new int[]{profile.getCenterX() + x, profile.getCenterZ() + z};
    }
}