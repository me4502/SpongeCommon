/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.persistence;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeVisitor;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.CharArraySchematic;
import org.spongepowered.common.world.schematic.GlobalPalette;

public class LegacySchematicTranslator implements DataTranslator<Schematic> {

    private static final LegacySchematicTranslator INSTANCE = new LegacySchematicTranslator();
    private static final TypeToken<Schematic> TYPE_TOKEN = TypeToken.of(Schematic.class);
    private static final int MAX_SIZE = 65535;

    public static LegacySchematicTranslator get() {
        return INSTANCE;
    }

    private LegacySchematicTranslator() {

    }

    @Override
    public String getId() {
        return "sponge:legacy_schematic";
    }

    @Override
    public String getName() {
        return "Legacy Schematic translator";
    }

    @Override
    public TypeToken<Schematic> getToken() {
        return TYPE_TOKEN;
    }

    @Override
    public Schematic translate(DataView view) throws InvalidDataException {
        // We default to sponge as the assumption should be that if this tag
        // (which is not in the sponge schematic specification) is not present
        // then it is more likely that its a sponge schematic than a legacy
        // schematic
        String materials = view.getString(DataQueries.LegacySchematic.MATERIALS).orElse("Sponge");
        if ("Sponge".equalsIgnoreCase(materials)) {
            // not a legacy schematic use the new loader instead.
            return DataTranslators.SCHEMATIC.translate(view);
        } else if (!"Alpha".equalsIgnoreCase(materials)) {
            throw new InvalidDataException(String.format("Schematic specifies unknown materials %s", materials));
        }
        int width = view.getShort(DataQueries.LegacySchematic.WIDTH).get();
        int height = view.getShort(DataQueries.LegacySchematic.HEIGHT).get();
        int length = view.getShort(DataQueries.LegacySchematic.LENGTH).get();
        if (width > MAX_SIZE || height > MAX_SIZE || length > MAX_SIZE) {
            throw new InvalidDataException(String.format(
                    "Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)", width, height, length, MAX_SIZE));
        }
        int offsetX = view.getInt(DataQueries.LegacySchematic.OFFSET_X).orElse(0);
        int offsetY = view.getInt(DataQueries.LegacySchematic.OFFSET_Y).orElse(0);
        int offsetZ = view.getInt(DataQueries.LegacySchematic.OFFSET_Z).orElse(0);
        Palette palette = new BimapPalette();
        CharArraySchematic schematic =
                new CharArraySchematic(palette, new Vector3i(-offsetX, -offsetY, -offsetZ), new Vector3i(width - 1, height - 1, length - 1));

        // TODO

        return schematic;
    }

    @Override
    public DataContainer translate(Schematic schematic) throws InvalidDataException {
        DataContainer data = new MemoryDataContainer();
        final int xMin = schematic.getBlockMin().getX();
        final int yMin = schematic.getBlockMin().getY();
        final int zMin = schematic.getBlockMin().getZ();
        final int width = schematic.getBlockSize().getX() + 1;
        final int height = schematic.getBlockSize().getY() + 1;
        final int length = schematic.getBlockSize().getZ() + 1;
        if (width > MAX_SIZE || height > MAX_SIZE || length > MAX_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)", width, height, length, MAX_SIZE));
        }
        data.set(DataQueries.LegacySchematic.WIDTH, width);
        data.set(DataQueries.LegacySchematic.HEIGHT, height);
        data.set(DataQueries.LegacySchematic.LENGTH, length);
        data.set(DataQuery.of("Materials"), "Alpha");
        // These are added for better interop with WorldEdit
        data.set(DataQuery.of("WEOffsetX"), xMin);
        data.set(DataQuery.of("WEOffsetY"), yMin);
        data.set(DataQuery.of("WEOffsetZ"), zMin);
        SaveIterator itr = new SaveIterator(width, height, length);
        schematic.getBlockWorker().iterate(itr);
        byte[] blockids = itr.blockids;
        byte[] extraids = itr.extraids;
        byte[] blockdata = itr.blockdata;
        data.set(DataQuery.of("Blocks"), blockids);
        data.set(DataQuery.of("Data"), blockdata);
        if (extraids != null) {
            data.set(DataQuery.of("AddBlocks"), extraids);
        }
        // TODO extract entities ?
        return data;
    }

    private static class SaveIterator implements BlockVolumeVisitor<Schematic> {

        private final int width;
        private final int length;

        public byte[] blockids;
        public byte[] extraids;
        public byte[] blockdata;

        public SaveIterator(int width, int height, int length) {
            this.width = width;
            this.length = length;

            this.blockids = new byte[width * height * length];
            this.extraids = null;
            this.blockdata = new byte[width * height * length];
        }

        @Override
        public void visit(Schematic volume, int x, int y, int z) {
            int x0 = x - volume.getBlockMin().getX();
            int y0 = y - volume.getBlockMin().getY();
            int z0 = z - volume.getBlockMin().getZ();
            int id = GlobalPalette.instance.getOrAssign(volume.getBlock(x, y, z));
            int blockid = id >> 4;
            int dataid = id & 0xF;
            int index = (y0 * this.length + z0) * this.width + x0;
            this.blockdata[index] = (byte) (blockid & 0xFF);
            if (blockid > 0xFF) {
                if (this.extraids == null) {
                    this.extraids = new byte[(this.blockdata.length >> 2) + 1];
                }
                this.extraids[index >> 1] = (byte) (((index & 1) == 0) ? this.extraids[index >> 1] & 0xF0 | (blockid >> 8) & 0xF
                        : this.extraids[index >> 1] & 0xF | ((blockid >> 8) & 0xF) << 4);
            }
            this.blockdata[index] = (byte) dataid;
            // TODO extract tile entities here
        }

    }

}
