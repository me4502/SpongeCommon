/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.service.whitelist;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.server.management.UserListWhitelistEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.common.util.UserListUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpongeWhitelistService implements WhitelistService {

    @SuppressWarnings("unchecked")
    @Override
    public Collection<GameProfile> getWhitelistedProfiles() {
        List<GameProfile> profiles = new ArrayList<>();

        for (UserListWhitelistEntry entry: MinecraftServer.getServer().getConfigurationManager().whiteListedPlayers.getValues().values()) {
            profiles.add((GameProfile) entry.getValue());
        }

        return profiles;
    }

    @Override
    public boolean isWhitelisted(GameProfile profile) {
        UserListWhitelist whitelist = this.getWhitelist();

        whitelist.removeExpired();
        return whitelist.getValues().containsKey(whitelist.getObjectKey((com.mojang.authlib.GameProfile) profile));
    }

    @Override
    public boolean addProfile(GameProfile profile) {
        boolean wasWhitelisted = this.isWhitelisted(profile);
        UserListUtils.addEntry(this.getWhitelist(), new UserListWhitelistEntry((com.mojang.authlib.GameProfile) profile));
        return wasWhitelisted;
    }

    @Override
    public boolean removeProfile(GameProfile profile) {
        boolean wasWhitelisted = this.isWhitelisted(profile);
        UserListUtils.removeEntry(this.getWhitelist(), profile);
        return wasWhitelisted;
    }

    private UserListWhitelist getWhitelist() {
        return MinecraftServer.getServer().getConfigurationManager().getWhitelistedPlayers();
    }
}
