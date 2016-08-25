package io.mrfeng.fontAwsome;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class SvgConvert {
    private static final String nameSpace = "android";
    private static final String ns = "android:";
    private static final String SVG_SOURCE = "C:\\Users\\MrFeng\\Documents\\Tencent Files\\751824142\\FileRecv\\fontawesome-webfont.svg";
    private static final String OUTPUT_DIR = "C:\\Users\\MrFeng\\Documents\\Tencent Files\\751824142\\FileRecv\\out";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        System.out.println("begin");
        FontAwsomeParse parse = new FontAwsomeParse();
        SAXParserFactory.newInstance().newSAXParser().parse(new File(SVG_SOURCE), parse);
        List<SvgInfo> mSvgInfos = parse.getMSvgInfos();
        System.out.println(mSvgInfos);


        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");                    // 编码格式
        format.setNewLineAfterDeclaration(false);       // 禁用首行后的空行
        format.setIndentSize(4);                        // 启用缩进
        format.setLineSeparator("\r\n");                // 行分隔符
        format.setNewLineAfterNTags(1);                 // 一个新Tag后的换行数量
        format.setNewlines(true);                       // 换行
        mSvgInfos.parallelStream()
                .filter(info -> info.getPath() != null && info.getPath().length() != 0)
                .map(SvgConvert::convertSvgInfo2XmlDocument)
                .forEach(document -> {
                    System.out.println(document.toString());
                    File file = new File(OUTPUT_DIR, string2Unicode(document.getName()) + ".xml");
                    XMLWriter writer = null;
                    try {
                        file.createNewFile();
                        writer = new AttributeXmlWriter(new FileWriter(file), format);
                        writer.write(document);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (writer != null)
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                });
    }

    private static Document convertSvgInfo2XmlDocument(SvgInfo info) {
        Document document = DocumentHelper.createDocument();
        Element vector = document.addElement("vector")
                .addNamespace(nameSpace, "http://schemas.android.com/apk/res/android")
                .addAttribute(ns + "width", "24dp")
                .addAttribute(ns + "height", "24dp")
                .addAttribute(ns + "viewportHeight", String.valueOf(info.getSize()))
                .addAttribute(ns + "viewportWidth", String.valueOf(info.getSize()));
        Element group = vector.addElement("group")
                .addAttribute(ns + "pivotX", String.valueOf(info.getSize() / 2))
                .addAttribute(ns + "pivotY", String.valueOf(info.getSize() / 2))
                .addAttribute(ns + "scaleY", "-1");
        group.addElement("path")
                .addAttribute(ns + "fillColor", "#ffff")
                .addAttribute(ns + "pathData", info.getPath());
        String name = info.getName() != null
                ? info.getName()
                : info.getUnicode();
        document.setName(name);
        return document;
    }

    /**
     * 字符串转换unicode
     */
    private static String string2Unicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
//            unicode.append('\\');
            unicode.append(Integer.toHexString(c));
        }
        return unicode.toString();
    }
}
