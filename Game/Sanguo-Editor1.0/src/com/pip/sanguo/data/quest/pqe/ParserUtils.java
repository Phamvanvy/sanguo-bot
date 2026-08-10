package com.pip.sanguo.data.quest.pqe;

import java.util.ArrayList;

public class ParserUtils {
    /**
     * 在一个节点下查找指定节点。指定节点必须只有一个。
     */
    public static Node findChild(Node node, Class cls) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (cls.isInstance(child)) {
                return child;
            }
        }
        return null;
    }

    /**
     * 在一个节点下查找指定节点的列表。
     */
    public static Node[] findChildren(Node node, Class cls) {
        ArrayList retList = new ArrayList();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (cls.isInstance(child)) {
                retList.add(child);
            }
        }
        Node[] ret = new Node[retList.size()];
        retList.toArray(ret);
        return ret;
    }

    /**
     * 把字符串常量解释为Java字符串。
     */
    public static String translateStringConstant(String str) {
        // 字符串常量必然开头和结尾都是"
        StringBuffer buf = new StringBuffer();
        char[] data = str.toCharArray();
        for (int i = 1; i < data.length - 1; i++) {
            char ch = data[i];
            if (ch == '\\') {
                switch (data[i + 1]) {
                case 'n':
                    buf.append("\n");
                    break;
                case 'r':
                    buf.append("\r");
                    break;
                case 't':
                    buf.append("\t");
                    break;
                default:
                    buf.append(data[i + 1]);
                    break;
                }
                i++;
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     * 解释整型常量。
     */
    public static int translateNumberConstant(String str) {
        if (str.startsWith("0x") || str.startsWith("0X")) {
            // 16进制
            str = str.substring(2);
            if (str.length() < 8) {
                return Integer.parseInt(str, 16);
            } else {
                int low = Integer.parseInt(str.substring(1), 16);
                int high = Integer.parseInt(str.substring(0, 1), 16);
                return (high << 28) | low;
            }
        } else {
            return Integer.parseInt(str);
        }
    }
}
