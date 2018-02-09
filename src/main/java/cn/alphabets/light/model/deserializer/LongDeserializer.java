package cn.alphabets.light.model.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.io.IOException;

/**
 * Long Deserializer
 * Created by lilin on 2016/10/27.
 */
public class LongDeserializer extends JsonDeserializer<Long> {

    /**
     * Json turn Long
     *
     * @param p    p
     * @param ctxt context
     * @return Long value
     * @throws IOException error
     */
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        if (node instanceof NullNode) {
            return 0L;
        }
        if (node instanceof IntNode) {
            return node.asLong();
        }
        if (node instanceof FloatNode) {
            return node.asLong();
        }
        if (node instanceof DoubleNode) {
            return node.asLong();
        }
        if (node instanceof TextNode) {
            return node.asLong();
        }
        if (node instanceof ObjectNode) {
            return node.get("$numberLong").asLong();
        }
        throw new JsonParseException(p, "can not deserialize node to Long : " + node);
    }
}
