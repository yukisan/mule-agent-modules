package com.mulesoft.agent.common.serializers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTypeSerializer implements TypeSerializer
{
    private final static Logger LOGGER =
            LoggerFactory.getLogger(DateTypeSerializer.class);

    private SimpleDateFormat dateFormat;

    public DateTypeSerializer (@NotNull String dateFormatPattern)
    {
        try
        {
            this.dateFormat = new SimpleDateFormat(dateFormatPattern);
            this.dateFormat.format(new Date());
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("There was an error using the dateFormatPattern " +
                    "you provided: %s. Please review the configuration.", dateFormatPattern), e);
            this.dateFormat = null;
        }
    }

    @Override
    public String serialize (@NotNull Object value)
    {
        if (this.dateFormat == null)
        {
            return "\"" + value.toString() + "\"";
        }
        Date date = (Date) value;
        return "\"" + this.dateFormat.format(date) + "\"";
    }
}
