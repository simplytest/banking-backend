package com.simplytest.core.utils;

import java.util.Optional;

public class Expected<ResultType, ErrorType>
{
    private Optional<ResultType> result;
    private Optional<ErrorType> error;

    protected Expected(Optional<ResultType> result, Optional<ErrorType> error)
    {
        this.result = result;
        this.error = error;
    }

    public boolean successful()
    {
        return result.isPresent();
    }

    public ResultType value()
    {
        return result.get();
    }

    public ErrorType error()
    {
        return error.get();
    }

    public static <R, E> Expected<R, E> error(E error)
    {
        return new Expected<R, E>(Optional.empty(), Optional.of(error));
    }

    public static <R, E> Expected<R, E> success(R result)
    {
        return new Expected<R, E>(Optional.of(result), Optional.empty());
    }
}