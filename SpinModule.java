package com.example.addon;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class SpinModule extends Module {
    public enum Direction { Right, Left, Random }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRandom = settings.createGroup("Unregelmäßigkeiten");

    // --- Allgemein ---
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Grundgeschwindigkeit in Grad pro Tick.")
        .defaultValue(30)
        .min(1)
        .sliderMax(180)
        .build()
    );

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("Drehrichtung. Random wechselt zufällig.")
        .defaultValue(Direction.Right)
        .build()
    );

    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("silent")
        .description("Dreht nur serverseitig, deine Kamera bleibt ruhig.")
        .defaultValue(false)
        .build()
    );

    // --- Unregelmäßigkeiten ---
    private final Setting<Double> jitter = sgRandom.add(new DoubleSetting.Builder()
        .name("speed-jitter")
        .description("Zufällige Abweichung der Geschwindigkeit in Prozent pro Tick.")
        .defaultValue(20)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private final Setting<Double> flipChance = sgRandom.add(new DoubleSetting.Builder()
        .name("direction-flip-chance")
        .description("Chance pro Tick in Prozent, die Richtung zu wechseln (nur bei Direction = Random).")
        .defaultValue(2)
        .min(0)
        .sliderMax(20)
        .visible(() -> direction.get() == Direction.Random)
        .build()
    );

    private final Setting<Double> pauseChance = sgRandom.add(new DoubleSetting.Builder()
        .name("pause-chance")
        .description("Chance pro Tick in Prozent, kurz mit dem Drehen auszusetzen.")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> pauseMin = sgRandom.add(new IntSetting.Builder()
        .name("pause-min-ticks")
        .description("Kürzeste Pause in Ticks.")
        .defaultValue(2)
        .min(1)
        .sliderMax(40)
        .visible(() -> pauseChance.get() > 0)
        .build()
    );

    private final Setting<Integer> pauseMax = sgRandom.add(new IntSetting.Builder()
        .name("pause-max-ticks")
        .description("Längste Pause in Ticks.")
        .defaultValue(8)
        .min(1)
        .sliderMax(40)
        .visible(() -> pauseChance.get() > 0)
        .build()
    );

    private final Setting<Double> pitchWobble = sgRandom.add(new DoubleSetting.Builder()
        .name("pitch-wobble")
        .description("Leichtes zufälliges Wackeln der Neigung in Grad (nur im Silent-Modus).")
        .defaultValue(0)
        .min(0)
        .sliderMax(15)
        .visible(silent::get)
        .build()
    );

    private final Random random = new Random();
    private float yaw;
    private int dir = 1;
    private int pauseLeft;
    private float pitchOffset;

    public SpinModule() {
        super(SpinAddon.CATEGORY, "spin", "Dreht dich die ganze Zeit.");
    }

    @Override
    public void onActivate() {
        if (mc.player != null) yaw = mc.player.getYaw();
        pauseLeft = 0;
        pitchOffset = 0;
        dir = direction.get() == Direction.Left ? -1 : 1;
        if (direction.get() == Direction.Random && random.nextBoolean()) dir = -dir;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        // Pause läuft
        if (pauseLeft > 0) {
            pauseLeft--;
            return;
        }

        // Neue Pause starten
        if (pauseChance.get() > 0 && random.nextDouble() * 100 < pauseChance.get()) {
            int min = pauseMin.get();
            int max = Math.max(min, pauseMax.get());
            pauseLeft = min + random.nextInt(max - min + 1);
            return;
        }

        // Richtung bestimmen
        switch (direction.get()) {
            case Right -> dir = 1;
            case Left -> dir = -1;
            case Random -> {
                if (random.nextDouble() * 100 < flipChance.get()) dir = -dir;
            }
        }

        // Geschwindigkeit mit Zufallsabweichung
        float s = speed.get().floatValue();
        if (jitter.get() > 0) {
            s *= 1f + (float) ((random.nextDouble() * 2 - 1) * jitter.get() / 100.0);
        }
        yaw += s * dir;

        if (silent.get()) {
            float pitch = mc.player.getPitch();
            if (pitchWobble.get() > 0) {
                float max = pitchWobble.get().floatValue();
                pitchOffset += (random.nextFloat() - 0.5f) * max * 0.3f;
                pitchOffset = Math.max(-max, Math.min(max, pitchOffset));
                pitch = Math.max(-90f, Math.min(90f, pitch + pitchOffset));
            }
            Rotations.rotate(yaw, pitch);
        } else {
            mc.player.setYaw(yaw);
        }
    }
}
