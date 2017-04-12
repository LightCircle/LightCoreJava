package cn.alphabets.light.model.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.io.IOException;

/**
 * Double Deserializer
 * Created by lilin on 2016/10/27.
 */
public class DoubleDeserializer extends JsonDeserializer<Double> {

    /**
     * Json turn Double
     *
     * @param p    p
     * @param ctxt context
     * @return Double value
     * @throws IOException error
     */
    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        if (node instanceof NullNode) {
            return 0.;
        }
        if (node instanceof IntNode) {
            return node.asDouble();
        }
        if (node instanceof FloatNode) {
            return node.asDouble();
        }
        if (node instanceof DoubleNode) {
            return node.asDouble();
        }
        if (node instanceof TextNode) {
            return Double.valueOf(node.asText());
        }
        if (node instanceof ObjectNode) {
            return Double.valueOf(node.get("$numberDouble").asText());
        }
        throw new JsonParseException(p, "can not deserialize node to Double : " + node);
    }
}
