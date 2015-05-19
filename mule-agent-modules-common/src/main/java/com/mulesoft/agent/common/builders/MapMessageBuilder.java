package com.mulesoft.agent.common.builders;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.message.MapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class MapMessageBuilder implements MessageBuilder<MapMessage>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(MapMessageBuilder.class);

    private final Class inputType;
    private String timestampGetterName;
    private SimpleDateFormat dateFormat;
    private Method timestampGetter;

    public MapMessageBuilder (String timestampGetterName, String dateFormatPattern, Class inputType)
    {
        this.timestampGetterName = timestampGetterName;
        this.inputType = inputType;
        try
        {
            this.dateFormat = new SimpleDateFormat(dateFormatPattern);
            this.dateFormat.format(new Date());
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("There was an error using the dateFormatPattern you provided: %s" +
                    "Please review the configuration.", dateFormatPattern), e);
            this.dateFormat = null;
        }
    }

    private Method obtainGetter ()
    {
        try
        {
            return this.inputType.getMethod(this.timestampGetterName);
        }
        catch (Exception e)
        {
            this.timestampGetterName = null;
            LOGGER.warn("There was an error reading the timestamp getter. This formattedDate feature will be disabled.", e);
        }
        return null;
    }

    public String getDefaultPattern() {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = FieldUtils.getAllFields(inputType);
        for(int i = 0; i < fields.length; i++) {
            sb.append(String.format("\"%1$s\": \"%%map{%1$s}\"", fields[i].getName()));
            if(i < fields.length - 1){
                sb.append(", ");
            }
        }

        sb.append("}%n");
        return sb.toString();
    }

    @Override
    public MapMessage build (Object message)
    {
        // First check if the getter is initialized
        if (this.timestampGetterName != null && this.timestampGetter == null)
        {
            this.timestampGetter = obtainGetter();
        }

        BeanMap beanMap = new BeanMap(message);
        Iterator<Map.Entry<Object, Object>> entryIterator = beanMap.entryIterator();
        MapMessage mapMessage = new MapMessage();
        while (entryIterator.hasNext())
        {
            Map.Entry<Object, Object> element = entryIterator.next();
            Object value = element.getValue();
            if (value == null)
            {
                value = "";
            }
            mapMessage.put(element.getKey().toString(), value.toString());
        }
        if (this.timestampGetter != null && dateFormat != null)
        {
            // Replace the timestamp value with a formatted one
            try
            {
                long timestamp = (long) this.timestampGetter.invoke(message);
                Date date = new Date(timestamp);
                String formattedDate = this.dateFormat.format(date);
                mapMessage.put("timestamp", formattedDate);
            }
            catch (Exception e)
            {
                LOGGER.warn(String.format("There was an error reading the timestamp: %s", this.timestampGetterName), e);
            }
        }

        return mapMessage;
    }
}
