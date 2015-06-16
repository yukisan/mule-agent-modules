package com.mulesoft.agent.common.serializers;

import javax.validation.constraints.NotNull;

import java.util.Map;

public class MapTypeSerializer implements TypeSerializer
{
    @Override
    public String serialize (@NotNull Object value)
    {
        Map map = ((Map) value);
        StringBuilder mapBuilder = new StringBuilder("{");
        for (Object key : map.keySet())
        {
            mapBuilder.append(String.format("\"%1$s\":\"%2$s\", ", key, map.get(key)));
        }
        // Remove the last comma
        if (mapBuilder.length() > 1)
        {
            mapBuilder.delete(mapBuilder.length() - 2, mapBuilder.length());
        }
        mapBuilder.append("}");
        return mapBuilder.toString();
    }
}
