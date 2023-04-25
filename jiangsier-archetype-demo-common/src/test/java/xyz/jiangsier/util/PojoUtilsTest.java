package xyz.jiangsier.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PojoUtilsTest {
    @SuppressWarnings("unused")
    static class Entry {
        private final String v;
        private Demo host;
        private Entry next;

        public Entry(String v) {
            this.v = v;
        }

        public Entry setHost(Demo host) {
            this.host = host;
            return this;
        }

        public Entry setNext(Entry next) {
            this.next = next;
            return this;
        }
    }

    @SuppressWarnings("unused")
    static class Demo {
        private final List<Entry> l = new LinkedList<>();

        public Demo() {
            l.add(new Entry("1").setHost(this)
                    .setNext(new Entry("12")
                            .setNext(new Entry("123")
                                    .setNext(new Entry("1234")
                                            .setNext(new Entry("12345")
                                                    .setNext(new Entry("123456")))))));
        }

        public List<Entry> getL() {
            return l;
        }
    }

    private String toPattern(String orig) {
        return orig.replaceAll("\\{", "\\\\{")
                .replaceAll("\\}", "\\\\}")
                .replaceAll("\\[", "\\\\[")
                .replaceAll("\\]", "\\\\]");
    }

    @Test
    public void testCircleAndDepth() {
        Demo demo = new Demo();
        String objInfo = PojoUtils.object2JsonString(demo, false);
        Pattern p = Pattern.compile(toPattern("{\"l\":[{\"v\":\"1\",\"host\":\"Demo@\\w+\",\"next\":{\"v\":\"12\",\"host\":null,\"next\":{\"v\":\"123\",\"host\":null,\"next\":{\"v\":\"1234\",\"host\":null,\"next\":{\"v\":\"12345\",\"host\":null,\"next\":\"Entry@\\w+\"}}}}}]}"));
        Matcher m = p.matcher(objInfo);
        Assert.assertTrue(m.matches());
    }
}
