package com.simplytest.server.utils;

public class Updatable<T> implements AutoCloseable
{
    private T value;
    private Runnable update;
    private boolean updated = false;

    private Updatable(T value, Runnable update)
    {
        this.value = value;
        this.update = update;
    }

    public static <T> Updatable<T> of(T value, Runnable update)
    {
        return new Updatable<T>(value, update);
    }

    public T value()
    {
        return value;
    }

    @Override
    public void close()
    {
        if (updated)
        {
            return;
        }

        update.run();
        updated = true;
    }
}
