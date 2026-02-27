package fr.robie.craftengineconverter.common.items;

import fr.robie.craftengineconverter.common.utils.CecAttributeModifier;
import fr.robie.craftengineconverter.common.utils.ItemConfigurationSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeModifiersConfiguration implements ItemConfigurationSerializable {
    private final List<CecAttributeModifier> attributeModifiers;

    public AttributeModifiersConfiguration(List<CecAttributeModifier> attributeModifiers) {
        this.attributeModifiers = attributeModifiers;
    }

    @Override
    public void serialize(@NotNull YamlConfiguration yamlConfiguration, @NotNull String path, @NotNull ConfigurationSection itemSection, @NotNull String itemId) {
        ConfigurationSection data = getOrCreateSection(itemSection, "data");
        List<Map<String, Object>> serializedModifiers = new ArrayList<>();
        for (CecAttributeModifier modifier : this.attributeModifiers) {
            Map<String, Object> serializedModifier = new HashMap<>();
            serializedModifier.put("type", modifier.type().toLowerCase());
            serializedModifier.put("amount", modifier.amount());
            serializedModifier.put("operation", modifier.operation().id());
            if (modifier.id() != null) {
                serializedModifier.put("id", modifier.id().asString());
            }
            serializedModifier.put("slot", modifier.slot().name().toLowerCase());
            if (modifier.display() != null) {
                serializedModifier.put("display", Map.of(
                        "type", modifier.display().type().name().toLowerCase(),
                        "value", modifier.display().value()
                ));
            }
            serializedModifiers.add(serializedModifier);
        }
        data.set("attribute-modifiers", serializedModifiers);
    }
}
