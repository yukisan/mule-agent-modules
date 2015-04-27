package com.mulesoft.agent.eventtracking.log;

import com.mulesoft.agent.common.internalhandlers.AbstractLogInternalHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Log Internal handler will store all the Event Notifications produced from the
 * Mule ESB flows in a configurable log file with a rolling file policy.
 * </p>
 */
@Singleton
@Named("mule.agent.tracking.handler.log")
public class EventTrackingLogInternalHandler extends AbstractLogInternalHandler<AgentTrackingNotification>
{
    @Override
    public String getTimestampGetterName ()
    {
        return "getTimestamp";
    }
}
