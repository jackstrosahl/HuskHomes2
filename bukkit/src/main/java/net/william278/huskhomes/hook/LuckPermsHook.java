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

package net.william278.huskhomes.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.QueryOptions;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A hook that hooks into the Vault API to provide economy features
 */
public class LuckPermsHook extends PermissionHook {

    protected LuckPerms luckPerms;

    public LuckPermsHook(@NotNull HuskHomes plugin) {
        super(plugin, "LuckPerms");
    }

    @Override
    public void initialize()  {
        final RegisteredServiceProvider<LuckPerms> rsp = ((BukkitHuskHomes) plugin).getServer()
                .getServicesManager().getRegistration(LuckPerms.class);
        if (rsp != null) {
            luckPerms = rsp.getProvider();
        }
    }

    @Override
    public Map<String, Boolean> getPermissions(net.william278.huskhomes.user.User user) {
        User lpUser = luckPerms.getUserManager().loadUser(user.getUuid()).join();
        Collection<PermissionNode> nodes = lpUser.resolveInheritedNodes(NodeType.PERMISSION, QueryOptions.nonContextual());
        Map<String, Boolean> out = new HashMap<>();
        for(PermissionNode node : nodes) {
            out.put(node.getKey(),node.getValue());
        }
        return out;
    }
}
