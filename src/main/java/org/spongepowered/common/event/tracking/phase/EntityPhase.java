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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.function.EntityFunction;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;

public final class EntityPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        DEATH_DROPS_SPAWNING,
        DEATH_UPDATE,
        ;

        @Override
        public EntityPhase getPhase() {
            return TrackingPhases.ENTITY;
        }

    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.DEATH_DROPS_SPAWNING) {
            final Entity dyingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class).orElseThrow(PhaseUtil.createIllegalStateSupplier("Dying entity not found!", phaseContext));
            phaseContext.getCapturedItemsSupplier().get().ifPresent(items -> EntityFunction.Drops.DEATH_DROPS.process(dyingEntity, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> EntityFunction.Entities.DEATH_DROPS.process(dyingEntity, causeTracker, phaseContext, entities));

            // TODO Handle block changes
        } else if (state == State.DEATH_UPDATE) {
            final Entity dyingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class).orElseThrow(PhaseUtil.createIllegalStateSupplier("Dying entity not found!", phaseContext));
            phaseContext.getCapturedItemsSupplier().get().ifPresent(items -> EntityFunction.Drops.DEATH_UPDATES.process(dyingEntity, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> EntityFunction.Entities.DEATH_UPDATES.process(dyingEntity, causeTracker, phaseContext, entities));

            // TODO Handle block changes
        }

    }

    EntityPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        // For now, short circuit and ignore block changes (they're passing right through currently)
        return false;
    }

}