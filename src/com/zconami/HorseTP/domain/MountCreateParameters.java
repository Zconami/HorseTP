package com.zconami.HorseTP.domain;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;

import com.zconami.Core.domain.LinkedEntityCreateParameters;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class MountCreateParameters extends LinkedEntityCreateParameters<Horse, EntityHorse> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final OfflinePlayer owner;

    private boolean primary = false;

    private String name = null;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public MountCreateParameters(Horse bukkitEntity, OfflinePlayer owner) {
        super(bukkitEntity);
        this.owner = owner;
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
