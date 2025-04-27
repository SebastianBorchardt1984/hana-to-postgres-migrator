package com.example.migration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.example.migration.model.TableMeta;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class MigrationRunner {

    private static final Logger LOG = Logger.getLogger(MigrationRunner.class);

    @Inject
    HanaMetadataService hanaService;

    @Inject
    PostgresSchemaService pgService;

    @ConfigProperty(name = "migration.limit", defaultValue = "-1")
    int migrationLimit;

    @ConfigProperty(name = "migration.dry-run", defaultValue = "false")
    boolean dryRun;

    @PostConstruct
    public void runMigration() {
        try {
            LOG.info("Starte SAP HANA ➔ PostgreSQL Migration...");

            if (dryRun) {
                LOG.info("⚡ Dry-Run Modus aktiv: Es werden nur DDL-Skripte generiert, keine Tabellen angelegt.");
            }

            List<TableMeta> tables = hanaService.fetchTables();
            LOG.infof("Gefundene Tabellen: %d", tables.size());

            int migrated = 0;

            for (TableMeta table : tables) {
                if (migrationLimit > 0 && migrated >= migrationLimit) {
                    LOG.infof("Erreichtes Limit von %d Tabellen. Migration wird gestoppt.", migrationLimit);
                    break;
                }

                try {
                    hanaService.fetchColumns(table);
                    hanaService.fetchPrimaryKey(table);
                    hanaService.fetchForeignKeys(table);

                    if (dryRun) {
                        pgService.generateFlywayScriptOnly(table);
                    } else {
                        pgService.createTableInPostgres(table);
                    }

                    migrated++;
                    LOG.infof("[%d] Tabelle '%s' migriert.", migrated, table.getName());

                } catch (Exception e) {
                    LOG.errorf("Fehler bei Tabelle '%s': %s", table.getName(), e.getMessage());
                }
            }

            LOG.infof("Migration abgeschlossen ✅: %d Tabellen verarbeitet.", migrated);
            LOG.info("Flyway-Skript verfügbar unter: src/main/resources/db/migration/V1__init.sql");

        } catch (SQLException e) {
            LOG.error("Fehler beim Laden der Tabellen: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Allgemeiner Fehler: " + e.getMessage(), e);
        }
    }
}
