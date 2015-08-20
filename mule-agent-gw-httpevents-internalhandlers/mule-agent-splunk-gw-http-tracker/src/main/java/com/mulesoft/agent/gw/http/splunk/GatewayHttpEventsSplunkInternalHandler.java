package com.mulesoft.agent.gw.http.splunk;

import com.mulesoft.agent.common.builders.MapMessageBuilder;
import com.mulesoft.agent.common.internalhandlers.AbstractSplunkInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.module.client.model.HttpEvent;
import org.apache.commons.lang.StringUtils;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Splunk Internal handler will store all the HTTP Events produced from the Mule API Gateway in Splunk instance.
 * </p>
 */

@Named("mule.agent.gw.http.handler.splunk")
@Singleton
public class GatewayHttpEventsSplunkInternalHandler extends AbstractSplunkInternalHandler<HttpEvent>
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
    protected String getPattern ()
    {
        if (StringUtils.isEmpty(this.pattern))
        {
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
    protected MapMessageBuilder getMessageBuilder ()
    {
        return new MapMessageBuilder(this.getTimestampGetterName(), this.dateFormatPattern, HttpEvent.class);
    }
}
