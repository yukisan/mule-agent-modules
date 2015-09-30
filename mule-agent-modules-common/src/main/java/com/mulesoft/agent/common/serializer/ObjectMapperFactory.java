package com.mulesoft.agent.common.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperFactory
{
    ObjectMapper create();
}
