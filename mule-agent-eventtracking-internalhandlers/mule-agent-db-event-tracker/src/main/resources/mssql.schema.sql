CREATE TABLE MULE_EVENTS (
  id                  CHAR(36)     NOT NULL,
  action              VARCHAR(500) NULL,
  application         VARCHAR(500) NULL,
  mule_message        VARCHAR(MAX) NULL,
  mule_message_id     VARCHAR(36)  NULL,
  notification_type   VARCHAR(500) NULL,
  path                VARCHAR(500) NULL,
  resource_identifier VARCHAR(500) NULL,
  timestamp           BIGINT       NOT NULL,
  source              VARCHAR(MAX) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE MULE_EVENTS_ANNOTATIONS (
  id               CHAR(36)     NOT NULL,
  event_id         CHAR(36)     NOT NULL,
  annotation_type  VARCHAR(100) NULL,
  annotation_value VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_MULE_EVENTS_ANNOTATIONS_MULE_EVENTS
  FOREIGN KEY (event_id) REFERENCES MULE_EVENTS (id)
    ON DELETE CASCADE
);

CREATE INDEX FK_MULE_EVENTS_ANNOTATIONS_MULE_EVENTS_IDX ON MULE_EVENTS_ANNOTATIONS (event_id);

CREATE TABLE MULE_EVENTS_BUSINESS (
  id             CHAR(36)     NOT NULL,
  event_id       CHAR(36)     NOT NULL,
  business_key   VARCHAR(30)  NOT NULL,
  business_value VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_MULE_EVENTS_BUSINESS_MULE_EVENTS
  FOREIGN KEY (event_id) REFERENCES MULE_EVENTS (id)
    ON DELETE CASCADE
);

CREATE INDEX FK_MULE_EVENTS_BUSINESS_IDX ON MULE_EVENTS_BUSINESS (event_id);