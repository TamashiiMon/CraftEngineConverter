package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumableConfiguration implements ItemConfigurationSerializable {

    private final String sound;
    private final boolean hasConsumeParticles;
    private final double consumeSeconds;
    private final Animation animation;
    private final List<ConsumeEffect> onConsumeEffects;

    public ConsumableConfiguration(String sound, boolean hasConsumeParticles, double consumeSeconds, Animation animation, List<ConsumeEffect> onConsumeEffects) {
        this.sound = sound;
        this.hasConsumeParticles = hasConsumeParticles;
        this.consumeSeconds = consumeSeconds;
        this.animation = animation;
        this.onConsumeEffects = onConsumeEffects;
    }

    public enum Animation {
        NONE, EAT, DRINK, BLOCK, BOW, SPEAR, CROSSBOW, SPYGLASS, TOOT_HORN, BRUSH, BUNDLE, TRIDENT;

        public String toKey() {
            return this.name().toLowerCase();
        }
    }

    public interface ConsumeEffect {
        Map<String, Object> serialize();
    }

    public record ApplyEffectsConsumeEffect(List<ApplyEffect> effects) implements ConsumeEffect {

        public record ApplyEffect(
                String id,
                int amplifier,
                int duration,
                boolean ambient,
                boolean showParticles,
                boolean showIcon,
                double probability
        ) {}

        @Override
        public Map<String, Object> serialize() {
            List<Map<String, Object>> serializedEffects = new ArrayList<>();
            for (ApplyEffect effect : effects) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", effect.id());
                map.put("amplifier", effect.amplifier());
                map.put("duration", effect.duration());
                map.put("ambient", effect.ambient());
                map.put("show_particles", effect.showParticles());
                map.put("show_icon", effect.showIcon());
                map.put("probability", effect.probability());
                serializedEffects.add(map);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("type", "apply_effects");
            result.put("effects", serializedEffects);
            return result;
        }
    }

    public record RemoveEffectsConsumeEffect(List<String> effects) implements ConsumeEffect {
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            result.put("type", "remove_effects");
            result.put("effects", effects);
            return result;
        }
    }

    public record ClearAllEffectsConsumeEffect() implements ConsumeEffect {
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            result.put("type", "clear_all_effects");
            return result;
        }
    }

    public record TeleportRandomlyConsumeEffect(double diameter) implements ConsumeEffect {
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            result.put("type", "teleport_randomly");
            result.put("diameter", diameter);
            return result;
        }
    }

    public record PlaySoundConsumeEffect(String soundId, double range) implements ConsumeEffect {
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> soundMap = new HashMap<>();
            soundMap.put("sound_id", soundId);
            soundMap.put("range", range);

            Map<String, Object> result = new HashMap<>();
            result.put("type", "play_sound");
            result.put("sound", soundMap);
            return result;
        }
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection) {
        ConfigurationSection components = getOrCreateSection(itemSection, "components");
        ConfigurationSection consumableSection = getOrCreateSection(components, "minecraft:consumable");

        if (!sound.equals("entity.generic.eat"))
            consumableSection.set("sound", sound);
        if (!hasConsumeParticles)
            consumableSection.set("has_consume_particles", false);
        if (consumeSeconds != 1.6)
            consumableSection.set("consume_seconds", consumeSeconds);
        if (animation != Animation.EAT)
            consumableSection.set("animation", animation.toKey());

        if (onConsumeEffects != null && !onConsumeEffects.isEmpty()) {
            List<Map<String, Object>> serializedEffects = new ArrayList<>();
            for (ConsumeEffect effect : onConsumeEffects) {
                serializedEffects.add(effect.serialize());
            }
            consumableSection.set("on_consume_effects", serializedEffects);
        }
    }
}