/*
 * Copyright (c) 2015 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.util.Arrays;

import javax.annotation.Nullable;

public class Bitset {

    private long[] bits;

    private int length;

    public Bitset(int length) {
        checkArgument(length >= 0, "Bitset length cannot be negative");
        this.bits = new long[length / 64 + 1];
        this.length = length;
    }

    public Bitset(int length, long[] data) {
        checkNotNull(data);
        checkArgument(length >= 0, "Bitset length cannot be negative");
        this.bits = Arrays.copyOf(data, length / 64 + 1);
        this.length = length;
        resize(length);
    }

    public Bitset(Bitset other) {
        checkNotNull(other);
        this.bits = Arrays.copyOf(other.bits, other.length / 64 + 1);
        this.length = other.length;
    }

    public int getLength() {
        return this.length;
    }

    public void set(int x, boolean state) {
        if (state) {
            set(x);
        } else {
            unset(x);
        }
    }

    public void set(int z) {
        if (z >= this.length || z < 0) {
            throw new ArrayIndexOutOfBoundsException(z);
        }
        this.bits[z / 64] = this.bits[z / 64] | (long) (1 << (z % 64));
    }

    public void unset(int z) {
        if (z >= this.length || z < 0) {
            throw new ArrayIndexOutOfBoundsException(z);
        }
        this.bits[z / 64] = this.bits[z / 64] & (long) ~(1 << (z % 64));
    }

    public boolean get(int z) {
        if (z >= this.length || z < 0) {
            throw new ArrayIndexOutOfBoundsException("Tried to set point outside of the shape: " + z);
        }
        return ((this.bits[z / 64] >> z % 64) & 1) == 1;
    }

    void resize(int nl) {
        if (this.bits.length == nl / 64 + 1) {
            return;
        }
        long[] newarray = new long[nl / 64 + 1];
        System.arraycopy(this.bits, 0, newarray, 0, Math.min(nl / 64 + 1, this.length / 64 + 1));
    }

    public void union(Bitset other) {
        checkNotNull(other);
        if (other.getLength() > getLength()) {
            resize(other.getLength());
        }
        for (int z = 0; z < Math.min(other.getLength() / 64 + 1, this.length / 64 + 1); z++) {
            this.bits[z] = this.bits[z] | other.bits[z];
        }
    }

    public void subtract(Bitset other) {
        checkNotNull(other);
        if (other.getLength() > getLength()) {
            resize(other.getLength());
        }
        for (int z = 0; z < Math.min(other.getLength() / 64 + 1, this.length / 64 + 1); z++) {
            this.bits[z] = this.bits[z] | ~other.bits[z];
        }
    }

    public void intersect(Bitset other) {
        checkNotNull(other);
        if (other.getLength() > getLength()) {
            resize(other.getLength());
        }
        for (int z = 0; z < Math.min(other.getLength() / 64 + 1, this.length / 64 + 1); z++) {
            this.bits[z] = this.bits[z] & other.bits[z];
        }
    }

    public void xor(Bitset other) {
        checkNotNull(other);
        if (other.getLength() > getLength()) {
            resize(other.getLength());
        }
        for (int z = 0; z < Math.min(other.getLength() / 8 + 1, this.length / 8 + 1); z++) {
            this.bits[z] = (this.bits[z] & ~other.bits[z]) | (~this.bits[z] & other.bits[z]);
        }
    }

    public void invert() {
        for (int z = 0; z < this.length / 8 + 1; z++) {
            this.bits[z] = ~this.bits[z];
        }
    }

    public long[] toArray() {
        return this.bits;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("length", this.length).toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Bitset)) {
            return false;
        }
        Bitset b = (Bitset) o;
        if (this.length != b.length) {
            return false;
        }
        for (int z = 0; z < this.length / 64; z++) {
            if (this.bits[z] != b.bits[z]) {
                return false;
            }
        }
        for (int z = -(this.length % 64); z < 0; z++) {
            if (get(this.length + z) != b.get(this.length + z)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 37 + this.length;
        for (int i = 0; i < this.bits.length; i++) {
            hash = hash * 37 + (int) (this.bits[i] ^ (this.bits[i] >>> 32));
        }
        return hash;
    }
}
