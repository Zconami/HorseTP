package com.zconami.HorseTP;

import static com.zconami.Core.util.Utils.sendMessage;
import static com.zconami.HorseTP.HorseTPPlugin.PLUGIN_NAME;
import static com.zconami.HorseTP.util.HorseTPUtils.teleportToOwnerWithdraw;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zconami.Core.util.ItemCallback;
import com.zconami.Core.util.Utils;
import com.zconami.HorseTP.domain.Mount;
import com.zconami.HorseTP.repository.MountRepository;

public class HorseTPCommandExecutor implements CommandExecutor {

    private final MountRepository mountRepository;

    public HorseTPCommandExecutor(MountRepository mountRepository) {
        this.mountRepository = mountRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("h")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    teleportPrimary(player);
                } else {
                    sendMessage(sender, PLUGIN_NAME, "Only players can teleport horses");
                }
            } else {
                final String secondary = args[0];
                if (secondary.equalsIgnoreCase("list")) {
                    if (sender instanceof Player) {
                        final int pageNumber;
                        if (args.length == 2) {
                            pageNumber = NumberUtils.toInt(args[1]);
                        } else {
                            pageNumber = 1;
                        }

                        final Player player = (Player) sender;
                        final List<Mount> mounts = mountRepository.getPlayerMounts(player);
                        listMounts(sender, pageNumber, mounts);
                    } else {
                        if (args.length >= 2) {
                            final int pageNumber;
                            if (args.length == 3) {
                                pageNumber = NumberUtils.toInt(args[2]);
                            } else {
                                pageNumber = 1;
                            }

                            final Optional<OfflinePlayer> optPlayer = Utils.getPlayer(args[1]);
                            if (optPlayer.isPresent()) {
                                final List<Mount> mounts = mountRepository.getPlayerMounts(optPlayer.get());
                                listMounts(sender, pageNumber, mounts);
                            } else {
                                sendMessage(sender, PLUGIN_NAME, "Couldn't find that player");
                            }
                        } else {
                            sendMessage(sender, PLUGIN_NAME, "You must specify a player, /h list <player>");
                        }
                    }
                } else if (secondary.equalsIgnoreCase("primary")) {
                    if (sender instanceof Player) {
                        final Player player = (Player) sender;
                        if (args.length == 2) {
                            final String targetMountKey = args[1];
                            final Mount targetMount = mountForTargetKey(player, targetMountKey);
                            if (targetMount != null) {
                                mountRepository.getPlayerMounts(player).forEach(mount -> mount.setPrimary(false));
                                targetMount.setPrimary(true);
                                sendMessage(sender, PLUGIN_NAME, "Primary horse set");
                            } else {
                                sendMessage(sender, PLUGIN_NAME,
                                        "Couldn't find that horse, check /h list and try again");
                            }
                        } else {
                            sendMessage(sender, PLUGIN_NAME,
                                    "You must specifiy a horse you want to set as a primary, e.g. /h primary 3 or /h primary JoeStallion");
                        }
                    } else {
                        sendMessage(sender, PLUGIN_NAME, "Only players can set a primary horses");
                    }
                } else if (secondary.equalsIgnoreCase("tp")) {
                    if (sender instanceof Player) {
                        final Player player = (Player) sender;
                        if (args.length == 1) {
                            teleportPrimary(player);
                        } else {
                            final String targetMountKey = args[1];
                            final Mount targetMount = mountForTargetKey(player, targetMountKey);
                            if (targetMount != null) {
                                teleportToOwnerWithdraw(player, targetMount);
                            } else {
                                sendMessage(sender, PLUGIN_NAME,
                                        "Couldn't find that horse, check /h list and try again");
                            }
                        }
                    } else {
                        sendMessage(sender, PLUGIN_NAME, "Only players can teleport horses");
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void listMounts(CommandSender sender, int pageNumber, List<Mount> mounts) {
        Utils.sendTable(sender, pageNumber, "Mounts",
                "Set primary mount with" + ChatColor.BLUE + " /h primary <horseNameOrIndex>", mounts,
                new ItemCallback<Mount>() {
                    @Override
                    public String itemEntry(Mount mount) {
                        final StringBuilder stringBuilder = new StringBuilder();
                        if (mount.getName() != null) {
                            stringBuilder.append(mount.getName());
                        }
                        if (mount.isPrimary()) {
                            stringBuilder.append(" (Primary)");
                        }
                        return stringBuilder.toString();
                    }
                });
    }

    private Mount mountForTargetKey(OfflinePlayer player, String targetMountKey) {
        final List<Mount> mounts = mountRepository.getPlayerMounts(player);
        if (NumberUtils.isNumber(targetMountKey)) {
            final Number targetNumber = NumberUtils.createNumber(targetMountKey);
            int index = targetNumber.intValue() - 1;
            Bukkit.getLogger().info("Mounts size: " + mounts.size());
            Bukkit.getLogger().info("index: " + index);
            if (!mounts.isEmpty() && mounts.size() > index) {
                return mounts.get(index);
            }
        } else {
            for (Mount mount : mounts) {
                if (mount.getName() != null && mount.getName().equalsIgnoreCase(targetMountKey)) {
                    return mount;
                }
            }
        }
        return null;
    }

    private void teleportPrimary(Player player) {
        final Mount primary = mountRepository.getPrimary(player);
        if (primary == null) {
            sendMessage(player, PLUGIN_NAME,
                    "You havn't designated a primary horse for the /h shorthand commands. Please set a primary horse with /h primary <horseNumber> and try again");
        } else {
            teleportToOwnerWithdraw(player, primary);
        }
    }

}
