package fr.robie.craftengineconverter.common.utils.enums;

import java.util.Map;

@FunctionalInterface
public interface TemplatesCommandsFunctionalInterface {
    Object apply(String args, Map<String, Object> remplacements);
}
