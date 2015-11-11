package com.zconami.HorseTP.listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.zconami.HorseTP.domain.Mount;
import com.zconami.HorseTP.repository.MountRepository;
import com.zconami.HorseTP.util.HorseTPUtils;

public class ChunkEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final MountRepository mountRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public ChunkEventListener(MountRepository mountRepository) {
        this.mountRepository = mountRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (HorseTPUtils.isMount(entity)) {
                final Mount mount = mountRepository.find((Horse) entity);
                mount.setChunk(chunk);
            }
        }
    }

}
