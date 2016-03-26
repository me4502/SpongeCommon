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
package org.spongepowered.common.event.tracking;

import org.spongepowered.api.event.cause.NamedCause;

import java.util.Optional;
import java.util.stream.Stream;

final class UnwindingPhaseContext extends PhaseContext {

    static PhaseContext unwind(IPhaseState state, PhaseContext context) {
        return new UnwindingPhaseContext(state, context);
    }

    UnwindingPhaseContext(IPhaseState unwindingState, PhaseContext unwindingContext) {
        add(NamedCause.of(TrackingUtil.UNWINDING_CONTEXT, unwindingContext));
        add(NamedCause.of(TrackingUtil.UNWINDING_STATE, unwindingState));
    }


    @Override
    public <T> Optional<T> first(Class<T> tClass) {
        return Stream.of(super.first(tClass), super.firstNamed(TrackingUtil.UNWINDING_CONTEXT, PhaseContext.class).get().first(tClass))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public <T> Optional<T> firstNamed(String name, Class<T> tClass) {
        return Stream.of(super.firstNamed(name, tClass), super.firstNamed(TrackingUtil.UNWINDING_CONTEXT, PhaseContext.class).get().firstNamed(name, tClass))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

}