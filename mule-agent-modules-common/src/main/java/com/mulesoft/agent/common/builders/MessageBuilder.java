package com.mulesoft.agent.common.builders;

public interface MessageBuilder<TMessage, TFinal>
{
    TFinal build (TMessage message);
}
