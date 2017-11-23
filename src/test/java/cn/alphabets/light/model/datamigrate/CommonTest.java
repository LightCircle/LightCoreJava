package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.mock.MockRoutingContext;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lilin on 2017/11/23.
 */
public class CommonTest {

    @Test
    public void testGetCondition() {

        // 空参数
        Document data = new Document();
        data.put("uid", null);

        Document document = new Document();
        document.put("data", data);

        Params params = new Params(document);
        String domain = Constant.SYSTEM_DB;
        String code = Constant.SYSTEM_DB_PREFIX;
        String uid = "";

        Context handler = new Context(params, domain, code, uid);

        Map<String, String> conditions = new HashMap<>();
        conditions.put("_id", "$uid");

        ModEtl.Mappings mapping = new ModEtl.Mappings();
        mapping.setConditions(conditions);

        Document condition = Common.getCondition(handler, mapping);
        Assert.assertNull(condition.get("_id"));


        // 空列表
        List<String> uidList = new ArrayList<>();
        uidList.add(null);
        uidList.add("123456789012345678901234");
        uidList.add(null);

        data = new Document();
        data.put("uid", uidList);

        document = new Document();
        document.put("data", data);

        params = new Params(document);
        handler = new Context(params, domain, code, uid);

        conditions = new HashMap<>();
        conditions.put("_id", "$uid");

        mapping = new ModEtl.Mappings();
        mapping.setConditions(conditions);

        condition = Common.getCondition(handler, mapping);
        System.out.println(condition);
        Assert.assertNotNull(((Document)condition.get("_id")).get("$in"));
    }

}
