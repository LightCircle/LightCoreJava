package cn.alphabets.light.model.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * DateSerializer
 * Created by luohao on 2016/10/27.
 */
public class DateSerializer extends JsonSerializer<Date> {

    private static SimpleDateFormat formatter;

    static {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        String formattedDate = formatter.format(value);
        jgen.writeString(formattedDate);
    }
}
