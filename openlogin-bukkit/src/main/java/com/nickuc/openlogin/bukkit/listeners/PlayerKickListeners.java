/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listeners;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.settings.Messages;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PlayerKickListeners implements Listener {

    private static final Pattern VALID_NICK = Pattern.compile("([a-zA-Z0-9_]{3,16})|(\\*[a-zA-Z0-9_]{3,17})");

    private final OpenLoginBukkit plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent e) {
        String name = e.getName();
        Player player = Bukkit.getPlayerExact(name);

        // prevent double online nickname
        if (player != null) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.ALREADY_ONLINE.asString());
            return;
        }

        // prevent invalid nicknames
        if (!VALID_NICK.matcher(name).matches()) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.INVALID_NICKNAME.asString("§cSorry, but you are using an invalid nickname."));
            return;
        }

        Optional<Account> accountOpt = plugin.getLoginManagement().retrieveOrLoad(name);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            String realname = account.getRealname();
            if (!name.equals(realname)) {
                String kickMessage = Messages.NICK_ALREADY_REGISTERED.asString()
                        .replace("{0}", name)
                        .replace("{1}", realname);
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = Bukkit.getPlayer(e.getPlayer().getName());

        // prevent double online nickname
        if (player != null) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Messages.ALREADY_ONLINE.asString());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerKick(PlayerKickEvent e) {
        String reason = e.getReason();

        // prevent kick online players
        if (reason.contains("You logged in from another location")) {
            e.setCancelled(true);
        }
    }

}
