package cn.alphabets.light.model.deserializer;

import cn.alphabets.light.Helper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * DateDeserializer
 * Created by luohao on 2016/10/27.
 */
public class DateDeserializer extends JsonDeserializer<Date> {

    /**
     * Json turn Date
     * eg:
     * "2015/05/08 13:33:32.333"  to Date
     * "2015-07-01T06:55:42.696Z" to Date
     * "{"$date":1471507601964}"  to Date
     *
     * @param p   parser
     * @param ctx context
     * @return date
     * @throws IOException error
     */
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctx) throws IOException {


        JsonNode node = p.readValueAsTree();
        if (node instanceof NullNode) {
            return null;
        }
        if (node instanceof TextNode) {

            String dateStr = node.asText();

            try {
                if (dateStr.endsWith("Z")) {
                    return Helper.fromUTCString(dateStr);
                } else {
                    TimeZone timeZone = getTimeZone(ctx);
                    return Helper.fromSupportedString(dateStr, timeZone);
                }
            } catch (Exception e) {
            }
        }
        if (node instanceof ObjectNode) {
            long timestamp = node.get("$date").asLong();
            return new Date(timestamp);
        }
        throw new JsonParseException(p, "can not deserialize node to Date: " + node);
    }


    private TimeZone getTimeZone(DeserializationContext ctx) {
        return ctx.getTimeZone();
    }


}
