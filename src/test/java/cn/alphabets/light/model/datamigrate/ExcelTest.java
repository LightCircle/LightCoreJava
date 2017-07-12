package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.entity.ModEtl;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel
 * Created by lilin on 2017/7/11.
 */
public class ExcelTest {

    @Test
    public void testParse() throws IOException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("ExcelTest.xlsx");

        List<ModEtl.Mappings> mappings = getMockMappings();
        List<Document> excel = new Excel().parse(inputStream, mappings);
        Assert.assertEquals(4, excel.size());

        System.out.println(excel);
    }

    @Test
    public void testDump() throws IOException {

        String file = "test.xlsx";
        OutputStream outputStream = new FileOutputStream(file);

        List<String> row;
        List<List<String>> data = new ArrayList<>();

        row = new ArrayList<>();
        row.add("1");
        row.add("2");
        row.add("3");
        row.add("4");
        row.add("5");
        data.add(row);

        row = new ArrayList<>();
        row.add("A");
        row.add("B");
        row.add("C");
        row.add("D");
        row.add("E");
        data.add(row);

        row = new ArrayList<>();
        row.add("中文");
        row.add("大连");
        row.add("企鹅");
        row.add("支持");
        row.add("管理者");
        data.add(row);

        new Excel().dump(outputStream, data);
        Assert.assertTrue(new File(file).delete());
    }

    public static List<ModEtl.Mappings> getMockMappings() {
        ModEtl.Mappings mapping;
        List<ModEtl.Mappings> mappings = new ArrayList<>();

        mapping = new ModEtl.Mappings();
        mapping.setCol(0L);
        mapping.setKey("A");
        mappings.add(mapping);

        mapping = new ModEtl.Mappings();
        mapping.setCol(1L);
        mapping.setKey("B");
        mappings.add(mapping);

        mapping = new ModEtl.Mappings();
        mapping.setCol(2L);
        mapping.setKey("C");
        mappings.add(mapping);

        mapping = new ModEtl.Mappings();
        mapping.setCol(3L);
        mapping.setKey("D");
        mappings.add(mapping);
        return mappings;
    }

}
