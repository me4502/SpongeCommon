/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.world.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.schematic.BlockPalette;

import java.util.Optional;

public class GlobalBlockPalette implements BlockPalette {

    private static final GlobalBlockPalette INSTANCE = new GlobalBlockPalette();

    public static GlobalBlockPalette getInstance() {
        return INSTANCE;
    }

    private GlobalBlockPalette() {
    }

    @Override
    public int getPaletteSize() {
        return 8192;
    }

    @Override
    public Optional<BlockState> getState(int index) {
        return Optional.ofNullable((BlockState) Block.BLOCK_STATE_IDS.getByValue(index));
    }

    @Override
    public Optional<Integer> getIndex(BlockState state) {
        return Optional.ofNullable(Block.BLOCK_STATE_IDS.get((IBlockState) state));
    }

    @Override
    public int addState(BlockState state) {
        return getIndex(state).get();
    }

    @Override
    public void clear() {
        // do nothing
    }

}
