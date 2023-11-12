package com.simplytest.server.utils;

import java.util.Optional;

import com.simplytest.server.api.ApiError;

public class Result<R, E>
{
    private Optional<R> result;
    private Optional<ApiError<E>> error;

    private Result(Optional<R> result, Optional<ApiError<E>> error)
    {
        this.error = error;
        this.result = result;
    }

    public boolean successful()
    {
        if (result == null)
        {
            return false;
        }

        return result.isPresent();
    }

    public R value()
    {
        return result.get();
    }

    public ApiError<E> error()
    {
        return error.get();
    }

    public static <R, E> Result<R, E> error(E error)
    {
        return new Result<R, E>(Optional.empty(),
                Optional.of(new ApiError<>(error)));
    }

    public static <R, E> Result<R, E> error(ApiError<E> error)
    {
        return new Result<R, E>(Optional.empty(), Optional.of(error));
    }

    public static <R, E> Result<Boolean, E> success()
    {
        return new Result<Boolean, E>(Optional.of(true), Optional.empty());
    }

    public static <R, E> Result<R, E> success(R result)
    {
        return new Result<R, E>(Optional.of(result), Optional.empty());
    }
}
