package de.jakob.game.util;

import java.util.List;
import java.util.Random;

public class Randomizer {

    private static final Random RANDOM = new Random();

    private Randomizer() {}

    public static int getRandomNumber(int start, int end) {
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        return RANDOM.nextInt((end - start) + 1) + start;
    }

    public static double getRandomDouble() {
        return RANDOM.nextDouble();
    }

    public static double getRandomDouble(double start, double end) {
        if (start > end) {
            double temp = start;
            start = end;
            end = temp;
        }

        return start + (RANDOM.nextDouble() * (end - start));
    }

    public static boolean getRandomBoolean() {
        return RANDOM.nextBoolean();
    }

    public static <T> T getRandomFromArray(T[] array) {
        if (array == null || array.length == 0) return null;
        return array[RANDOM.nextInt(array.length)];
    }

    public static <T> T getRandomFromList(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(RANDOM.nextInt(list.size()));
    }

    public static <T> T getRandomFromList(List<T> list, boolean remove) {
        if (list == null || list.isEmpty()) return null;

        int index = RANDOM.nextInt(list.size());
        T value = list.get(index);

        if (remove) {
            list.remove(index);
        }

        return value;
    }

    public static boolean chance(double percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;

        return RANDOM.nextDouble() * 100 < percent;
    }

    public static Random getRawRandom() {
        return RANDOM;
    }
}