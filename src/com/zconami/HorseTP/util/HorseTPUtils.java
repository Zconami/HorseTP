package com.zconami.HorseTP.util;

import static com.zconami.Core.util.Utils.sendMessage;
import static com.zconami.HorseTP.HorseTPPlugin.PLUGIN_NAME;
import static com.zconami.HorseTP.HorseTPPlugin.getHorseTPConfig;
import static com.zconami.HorseTP.HorseTPPlugin.getHorseTPPlugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.accountholder.PlayerAccountHolder;

import com.zconami.HorseTP.domain.Mount;

public class HorseTPUtils {

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private HorseTPUtils() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static boolean isMount(Entity candidate) {
        if (candidate instanceof Horse) {
            return getHorseTPPlugin().getMountRepository().find((Horse) candidate) != null;
        }
        return false;
    }

    public static boolean teleportToOwnerWithdraw(Player player, Mount mount) {
        final PlayerAccountHolder playerAccountHolder = new PlayerAccountHolder(player);
        final GringottsAccount playerAccount = Gringotts.G.accounting.getAccount(playerAccountHolder);
        final long playerBalance = playerAccount.balance();
        final long tpCost = getHorseTPConfig().getLong("tpCost");
        if (playerBalance < tpCost) {
            sendMessage(player, PLUGIN_NAME,
                    "You can't afford to teleport a horse right now, it costs " + ChatColor.GREEN + tpCost + " "
                            + Configuration.CONF.currency.namePlural + ChatColor.WHITE + " and you have "
                            + ChatColor.GREEN + playerBalance + " " + Configuration.CONF.currency.namePlural);
            return false;
        } else {
            playerAccount.remove(tpCost);
            final boolean teleported = teleportToOwner(mount);
            if (teleported) {
                sendMessage(player, PLUGIN_NAME, "You teleported your mount to you for " + ChatColor.GREEN + tpCost
                        + " " + Configuration.CONF.currency.namePlural);
            }
            return teleported;
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private static boolean teleportToOwner(Mount mount) {
        final Player onlinePlayer = mount.getOwner().getPlayer();
        if (onlinePlayer != null) {
            mount.getBukkitEntity().teleport(onlinePlayer);
            return true;
        }

        return false;
    }

}
