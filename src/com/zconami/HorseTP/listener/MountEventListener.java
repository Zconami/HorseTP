package com.zconami.HorseTP.listener;

import static com.zconami.HorseTP.HorseTPPlugin.getHorseTPPlugin;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.zconami.Core.util.Utils;
import com.zconami.HorseTP.HorseTPPlugin;
import com.zconami.HorseTP.domain.Mount;
import com.zconami.HorseTP.domain.MountCreateParameters;
import com.zconami.HorseTP.repository.MountRepository;
import com.zconami.HorseTP.util.HorseTPUtils;

public class MountEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final MountRepository mountRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public MountEventListener(MountRepository mountRepository) {
        this.mountRepository = mountRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        if (HorseTPUtils.isMount(entity)) {
            final Mount deadMount = mountRepository.find((Horse) entity);
            final Player ownerOnlinePlayer = deadMount.getOwner().getPlayer();
            if (ownerOnlinePlayer != null) {
                Utils.sendMessage(ownerOnlinePlayer, HorseTPPlugin.PLUGIN_NAME, "A mount you own has died!");
            }

            if (deadMount.isPrimary()) {
                final List<Mount> playerMounts = mountRepository.getPlayerMounts(deadMount.getOwner());
                final Optional<Mount> otherMount = playerMounts.stream()
                        .filter(mount -> mount.getKey().equals(deadMount.getKey())).findFirst();
                if (otherMount.isPresent()) {
                    otherMount.get().setPrimary(true);
                }
            }

            deadMount.remove();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory topInventory = event.getView().getTopInventory();
        if (topInventory instanceof HorseInventory) {
            // Run next tick when event is finished (easier to follow what
            // happened)
            Utils.getScheduler().runTaskLater(getHorseTPPlugin(), new Runnable() {
                @Override
                public void run() {
                    final ItemStack saddleSlotItem = ((HorseInventory) topInventory).getSaddle();
                    final Horse horse = (Horse) topInventory.getHolder();
                    final Mount existingMount = mountRepository.find(horse);

                    // If there used to be a saddle on this horse and now there
                    // isn't...
                    if (existingMount != null && saddleSlotItem == null) {
                        final OfflinePlayer oldOwner = existingMount.getOwner();
                        notifyOwnershipLost(oldOwner);
                        existingMount.remove();
                        return;
                    }

                    // There is a saddle on the horse...
                    if (saddleSlotItem != null && saddleSlotItem.getType() == Material.SADDLE) {
                        final String saddleDisplayName = saddleSlotItem.getItemMeta().getDisplayName();
                        final Optional<OfflinePlayer> optPlayer = Utils.getPlayer(saddleDisplayName);
                        // .. if the saddle on the horse is named after a player
                        // ..
                        if (optPlayer.isPresent()) {
                            final OfflinePlayer horseOwner = optPlayer.get();
                            // .. and there wasn't a saddle on this horse
                            // previously,
                            // then it's a new mount ..
                            if (existingMount == null) {
                                // Create new mount
                                final MountCreateParameters params = new MountCreateParameters(horse, horseOwner);
                                params.setPrimary(mountRepository.getPrimary(horseOwner) == null);
                                params.setName(horse.getCustomName());

                                final Mount createdMount = new Mount(params);
                                mountRepository.save(createdMount);
                            }
                            // ... otherwise the ownership might have changed ..
                            else {
                                final OfflinePlayer oldOwner = existingMount.getOwner();
                                // .. if a new owner ..
                                if (horseOwner.getUniqueId() != oldOwner.getUniqueId()) {
                                    notifyOwnershipLostToNewOwner(oldOwner, horseOwner);
                                    existingMount.setOwner(horseOwner);
                                }
                                // .. owner is same as old, do nothing ..
                                else {
                                    return;
                                }
                            }

                            // Notify new owner
                            final Player onlinePlayer = horseOwner.getPlayer();
                            if (onlinePlayer != null) {
                                Utils.sendMessage(onlinePlayer, HorseTPPlugin.PLUGIN_NAME,
                                        "You've become the owner of a mount, do /h list to see your available mounts");
                            }

                        }
                    }
                }
            }, 1);
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void notifyOwnershipLost(OfflinePlayer oldOwner) {
        final Player oldOnlinePlayer = oldOwner.getPlayer();
        if (oldOnlinePlayer != null) {
            Utils.sendMessage(oldOnlinePlayer, HorseTPPlugin.PLUGIN_NAME, " You've lost ownership of a mount");
        }
    }

    private void notifyOwnershipLostToNewOwner(OfflinePlayer oldOwner, OfflinePlayer newOwner) {
        final Player oldOnlinePlayer = oldOwner.getPlayer();
        if (oldOnlinePlayer != null) {
            Utils.sendMessage(oldOnlinePlayer, HorseTPPlugin.PLUGIN_NAME,
                    newOwner.getName() + " has taken ownership of a mount you used to own");
        }

    }

}
