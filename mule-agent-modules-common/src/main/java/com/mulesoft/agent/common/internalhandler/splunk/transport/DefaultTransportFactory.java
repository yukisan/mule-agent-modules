/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.mulesoft.agent.AgentConfigurationException;
import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.HECTransportConfig;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.RestTransportConfig;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.TCPTransportConfig;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * Factory to create the specific transports.
 * In this version in order to maintain backward compatibility uses a set of checks on the Splunk Internal Handler fields.
 * </p>
 *
 * @author Walter Poch
 *         created on 10/28/15
 * @since 1.3.0
 */
public class DefaultTransportFactory<T> implements TransportFactory<T>
{
    private final AbstractSplunkInternalHandler<T> internalHandler;

    public DefaultTransportFactory(AbstractSplunkInternalHandler<T> internalHandler)
    {
        this.internalHandler = internalHandler;
    }

    @Override
    public Transport<T> create() throws AgentConfigurationException
    {
        if (StringUtils.isNotBlank(this.internalHandler.token))
        {
            HECTransportConfig config = new HECTransportConfig.Builder(this.internalHandler).build();
            return new HECTransport<T>(config, this.internalHandler.getObjectMapper());
        }

        if (this.internalHandler.scheme.equals("tcp"))
        {
            TCPTransportConfig config = new TCPTransportConfig.Builder(this.internalHandler).build();
            return new TCPTransport<T>(config, this.internalHandler.getObjectMapper());
        }

        // Default old behavior, REST
        RestTransportConfig config = new RestTransportConfig.Builder(this.internalHandler).build();
        return new RestTransport<T>(config, this.internalHandler.getObjectMapper());
    }
}
