/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Walter Poch
 *         created on 10/27/15
 */
public abstract class AbstractTransport<T> implements Transport<T>
{
    protected final static String CHARSET = "UTF-8";
    protected final static String LINE_BREAKER = "\r\n";

    private final ObjectMapper objectMapper;

    protected AbstractTransport(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    protected ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }
}
