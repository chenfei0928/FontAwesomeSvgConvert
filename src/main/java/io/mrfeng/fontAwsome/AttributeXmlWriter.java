package io.mrfeng.fontAwsome;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.NamespaceStack;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;

public class AttributeXmlWriter extends XMLWriter {
    private NamespaceStack mNamespaceStack = getNamespaceStack();
    private OutputFormat format;

    public AttributeXmlWriter() {
    }

    public AttributeXmlWriter(OutputFormat format) throws UnsupportedEncodingException {
        super(format);
        this.format = format;
    }

    public AttributeXmlWriter(OutputStream out) throws UnsupportedEncodingException {
        super(out);
    }

    public AttributeXmlWriter(OutputStream out, OutputFormat format) throws UnsupportedEncodingException {
        super(out, format);
        this.format = format;
    }

    public AttributeXmlWriter(Writer writer) {
        super(writer);
    }

    public AttributeXmlWriter(Writer writer, OutputFormat format) {
        super(writer, format);
        this.format = format;
    }

    private NamespaceStack getNamespaceStack() {
        if (mNamespaceStack == null)
            try {
                Field namespaceStack = XMLWriter.class.getDeclaredField("namespaceStack");
                namespaceStack.setAccessible(true);
                this.mNamespaceStack = (NamespaceStack) namespaceStack.get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        return mNamespaceStack;
    }

    @Override
    protected void writeAttributes(Attributes attributes) throws IOException {
        for (int i = 0, size = attributes.getLength(); i < size; i++) {
            writeAttribute(attributes, i);
            if (i < size - 1)
                attributeIndent();
        }
    }

    @Override
    protected void writeAttributes(Element element) throws IOException {
        // I do not yet handle the case where the same prefix maps to
        // two different URIs. For attributes on the same element
        // this is illegal; but as yet we don't throw an exception
        // if someone tries to do this
        for (int i = 0, size = element.attributeCount(); i < size; i++) {
            attributeIndent();

            Attribute attribute = element.attribute(i);
            Namespace ns = attribute.getNamespace();

            if ((ns != null) && (ns != Namespace.NO_NAMESPACE)
                    && (ns != Namespace.XML_NAMESPACE)) {
                String prefix = ns.getPrefix();
                String uri = mNamespaceStack.getURI(prefix);

                if (!ns.getURI().equals(uri)) {
                    writeNamespace(ns);
                    mNamespaceStack.push(ns);

                    attributeIndent();
                }
            }

            // If the attribute is a namespace declaration, check if we have
            // already written that declaration elsewhere (if that's the case,
            // it must be in the namespace stack
            String attName = attribute.getName();

            if (attName.startsWith("xmlns:")) {
                String prefix = attName.substring(6);

                if (mNamespaceStack.getNamespaceForPrefix(prefix) == null) {
                    String uri = attribute.getValue();
                    mNamespaceStack.push(prefix, uri);
                    writeNamespace(prefix, uri);
                }
            } else if (attName.equals("xmlns")) {
                if (mNamespaceStack.getDefaultNamespace() == null) {
                    String uri = attribute.getValue();
                    mNamespaceStack.push(null, uri);
                    writeNamespace(null, uri);
                }
            } else {
                char quote = format.getAttributeQuoteCharacter();
                writer.write(" ");
                writer.write(attribute.getQualifiedName());
                writer.write("=");
                writer.write(quote);
                writeEscapeAttributeEntities(attribute.getValue());
                writer.write(quote);
            }
        }
    }

    private void attributeIndent() throws IOException {
        writer.write("\r\n");
        indent();
        writer.write(format.getIndent().substring(0, format.getIndent().length() - 1));
    }

    @Override
    protected void writeAttribute(Attribute attribute) throws IOException {
        super.writeAttribute(attribute);
        attributeIndent();
    }
}
