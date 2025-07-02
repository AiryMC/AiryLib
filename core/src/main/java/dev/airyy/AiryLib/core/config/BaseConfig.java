package dev.airyy.AiryLib.core.config;

import dev.airyy.AiryLib.core.config.migration.IConfigMigration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class BaseConfig {

    private final List<IConfigMigration> migrations = new ArrayList<>();
    private final File dataFolder;

    public BaseConfig(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void addMigration(IConfigMigration migration) {
        migrations.add(migration);
        migrations.sort(Comparator.comparingInt(IConfigMigration::fromVersion));
    }

    public List<IConfigMigration> getMigrations() {
        return migrations;
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
