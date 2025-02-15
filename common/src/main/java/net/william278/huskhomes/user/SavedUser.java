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

package net.william278.huskhomes.user;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents data about a player on the server
 */
public class SavedUser {

    private final User user;

    private int homeSlots;
    private boolean ignoringTeleports;
    private Instant rtpCooldown;

    public SavedUser(@NotNull User user, int homeSlots, boolean ignoringTeleports, @NotNull Instant rtpCooldown) {
        this.user = user;
        this.homeSlots = homeSlots;
        this.ignoringTeleports = ignoringTeleports;
        this.rtpCooldown = rtpCooldown;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public UUID getUserUuid() {
        return user.getUuid();
    }

    @NotNull
    public String getUsername() {
        return user.getUsername();
    }

    public int getHomeSlots() {
        return homeSlots;
    }

    public void setHomeSlots(int homeSlots) {
        this.homeSlots = homeSlots;
    }

    public boolean isIgnoringTeleports() {
        return ignoringTeleports;
    }

    public void setIgnoringTeleports(boolean ignoringTeleports) {
        this.ignoringTeleports = ignoringTeleports;
    }

    @NotNull
    public Instant getRtpCooldown() {
        return rtpCooldown;
    }

    public void setRtpCooldown(@NotNull Instant rtpCooldown) {
        this.rtpCooldown = rtpCooldown;
    }

}
