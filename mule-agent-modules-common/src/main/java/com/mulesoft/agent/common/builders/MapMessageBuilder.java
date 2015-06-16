package com.mulesoft.agent.common.builders;

import com.mulesoft.agent.common.serializers.TypeSerializer;
import com.mulesoft.agent.common.serializers.TypeSerializerFactory;
import org.apache.commons.beanutils.BeanMap;
import org.apache.logging.log4j.message.MapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class MapMessageBuilder implements MessageBuilder<MapMessage>
{
    private final static Logger LOGGER =
            LoggerFactory.getLogger(MapMessageBuilder.class);
    private final TypeSerializerFactory typeSerializerFactory;

    private final Class inputType;
    private String timestampGetterName;
    private Method timestampGetter;

    public MapMessageBuilder (String timestampGetterName, String dateFormatPattern,
                              Class inputType)
    {
        this.typeSerializerFactory = new TypeSerializerFactory(dateFormatPattern);
        this.timestampGetterName = timestampGetterName;
        this.inputType = inputType;
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
            LOGGER.warn("There was an error reading the timestamp getter. " +
                    "This formattedDate feature will be disabled.", e);
        }
        return null;
    }

    public String getDefaultPattern ()
    {
        final StringBuilder sb = new StringBuilder("{");
        ReflectionUtils.doWithFields(inputType, new ReflectionUtils.FieldCallback()
                {
                    @Override
                    public void doWith (Field field)
                            throws IllegalArgumentException, IllegalAccessException
                    {
                        sb.append(String.format("\"%1$s\":%%map{%1$s}, ",
                                field.getName()));
                    }
                }
        );
        // Delete the last comma
        if (sb.length() > 1)
        {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("}%n");
        String pattern = sb.toString();
        LOGGER.debug(String.format("The default pattern used for the type " +
                "'%s' is: %s", this.inputType, pattern));
        return pattern;
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
            TypeSerializer serializer = typeSerializerFactory.create(value);
            String serialized = serializer.serialize(value);
            mapMessage.put(element.getKey().toString(), serialized);
        }

        if (this.timestampGetter != null)
        {
            // Replace the timestamp value with a formatted one
            try
            {
                long timestamp = (long) this.timestampGetter.invoke(message);
                Date date = new Date(timestamp);
                TypeSerializer serializer = typeSerializerFactory.create(date);
                String serialized = serializer.serialize(date);
                mapMessage.put("timestamp", serialized);
            }
            catch (Exception e)
            {
                LOGGER.warn(String.format("There was an error reading the timestamp: %s",
                        this.timestampGetterName), e);
            }
        }

        return mapMessage;
    }
}
