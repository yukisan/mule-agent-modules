package com.mulesoft.agent.common.serializers;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

public class TypeSerializerFactory
{
    private final TypeSerializer mapSerializer;
    private final TypeSerializer iterableSerializer;
    private final TypeSerializer genericSerializer;
    private final TypeSerializer nullSerializer;
    private final TypeSerializer dateSerializer;

    public TypeSerializerFactory (@NotNull String dateFormatPattern)
    {
        this.mapSerializer = new MapTypeSerializer();
        this.iterableSerializer = new IterableTypeSerializer();
        this.genericSerializer = new GenericTypeSerializer();
        this.nullSerializer = new NullTypeSerializer();
        this.dateSerializer = new DateTypeSerializer(dateFormatPattern);
    }

    public TypeSerializer create (Object value)
    {
        if (value == null)
        {
            return nullSerializer;
        }
        if (Date.class.isAssignableFrom(value.getClass()))
        {
            return dateSerializer;
        }
        if (Map.class.isAssignableFrom(value.getClass()))
        {
            return mapSerializer;
        }
        if (Iterable.class.isAssignableFrom(value.getClass()))
        {
            return iterableSerializer;
        }
        return genericSerializer;
    }
}
