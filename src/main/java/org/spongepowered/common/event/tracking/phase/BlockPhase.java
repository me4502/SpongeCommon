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
package org.spongepowered.common.event.tracking.phase;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.BlockStateTriplet;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.world.CaptureType;

public class BlockPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        BLOCK_DECAY(false),
        RESTORING_BLOCKS,
        POST_NOTIFICATION_EVENT,
        ;

        private final boolean managed;

        State() {
            this.managed = false;
        }

        State(boolean managed) {
            this.managed = managed;
        }

        @Override
        public boolean isBusy() {
            return true;
        }

        @Override
        public boolean isManaged() {
            return this.managed;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return false;
        }

        @Override
        public BlockPhase getPhase() {
            return TrackingPhases.BLOCK;
        }

    }

    public BlockPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState != State.RESTORING_BLOCKS;
    }

    @Override
    public BlockStateTriplet captureBlockChange(CauseTracker causeTracker, IBlockState currentState, IBlockState newState, Block block, BlockPos pos,
            int flags, PhaseContext phaseContext, IPhaseState phaseState) {
        // Only capture final state of decay, ignore the rest
        BlockSnapshot originalBlockSnapshot;
        final IMixinWorld mixinWorld = causeTracker.getMixinWorld();

        originalBlockSnapshot = mixinWorld.createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                causeTracker.getMinecraftWorld(), pos), pos, flags);
        if (block == Blocks.air) {
            ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.DECAY;
            phaseContext.getCapturedBlocks().get().add(originalBlockSnapshot);
            return new BlockStateTriplet(originalBlockSnapshot, null);

        }
        return super.captureBlockChange(causeTracker, currentState, newState, block, pos, flags, phaseContext, phaseState);
    }

    @Override
    public boolean ignoresEntitySpawns(IPhaseState currentState) {
        return currentState == State.RESTORING_BLOCKS;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {

    }

}