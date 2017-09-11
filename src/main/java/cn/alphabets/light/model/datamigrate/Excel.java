package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.entity.ModEtl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel
 * Created by lilin on 2017/7/11.
 */
public class Excel {

    private static final Logger logger = LoggerFactory.getLogger(Excel.class);

    private String findKey(List<ModEtl.Mappings> mappings, int index) {
        ModEtl.Mappings mapping = mappings.stream()
                .filter(item -> item.getCol() == index)
                .findFirst()
                .orElse(null);

        if (mapping == null) {
            return "_no_mapping_" + index;
        }
        return mapping.getKey();
    }

    /**
     * 加载Excel
     * @param excel Excel文件
     * @param mappings 列与字段的对应关系
     * @return Excel content
     * @throws IOException exception
     */
    public List<Document> parse(InputStream excel, List<ModEtl.Mappings> mappings) throws IOException {

        Workbook workbook = new XSSFWorkbook(excel);
        List<Document> result = new ArrayList<>();

        workbook.sheetIterator().forEachRemaining(sheet -> {
            sheet.iterator().forEachRemaining(row -> {

                // 第一行认为是标题
                if (row.getRowNum() == 0) {
                    return;
                }

                Document document = new Document();
                row.cellIterator().forEachRemaining(cell -> {

                    String value = "";
                    String key = this.findKey(mappings, cell.getColumnIndex() + 1); // 定义中的col是从1开始计数

                    switch (cell.getCellTypeEnum()) {
                        case NUMERIC:
                            value = String.valueOf(cell.getNumericCellValue());
                            break;
                        case STRING:
                            value = cell.getStringCellValue();
                            break;
                        case FORMULA:
                            value = cell.getCellFormula();
                            break;
                        case BOOLEAN:
                            value = String.valueOf(cell.getBooleanCellValue());
                            break;
                        default:
                            logger.warn("The cell can not be converted correctly. ", cell.getCellTypeEnum());
                            break;
                    }

                    document.put(key, value);
                });

                result.add(document);
            });
        });

        workbook.close();
        return result;
    }

    /**
     * 生成Excel
     * @param excel Excel文件
     * @param data 保存到Excel里的数据
     * @throws IOException exception
     */
    public void dump(OutputStream excel, List<List<String>> data) throws IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("data");

        AtomicInteger rowIndex = new AtomicInteger();
        data.forEach(item -> {

            Row row = sheet.createRow(rowIndex.getAndIncrement());

            AtomicInteger colIndex = new AtomicInteger();
            item.forEach(value -> {
                Cell cell = row.createCell(colIndex.getAndIncrement());
                cell.setCellValue(value);
            });
        });

        wb.write(excel);
    }
}
