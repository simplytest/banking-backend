package com.simplytest.core.utils;

import java.util.Optional;

public class Result<E extends Enum<E>> extends Expected<Boolean, E>
{
    private Result(Optional<Boolean> result, Optional<E> error)
    {
        super(result, error);
    }

    public static <E extends Enum<E>> Result<E> error(E error)
    {
        return new Result<E>(Optional.empty(), Optional.of(error));
    }

    public static <E extends Enum<E>> Result<E> success()
    {
        return new Result<E>(Optional.of(true), Optional.empty());
    }
}