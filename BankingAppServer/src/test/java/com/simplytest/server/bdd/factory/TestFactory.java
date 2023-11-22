package com.simplytest.server.bdd.factory;

import com.simplytest.server.bdd.context.World;

public class TestFactory
{
    protected static World world;

    public TestFactory()
    {
        if (world != null)
        {
            return;
        }

        world = new World();
    }
}
