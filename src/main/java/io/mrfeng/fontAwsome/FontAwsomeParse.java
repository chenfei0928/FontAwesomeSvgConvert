package io.mrfeng.fontAwsome;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

class FontAwsomeParse extends DefaultHandler {
    @Getter
    private final List<SvgInfo> mSvgInfos = new ArrayList<>();
    private int size;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "font":
                size = Integer.parseInt(attributes.getValue("horiz-adv-x"));
                break;
            case "glyph":
                mSvgInfos.add(parseSvgElements(attributes));
                break;
        }
    }

    private SvgInfo parseSvgElements(Attributes attributes) {
        SvgInfo info = new SvgInfo();
        info.setName(attributes.getValue("glyph-name"));
        info.setPath(attributes.getValue("d"));
        info.setUnicode(attributes.getValue("unicode"));
        String value = attributes.getValue("horiz-adv-x");
        info.setSize(value == null ? size : Integer.parseInt(value));
        return info;
    }
}
