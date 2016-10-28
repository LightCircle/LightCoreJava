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
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Created by luohao on 2016/10/27.
 */
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {


    /**
     * json 转 ObjectId
     * eg:
     * "55530a8a522b337520746efa" -> ObjectId
     * "{"$oid":"57b56c919e0fbd0500572e84"}"  -> ObjectId
     *
     * @param p
     * @param ctxt
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.readValueAsTree();

        if (node instanceof NullNode) {
            return null;
        }

        if (node instanceof TextNode) {
            return new ObjectId(node.asText());
        }

        if (node instanceof ObjectNode) {
            String id = node.get("$oid").asText();
            return new ObjectId(id);
        }
        throw new JsonParseException(p, "can not deserialize node to ObjectId : " + node);
    }
}
