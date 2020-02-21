package com.sjhy.plugin;

/**
 * @author bruce ge
 */
public class GenerateKtTypeMap {
    public static void main(String[] args) {
        String s = "java.lang.Byte\tkotlin.Byte?\n" +
                "                java.lang.Short\tkotlin.Short?\n" +
                "                java.lang.Integer\tkotlin.Int?\n" +
                "                java.lang.Long\tkotlin.Long?\n" +
                "                java.lang.Character\tkotlin.Char?\n" +
                "                java.lang.Float\tkotlin.Float?\n" +
                "                java.lang.Double\tkotlin.Double?\n" +
                "                java.lang.Boolean\tkotlin.Boolean?";

        String d= "byte\tkotlin.Byte\n" +
                "short\tkotlin.Short\n" +
                "int\tkotlin.Int\n" +
                "long\tkotlin.Long\n" +
                "char\tkotlin.Char\n" +
                "float\tkotlin.Float\n" +
                "double\tkotlin.Double\n" +
                "boolean\tkotlin.Boolean";
        String[] split = d.split("\n");
        for (String s1 : split) {
            String[] split1 = s1.split("\t");
            System.out.println("put(\"" + split1[0].trim() + "\"," + "\"" + split1[1].replace("?", "").trim() + "\");");
        }
    }
}
