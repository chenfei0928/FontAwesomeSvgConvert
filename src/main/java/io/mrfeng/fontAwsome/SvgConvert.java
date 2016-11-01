package io.mrfeng.fontAwsome;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class SvgConvert {
    private static final String nameSpace = "android";
    private static final String ns = "android:";
    private static final String SVG_SOURCE = "C:\\Users\\MrFeng\\Desktop\\font\\fontawesome-webfont.svg";
    private static final String OUTPUT_DIR = "C:\\Users\\MrFeng\\Desktop\\font\\out";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        System.out.println("begin");

        org.jsoup.nodes.Document htmlParse = Jsoup.parse(new URL("http://fontawesome.io/cheatsheet/"), 10000);

        Map<String, String> dictionary = new LinkedHashMap<>();
        Map<String, Set<String>> aliasDictionary = new LinkedHashMap<>();
        int length = 0;
        for (org.jsoup.nodes.Element container : htmlParse.body().getElementById("wrap").getElementsByClass("container")) {
            for (org.jsoup.nodes.Element row : container.getElementsByClass("row")) {
                for (org.jsoup.nodes.Element element : row.getElementsByTag("div")) {
                    // 获取到fa-所在的div
                    List<TextNode> textNodes = element.textNodes();
                    for (TextNode textNode : textNodes) {
                        String trim = textNode.text().replace('\r', ' ').replace('\n', ' ').trim();
                        if (trim.startsWith("fa-")) {
                            for (org.jsoup.nodes.Element span : element.getElementsByTag("span")) {
                                if (span.text().contains("[&#x")) {
                                    String spanText = span.text().trim();
                                    String substring = spanText.substring(4, spanText.length() - 2);
                                    if (dictionary.containsKey(substring)) {
                                        Set<String> orDefault = aliasDictionary.get(substring);
                                        orDefault = orDefault == null ? new LinkedHashSet<>() : orDefault;
                                        orDefault.add(dictionary.get(substring));
                                        orDefault.add(trim);
                                        aliasDictionary.put(substring, orDefault);
                                    } else {
                                        dictionary.put(substring, trim);
                                    }
                                    length++;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(dictionary.size() + " " + aliasDictionary.size() + " " + length);

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
                    String unicode = string2Unicode(document.getName());
                    String orDefault = dictionary.getOrDefault(unicode, "fa_" + unicode).replace('-', '_');
                    File file = new File(OUTPUT_DIR, orDefault + ".xml");
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
        System.out.println("finished");
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
        String name = info.getUnicode();
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
