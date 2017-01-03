package cn.alphabets.light.model;

import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModValidator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by luohao on 2017/1/3.
 */
public class EntiyTest {

    @Test
    public void testGetFieldValue() throws IOException {
        ModBoard board = new ModBoard();

        board.setClass_("class");
        Assert.assertEquals("class", board.getFieldValue("class"));
        System.out.println(board.toDocument().get("class"));

        board.setAction("action");
        Assert.assertEquals("action", board.getFieldValue("action"));
        System.out.println(board.toDocument().get("action"));

        board.setKind(3L);
        Assert.assertEquals(3L, board.getFieldValue("kind"));
        System.out.println(board.toDocument().get("kind"));


        ModValidator validator = new ModValidator();
        ModValidator.Condition condition = new ModValidator.Condition();
        condition.setParameter("condition.parameter");
        validator.setCondition(condition);
        Assert.assertEquals("condition.parameter", validator.getFieldValue("condition.parameter"));


    }
}
