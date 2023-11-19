package com.simplytest.server.json;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.simplytest.core.Id;

public class IDAdapter implements JsonDeserializer<Id>
{
    @Override
    public final Id deserialize(final JsonElement elem, final Type interfaceType,
            final JsonDeserializationContext context) throws JsonParseException
    {
        if (elem.isJsonObject())
        {
            return new Gson().fromJson(elem.getAsJsonObject(), Id.class);
        }

        if (!elem.isJsonPrimitive())
        {
            throw new JsonParseException("Expected primitive");
        }

        return Id.from(elem.getAsString()).value();
    }
}
