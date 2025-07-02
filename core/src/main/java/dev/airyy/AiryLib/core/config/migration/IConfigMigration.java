package dev.airyy.AiryLib.core.config.migration;

import java.util.Map;

public interface IConfigMigration {

    int fromVersion();
    void migrate(Map<String, Object> data);
}
