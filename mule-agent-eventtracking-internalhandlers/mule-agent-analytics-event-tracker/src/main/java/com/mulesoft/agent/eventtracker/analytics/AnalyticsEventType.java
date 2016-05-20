/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracker.analytics;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

public enum AnalyticsEventType {

    ASYNC_START("AsyncMessageNotification", "async process scheduled"),
    ASYNC_END("AsyncMessageNotification", "async process complete"),

    COMPONENT_PRE_INVOKE("ComponentMessageNotification", "component pre invoke"),
    COMPONENT_POST_INVOKE("ComponentMessageNotification", "component post invoke"),

    MESSAGE_RECEIVE("EndpointMessageNotification", "receive"),
    MESSAGE_RESPONSE("EndpointMessageNotification", "response"),

    MESSAGE_DISPATCH_BEGIN("EndpointMessageNotification", "begin dispatch"),
    MESSAGE_DISPATCH_END("EndpointMessageNotification", "end dispatch"),

    MESSAGE_SEND_BEGIN("EndpointMessageNotification", "begin send"),
    MESSAGE_SEND_END("EndpointMessageNotification", "end send"),

    MESSAGE_REQUEST_BEGIN("EndpointMessageNotification", "begin request"),
    MESSAGE_REQUEST_END("EndpointMessageNotification", "end request"),

    EXCEPTION_PROCESS_START("ExceptionStrategyNotification", "exception strategy process start"),
    EXCEPTION_PROCESS_END("ExceptionStrategyNotification", "exception strategy process end"),

    MESSAGE_PROCESSOR_PRE_INVOKE("MessageProcessorNotification", "message processor pre invoke"),
    MESSAGE_PROCESSOR_POST_INVOKE("MessageProcessorNotification", "message processor post invoke"),

    PROCESS_START("FlowMessageNotification", "pipeline process start"),
    PROCESS_COMPLETE("FlowMessageNotification", "pipeline process complete"),
    PROCESS_END("FlowMessageNotification", "pipeline request message processing end"),

    BATCH_START("BatchMessageNotification", "Batch Start"),
    BATCH_COMPLETE("BatchMessageNotification", "Batch Complete"),
    BATCH_END("BatchMessageNotification", "Batch End"),

    TRANSACTION_BEGIN("TransactionNotification", "begin"),
    TRANSACTION_COMMIT("TransactionNotification", "commit"),
    TRANSACTION_ROLLBACK("TransactionNotification", "rollback"),

    CUSTOM_EVENT_ACTION("EventNotification", "custom event"),

    EXCEPTION_ACTION("ExceptionNotification", "exception");

    private static final String PIPELINE_MESSAGE_NOTIFICATION_TYPE = "PipelineMessageNotification";

    private static final String FLOW_MESSAGE_NOTIFICATION_TYPE = "FlowMessageNotification";

    private static final String BATCH_MESSAGE_NOTIFICATION_TYPE = "BatchMessageNotification";

    private static final String FLOW_PIPELINE_TYPE = "Flow";

    private static final String BATCH_PIPELINE_TYPE = "Batch";

    private String notificationType;

    private String action;

    AnalyticsEventType(String notificationType, String action) {
        this.notificationType = notificationType;
        this.action = action;
    }

    public static AnalyticsEventType getAnalyticsEventType(AgentTrackingNotification notification) {
        String notificationType = notification.getNotificationType();
        String action = notification.getAction();

        if (notification.getNotificationType().equals(PIPELINE_MESSAGE_NOTIFICATION_TYPE)) {
            if (notification.getPipelineType().equals(FLOW_PIPELINE_TYPE)) {
                notificationType = FLOW_MESSAGE_NOTIFICATION_TYPE;
            } else if (notification.getPipelineType().equals(BATCH_PIPELINE_TYPE)) {
                notificationType = BATCH_MESSAGE_NOTIFICATION_TYPE;
            }
        }
        for (AnalyticsEventType type : AnalyticsEventType.values()) {
            if (type.notificationType.equals(notificationType) && type.action.equals(action)) {
                return type;
            }
        }
        return null;
    }
}
