package com.simplytest.server.utils;

import java.util.Optional;

public record ApiError<E>(E error, Optional<String> message)
{
    public ApiError(E error)
    {
        this(error, Optional.empty());
    }

    public ApiError(E error, String message)
    {
        this(error, Optional.of(message));
    }
}
