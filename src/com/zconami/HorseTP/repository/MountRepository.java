package com.zconami.HorseTP.repository;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zconami.Core.ZconamiPlugin;
import com.zconami.Core.repository.LinkedRepository;
import com.zconami.Core.storage.DataKey;
import com.zconami.HorseTP.domain.Mount;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class MountRepository extends LinkedRepository<Horse, EntityHorse, Mount> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final String NAME = "mount";

    private static final Map<OfflinePlayer, List<Mount>> ownerLookup = Maps.newHashMap();

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public MountRepository(ZconamiPlugin plugin) {
        super(plugin);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Mount save(Mount mount) {
        return super.save(mount);
    }

    public List<Mount> getPlayerMounts(OfflinePlayer player) {

        final List<Mount> mounts = ownerLookup.get(player);
        if (mounts == null) {
            ownerLookup.put(player, Lists.newArrayList());
            all();
            return ownerLookup.get(player);
        }

        if (mounts.isEmpty()) {
            all();
            return ownerLookup.get(player);
        }

        return mounts;
    }

    public Mount getPrimary(OfflinePlayer player) {
        final List<Mount> mounts = getPlayerMounts(player);
        if (mounts.isEmpty()) {
            return null;
        }

        final Optional<Mount> optPrimary = mounts.stream().filter(Mount::isPrimary).findFirst();
        if (optPrimary.isPresent()) {
            return optPrimary.get();
        }

        // No primary, set and return 1st
        final Mount primary = mounts.get(0);
        primary.setPrimary(true);
        return primary;
    }

    // ===================================
    // IMPLEMENTATION OF LinkedRepository
    // ===================================

    @Override
    protected Class<Horse> getBukkitEntityType() {
        return Horse.class;
    }

    @Override
    protected Mount recreate(Horse bukkitEntity, DataKey entityData) {
        return new Mount(bukkitEntity, entityData);
    }

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    public void entityChanged(Mount changedEntity) {
        for (Entry<OfflinePlayer, List<Mount>> entry : ownerLookup.entrySet()) {
            final List<Mount> mounts = entry.getValue();
            for (Mount mount : mounts) {
                if (mount.getKey().equals(changedEntity.getKey())) {
                    mounts.remove(mount);
                    createLookups(changedEntity);
                }
            }
        }
        super.entityChanged(changedEntity);
    }

    @Override
    protected void createLookups(Mount mount) {
        final OfflinePlayer owner = mount.getOwner();
        final List<Mount> playerMounts = ownerLookup.get(owner);
        if (playerMounts == null) {
            ownerLookup.put(owner, Lists.newArrayList());
            createLookups(mount);
            return;
        }

        playerMounts.remove(mount);
        playerMounts.add(mount);
    }

    @Override
    protected void removeLookups(Mount mount) {
        ownerLookup.remove(mount.getOwner());
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

}
