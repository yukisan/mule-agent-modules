/*
* (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
* law. All use of this software is subject to MuleSoft's Master Subscription Agreement
* (or other master license agreement) separately entered into in writing between you and
* MuleSoft. If such an agreement is not in place, you may not use the software.
*/

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.handlers.InternalMessageHandler;
import com.mulesoft.agent.services.OnOffSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractMonitorPublisher<M> implements InternalMessageHandler<M>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractMonitorPublisher.class);

    @Configurable("true")
    protected boolean enabled;

    @PostConfigure
    public void createSwitcher()
    {
        enabledSwitch = OnOffSwitch.newNullSwitch(enabled);
    }

    @Override
    public void enable(boolean state) throws AgentEnableOperationException
    {
        enabledSwitch.switchTo(state);
    }

    @Override
    public boolean isEnabled()
    {
        return enabledSwitch.isEnabled();
    }

    protected OnOffSwitch enabledSwitch;

    @Override
    public boolean handle(M metrics)
    {
        if (this.isEnabled())
        {
            return doHandle(metrics);
        }

        LOGGER.debug("Skipped handling of message, handler is not enabled");

        return false;
    }

    protected abstract boolean doHandle(M metrics);

}
