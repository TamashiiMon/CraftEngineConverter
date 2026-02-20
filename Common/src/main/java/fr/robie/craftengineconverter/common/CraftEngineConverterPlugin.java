package fr.robie.craftengineconverter.common;

import fr.robie.craftengineconverter.api.profile.ServerProfile;
import fr.robie.craftengineconverter.common.format.MessageFormatter;
import fr.robie.craftengineconverter.common.manager.FoliaCompatibilityManager;
import fr.robie.craftengineconverter.common.tag.ITagResolver;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CraftEngineConverterPlugin extends JavaPlugin {
    protected CraftEnginePlacementTracker placementTracker = new CraftEnginePlacementTracker();

    public abstract MessageFormatter getMessageFormatter();

    public abstract ITagResolver getTagResolver();

    public abstract FoliaCompatibilityManager getFoliaCompatibilityManager();

    public CraftEnginePlacementTracker getPlacementTracker() {
        return this.placementTracker;
    }

    public abstract ServerProfile getServerProfile();
}
