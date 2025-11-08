package dev.airyy.AiryLib.core.config.migration;

import dev.dejvokep.boostedyaml.YamlDocument;

public interface IConfigMigration {

    int fromVersion();
    void migrate(YamlDocument config);
}
