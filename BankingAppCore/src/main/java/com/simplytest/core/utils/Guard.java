package com.simplytest.core.utils;

import java.util.concurrent.locks.Lock;

public class Guard implements AutoCloseable
{
    private boolean isClosed = false;
    private final Lock lock;

    public Guard(Lock lock)
    {
        this.lock = lock;
        lock.lock();
    }

    @Override
    public void close()
    {
        if (isClosed)
        {
            return;
        }

        isClosed = true;
        lock.unlock();
    }
}
