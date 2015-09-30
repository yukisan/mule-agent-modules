package com.mulesoft.agent.common.mixin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mulesoft.agent.common.serializer.TimestampToDateSerializer;

public abstract class AgentTrackingNotificationMixin
{
    @JsonSerialize(using = TimestampToDateSerializer.class)
    abstract long getTimestamp();
}
