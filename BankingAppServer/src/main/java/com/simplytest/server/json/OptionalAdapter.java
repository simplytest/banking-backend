package com.simplytest.server.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OptionalAdapter<T>
        implements JsonSerializer<Optional<T>>, JsonDeserializer<Optional<T>>
{
    @Override
    public JsonElement serialize(Optional<T> object, Type type,
            JsonSerializationContext context)
    {
        return context.serialize(object.orElse(null));
    }

    @Override
    public Optional<T> deserialize(JsonElement json, Type type,
            JsonDeserializationContext context) throws JsonParseException
    {
        T value = context.deserialize(json,
                ((ParameterizedType) type).getActualTypeArguments()[0]);

        return Optional.ofNullable(value);
    }
}
