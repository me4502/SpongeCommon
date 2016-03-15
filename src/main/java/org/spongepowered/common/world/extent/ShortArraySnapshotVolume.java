/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.world.extent;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.SnapshotVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.common.util.gen.ShortArrayMutableBlockBuffer;
import org.spongepowered.common.world.schematic.HashBlockPalette;

import java.util.List;
import java.util.Optional;

public class ShortArraySnapshotVolume extends ShortArrayMutableBlockBuffer implements SnapshotVolume {

    public ShortArraySnapshotVolume(Vector3i start, Vector3i size) {
        super(start, size);
    }

    public ShortArraySnapshotVolume(Vector3i start, Vector3i size, BlockPalette palette) {
        super(start, size, palette);
    }

    @Override
    public Optional<BlockSnapshot> getTileEntity(int x, int y, int z) {
        return null;
    }

    @Override
    public List<BlockSnapshot> getTileEntities() {
        return null;
    }

    @Override
    public MutableBlockVolumeWorker<? extends SnapshotVolume> getBlockWorker() {
        return null;
    }

}
