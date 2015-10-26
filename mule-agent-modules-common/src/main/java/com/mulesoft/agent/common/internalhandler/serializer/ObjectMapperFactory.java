package com.mulesoft.agent.common.internalhandler.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperFactory
{
    ObjectMapper create();
}
