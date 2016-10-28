package cn.alphabets.light.model.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by luohao on 2016/10/27.
 */
public class DateDeserializer extends JsonDeserializer<Date> {
    static SimpleDateFormat formatter;

    static {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * json 转 Date
     * eg:
     * "2015-07-01T06:55:42.696Z" -> Date
     * "{"$date":1471507601964}"  -> Date
     *
     * @param p
     * @param ctxt
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.readValueAsTree();
        if (node instanceof NullNode) {
            return null;
        }
        if (node instanceof TextNode) {
            try {
                return formatter.parse(node.asText());
            } catch (ParseException e) {
                throw new JsonParseException(p, node.asText(), e);
            }
        }
        if (node instanceof ObjectNode) {
            long timestamp = node.get("$date").asLong();
            return new Date(timestamp);
        }
        throw new JsonParseException(p, "can not deserialize node to Date: " + node);

    }
}
