package cn.alphabets.light.model;

import cn.alphabets.light.Environment;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ModBaseTest
 * Created by lilin on 2016/11/19.
 */
public class ModBaseTest {

    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
    }

    @Test
    public void testToDocument() {

        ObjectId id  = new ObjectId("000000000000000000001234");

        ModDaughter daughter = new ModDaughter();
        daughter.setK(id);

        Assert.assertEquals(id, ModBase.toDocument(daughter).getObjectId("k"));

        ModGrandson grandson = new ModGrandson();
        grandson.setA("A");
        grandson.setH(10L);
        grandson.setJ(new Date());

        Assert.assertEquals("A", ModBase.toDocument(grandson).getString("a"));

        ModSon son = new ModSon();
        son.setA("B");
        son.setB(daughter);
        son.setC(Arrays.asList("C", "D"));
        son.setD(Arrays.asList(daughter, daughter));
        son.setE(Arrays.asList(Arrays.asList("E", "F"), Arrays.asList("G", "H")));
        son.setF(Arrays.asList(Arrays.asList(daughter, daughter), Arrays.asList(daughter, daughter)));
        son.setG(Arrays.asList("I", daughter));
        son.setI(true);

        Document doc = ModBase.toDocument(son);
        Assert.assertEquals(id, ((Document)doc.get("b")).get("k"));
        Assert.assertEquals("I", ((List)doc.get("g")).get(0));
        Assert.assertTrue(doc.getBoolean("I"));
    }

    public static class ModSon extends ModBase {
        private String a;
        private ModDaughter b;
        private List<String> c;
        private List<ModDaughter> d;
        private List<List<String>> e;
        private List<List<ModDaughter>> f;
        private List<Object> g;

        @JsonProperty("I")
        private Boolean i;

        public Boolean getI() {
            return i;
        }

        public void setI(Boolean i) {
            this.i = i;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public ModDaughter getB() {
            return b;
        }

        public void setB(ModDaughter b) {
            this.b = b;
        }

        public List<String> getC() {
            return c;
        }

        public void setC(List<String> c) {
            this.c = c;
        }

        public List<ModDaughter> getD() {
            return d;
        }

        public void setD(List<ModDaughter> d) {
            this.d = d;
        }

        public List<List<String>> getE() {
            return e;
        }

        public void setE(List<List<String>> e) {
            this.e = e;
        }

        public List<List<ModDaughter>> getF() {
            return f;
        }

        public void setF(List<List<ModDaughter>> f) {
            this.f = f;
        }

        public List<Object> getG() {
            return g;
        }

        public void setG(List<Object> g) {
            this.g = g;
        }
    }

    public static class ModGrandson extends ModSon {
        private Long h;
        private Date j;

        public Long getH() {
            return h;
        }

        public void setH(Long h) {
            this.h = h;
        }

        public Date getJ() {
            return j;
        }

        public void setJ(Date j) {
            this.j = j;
        }
    }

    public static class ModDaughter extends Entity {

        private ObjectId k;

        public ObjectId getK() {
            return k;
        }

        public void setK(ObjectId k) {
            this.k = k;
        }

    }
}
