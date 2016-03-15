/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.world.schematic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.schematic.BlockPalette;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class HashBlockPalette implements BlockPalette {

    private static final int INITIAL_ID_SPACE = 16;

    private final BiMap<BlockState, Integer> ids = HashBiMap.create(INITIAL_ID_SPACE);
    private final BiMap<Integer, BlockState> rev_ids = this.ids.inverse();
    private final AtomicInteger next = new AtomicInteger(0);

    public HashBlockPalette() {

    }

    @Override
    public int getPaletteSize() {
        return this.next.get();
    }

    @Override
    public Optional<BlockState> getState(int index) {
        return Optional.ofNullable(this.rev_ids.get(index));
    }

    @Override
    public Optional<Integer> getIndex(BlockState state) {
        return Optional.ofNullable(this.ids.get(state));
    }

    @Override
    public int addState(BlockState state) {
        if (this.ids.containsKey(state)) {
            return this.ids.get(state);
        }
        int next = this.next.getAndIncrement();
        this.ids.put(state, next);
        return next;
    }

    @Override
    public void clear() {
        this.ids.clear();
        this.next.set(0);
    }

    public Map<String, Integer> asStringMapping() {
        Map<String, Integer> mapping = Maps.newHashMapWithExpectedSize(this.ids.size());
        this.ids.forEach((state, index) -> mapping.put(state.getId(), index));
        return mapping;
    }

}
