package com.mulesoft.agent.common.serializers;

import javax.validation.constraints.NotNull;

public class IterableTypeSerializer implements TypeSerializer
{
    @Override
    public String serialize (@NotNull Object value)
    {
        Iterable iterable = ((Iterable) value);
        StringBuilder listBuilder = new StringBuilder("[");
        for (Object item : iterable)
        {
            listBuilder.append(String.format("\"%1$s\", ", item));
        }
        // Remove the last comma
        if (listBuilder.length() > 1)
        {
            listBuilder.delete(listBuilder.length() - 2, listBuilder.length());
        }
        listBuilder.append("]");
        return listBuilder.toString();
    }
}
