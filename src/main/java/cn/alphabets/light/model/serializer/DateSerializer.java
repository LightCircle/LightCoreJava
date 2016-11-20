package cn.alphabets.light.model.serializer;

import cn.alphabets.light.Helper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * DateSerializer
 * Created by luohao on 2016/10/27.
 */
public class DateSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(Helper.toUTCString(value));
    }
}
