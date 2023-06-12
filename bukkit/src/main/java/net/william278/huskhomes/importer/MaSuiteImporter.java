/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.importer;

import com.zaxxer.hikari.HikariDataSource;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.BukkitAdapter;
import net.william278.huskhomes.util.ValidationException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MaSuiteImporter extends Importer {
    HikariDataSource dataSource;
    public MaSuiteImporter(@NotNull HuskHomes plugin) {
        super("MaSuite", List.of(ImportData.HOMES, ImportData.WARPS), plugin);
        FileConfiguration masuiteConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),
                "masuite.yml"));
        ConfigurationSection dbSection = masuiteConfig.getConfigurationSection("database");
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://" +
                dbSection.getString("address") +
                ":" +
                dbSection.getString("port") +
                "/" +
                dbSection.getString("name"));

        // Authenticate with the database
        dataSource.setUsername(dbSection.getString("username"));
        dataSource.setPassword(dbSection.getString("password"));
    }

    private record Home(String name, User owner, Position position) {}

    private int importHomes() throws SQLException {
        Logger logger = ((BukkitHuskHomes)plugin).getLogger();
        List<Home> homes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT name,owner,world,x,y,z,yaw,pitch FROM masuite_homes WHERE server=?;")) {
            statement.setString(1,plugin.getServerName());
            try(ResultSet results = statement.executeQuery()) {
                while(results.next()) {
                    String name = results.getString(1);
                    OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(UUID.fromString(results.getString(2)));
                    User owner = OnlineUser.of(ownerPlayer.getUniqueId(), Objects.requireNonNullElse(ownerPlayer.getName(),""));
                    String worldName = results.getString(3);
                    Optional<World> world = BukkitAdapter.adaptWorld(Bukkit.getWorld(worldName));
                    if(world.isEmpty()) {
                        logger.severe(String.format("The home %s owned by %s with UUID %s is in a world that doesn't exist: %s.",
                                name, ownerPlayer.getName(), ownerPlayer.getUniqueId(), worldName));
                        continue;
                    }
                    Position position = Position.at(results.getDouble(4),results.getDouble(5),
                            results.getDouble(6),results.getFloat(7),results.getFloat(8),
                            world.get(), plugin.getServerName());
                    homes.add(new Home(name,owner,position));
                }
            }
        }
        final AtomicInteger homesImported = new AtomicInteger();
        for (Home home : homes) {
            plugin.getDatabase().ensureUser(home.owner());
            plugin.getSavedUsers().add(plugin.getDatabase().getUserData(home.owner().getUuid()).get());
            try {
                plugin.getManager().homes().createHome(
                        home.owner(),
                        this.normalizeName(home.name()),
                        home.position(),
                        true,
                        true
                );
            } catch (ValidationException e) {
                logger.severe(
                        String.format("The home %s owned by %s with UUID %s caused a ValidationException: %s.",
                                home.name(), home.owner().getUsername(), home.owner().getUuid(), e.getType()));
                continue;
            }
            homesImported.getAndIncrement();
        }
        return homesImported.get();
    }

    private record Warp(String name, Position position) {}
    private int importWarps() throws SQLException {
        List<Warp> warps = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT name, world, x, y, z, yaw, pitch FROM masuite_warps WHERE server=?;")) {
            statement.setString(1, plugin.getServerName());
            try(ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    String name = results.getString(1);
                    World world = BukkitAdapter.adaptWorld(Bukkit.getWorld(results.getString(2))).get();
                    Position position = Position.at(results.getDouble(3),results.getDouble(4),
                            results.getDouble(5),results.getFloat(6),results.getFloat(7),
                            world, plugin.getServerName());
                    warps.add(new Warp(name, position));
                }
            }
        }
        final AtomicInteger warpsImported = new AtomicInteger();
        for (Warp warp : warps) {
            plugin.getManager().warps().createWarp(
                this.normalizeName(warp.name()),
                warp.position(),
                true
            );
            warpsImported.getAndIncrement();
        }
        return warpsImported.get();
    }

    @NotNull
    private String normalizeName(@NotNull String name) {
        if (plugin.getValidator().isValidName(name)) {
            return name;
        }

        // Remove spaces
        name = name.replaceAll(" ", "_");

        // Remove unicode characters
        if (!plugin.getSettings().doAllowUnicodeNames()) {
            name = name.replaceAll("[^A-Za-z0-9_-]", "");
        }

        // Ensure the name is not blank
        if (name.isBlank()) {
            name = "imported-" + UUID.randomUUID().toString().substring(0, 5);
        }

        // Ensure name is not too long
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }
        return name;
    }

    @Override
    protected int importData(@NotNull Importer.ImportData importData) throws Throwable {
        return switch (importData) {
            case USERS -> 0;
            case HOMES -> importHomes();
            case WARPS -> importWarps();
        };
    }

}
