/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.world.schematic;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.api.world.schematic.SchematicVolume;
import org.spongepowered.common.world.extent.ShortArraySnapshotVolume;

import java.util.List;
import java.util.Properties;

public class SpongeSchematicVolume extends ShortArraySnapshotVolume implements SchematicVolume {

    private final Properties metadata;
    private final List<EntitySnapshot> entities;

    public SpongeSchematicVolume(Vector3i start, Vector3i size, BlockPalette palette, Properties metadata) {
        super(start, size, palette);
        this.metadata = metadata;
        this.entities = Lists.newArrayList();
    }

    @Override
    public Properties getMetaData() {
        return this.metadata;
    }

    @Override
    public BlockPalette getPalette() {
        return this.palette;
    }

    @Override
    public List<EntitySnapshot> getEntities() {
        return this.entities;
    }

}
