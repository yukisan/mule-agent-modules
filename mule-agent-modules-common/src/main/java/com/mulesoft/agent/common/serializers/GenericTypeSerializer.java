package com.mulesoft.agent.common.serializers;

import javax.validation.constraints.NotNull;

public class GenericTypeSerializer implements TypeSerializer
{
    @Override
    public String serialize (@NotNull Object value)
    {
        return "\"" + value.toString() + "\"";
    }
}
