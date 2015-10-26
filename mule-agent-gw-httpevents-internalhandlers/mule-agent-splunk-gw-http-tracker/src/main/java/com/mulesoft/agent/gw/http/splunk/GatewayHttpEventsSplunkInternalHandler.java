package com.mulesoft.agent.gw.http.splunk;

import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.module.client.model.HttpEvent;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Splunk Internal handler will store all the HTTP Events produced from the Mule API Gateway in Splunk instance.
 * </p>
 */
@Singleton
@Named("mule.agent.gw.http.handler.splunk")
public class GatewayHttpEventsSplunkInternalHandler extends AbstractSplunkInternalHandler<HttpEvent>
{
}
