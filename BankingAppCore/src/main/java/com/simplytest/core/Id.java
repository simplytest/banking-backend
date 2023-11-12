package com.simplytest.core;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import com.simplytest.core.utils.Expected;

public class Id
{
    private AtomicLong counter = new AtomicLong();

    private final long parent;
    private final long child;

    public Id(long parent)
    {
        this.parent = parent;
        this.child = 0;
    }

    public Id(long parent, long child)
    {
        this.counter.set(child);
        this.parent = parent;
        this.child = child;
    }

    public Id(Id other)
    {
        this.counter.set(other.counter.get());
        this.parent = other.parent;
        this.child = other.child;
    }

    public long parent()
    {
        return parent;
    }

    public long child()
    {
        return child;
    }

    public Id create()
    {
        return new Id(parent, counter.incrementAndGet());
    }

    public static Expected<Id, Boolean> from(String id)
    {
        final var pattern = Pattern.compile("(\\d+):(\\d+)");
        var match = pattern.matcher(id);

        if (!match.find())
        {
            return Expected.error(false);
        }

        var parent = Long.parseLong(match.group(1));
        var child = Long.parseLong(match.group(2));

        return Expected.success(new Id(parent, child));
    }

    @Override
    public String toString()
    {
        return String.format("%05d:%05d", parent, child);
    }

    @Override
    public Id clone()
    {
        return new Id(this);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(parent, child);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }

        var otherId = (Id) other;

        if (otherId.parent != parent || otherId.child != child)
        {
            return false;
        }

        return true;
    }
}