package com.mulesoft.agent.eventtracking.splunk;

import com.mulesoft.agent.common.builders.MapMessageBuilder;
import com.mulesoft.agent.common.builders.MessageBuilder;
import com.mulesoft.agent.common.internalhandlers.AbstractSplunkInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.message.MapMessage;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Splunk Internal handler will store all the Event Notifications produced from the Mule ESB flows in Splunk instance.
 * </p>
 */

@Named("mule.agent.tracking.handler.splunk")
@Singleton
public class EventTrackingSplunkInternalHandler extends AbstractSplunkInternalHandler<AgentTrackingNotification>
{
    /**
     * <p>
     * A log4j2 PatternLayout (https://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout).
     * You can print the properties of the object using the %map{key} notation, for example: %map{timestamp}
     * Default: null, so all the properties will be used as a JSON object.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String pattern;

    @Override
    protected String getPattern () {
        if(StringUtils.isEmpty(this.pattern)) {
            return super.getPattern();
        }
        return this.pattern;
    }

    @Override
    public String getTimestampGetterName ()
    {
        return "getTimestamp";
    }

    @Override
    protected MapMessageBuilder getMessageBuilder() {
        return new MapMessageBuilder(this.getTimestampGetterName(), this.dateFormatPattern, AgentTrackingNotification.class);
    }
}
