package cn.edu.ctgu.se.lab4;

import java.util.ArrayList;
import java.util.List;

/**
 * B3 从源代码中提取双引号字符串（处理反斜杠转义）
 *
 * 状态：SCANNING(字符串外) / IN_STRING(字符串内) / ESCAPED(已遇到反斜杠)
 * 规则：\" \\ \n 等转义序列只保留被转义的那个字符本身；
 *       示例： "输出\"Hello World!\""  =>  输出"Hello World!"
 */
public final class StringExtractor {

    public enum State { SCANNING, IN_STRING, ESCAPED }

    private StringExtractor() { }

    public static List<String> extract(String source) {
        List<String> result = new ArrayList<>();
        State state = State.SCANNING;
        StringBuilder buf = new StringBuilder();
        StringBuilder raw = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            switch (state) {
                case SCANNING:
                    if (ch == '"') {
                        buf.setLength(0);
                        raw.setLength(0);
                        state = State.IN_STRING;
                    }
                    break;

                case IN_STRING:
                    if (ch == '\\') {
                        raw.append(ch);
                        state = State.ESCAPED;
                    } else if (ch == '"') {
                        result.add(buf.toString());
                        state = State.SCANNING;
                    } else {
                        raw.append(ch);
                        buf.append(ch);
                    }
                    break;

                case ESCAPED:
                    // 被转义的字符原样保留（\n 保留 n，\" 保留 "）
                    raw.append(ch);
                    buf.append(ch);
                    state = State.IN_STRING;
                    break;

                default:
                    break;
            }
        }
        if (state == State.ESCAPED) {
            // 末尾悬空的反斜杠，按字面保留
            buf.append('\\');
        }
        return result;
    }

    public static void main(String[] args) {
        String demo = "String s = \"一般学习一门编程语言的时候，基本上第一个程序是输出\\\"Hello World!\\\"\";";
        List<String> r = extract(demo);
        System.out.println("输入: " + demo);
        System.out.println("输出: " + r);
        assertEquals(1, r.size());
        assertEquals("一般学习一门编程语言的时候，基本上第一个程序是输出\"Hello World!\"", r.get(0));

        List<String> r2 = extract("a = \"x\"; b = \"y\\\"z\"; c = \"\";");
        System.out.println("多个字符串: " + r2);
        assertEquals(3, r2.size());
        assertEquals("x", r2.get(0));
        assertEquals("y\"z", r2.get(1));
        assertEquals("", r2.get(2));

        List<String> r3 = extract("没有字符串的内容");
        assertEquals(0, r3.size());

        List<String> r4 = extract("\"path is C:\\\\temp\\\\file.txt\"");
        System.out.println("反斜杠: " + r4);
        assertEquals("path is C:\\temp\\file.txt", r4.get(0));

        System.out.println("\n全部用例通过");
    }

    private static void assertEquals(Object expect, Object actual) {
        if (!expect.equals(actual)) {
            throw new AssertionError("期望 [" + expect + "] 实际 [" + actual + "]");
        }
    }
}
