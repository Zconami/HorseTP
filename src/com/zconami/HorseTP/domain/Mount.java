package com.zconami.HorseTP.domain;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;

import com.zconami.Core.domain.LinkedEntity;
import com.zconami.Core.storage.DataKey;
import com.zconami.Core.util.Utils;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class Mount extends LinkedEntity<Horse, EntityHorse> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String OWNER = "owner";
    private OfflinePlayer owner;

    public static final String PRIMARY = "primary";
    private boolean primary;

    public static final String NAME = "name";
    private String name;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Mount(Horse bukkitEntity, DataKey dataKey) {
        super(bukkitEntity, dataKey);
    }

    public Mount(MountCreateParameters params) {
        super(params);
        this.owner = params.getOwner();
        this.primary = params.isPrimary();
        this.name = params.getName();
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public OfflinePlayer getOwner() {
        return owner;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getName() {
        // Check if name has changed
        final String customName = this.getBukkitEntity().getCustomName();
        if (customName != null && !customName.equals(name)) {
            this.name = customName;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
        this.setDirty(true);
    }

    // ===================================
    // IMPLEMENTATION OF LinkedEntity
    // ===================================

    @Override
    public Class<Horse> getBukkitEntityType() {
        return Horse.class;
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public void writeData(DataKey dataKey) {
        super.writeData(dataKey);
        dataKey.setString(OWNER, owner.getUniqueId().toString());
        dataKey.setBoolean(PRIMARY, primary);
        if (name != null) {
            dataKey.setString(NAME, name);
        }
    }

    @Override
    public void readData(DataKey dataKey) {
        super.readData(dataKey);

        final UUID ownerUUID = UUID.fromString(dataKey.getString(OWNER));
        this.owner = Utils.getPlayer(ownerUUID);

        final String pName = dataKey.getString(NAME);
        if (pName != null) {
            this.name = pName;
        }

        this.primary = dataKey.getBoolean(PRIMARY);
    }

}
