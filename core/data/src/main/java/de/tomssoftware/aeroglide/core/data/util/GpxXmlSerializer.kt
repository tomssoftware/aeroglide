package de.tomssoftware.aeroglide.core.data.util

import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

fun XmlSerializer.document(
    docName: String = "UTF-8",
    xmlStringWriter: StringWriter = StringWriter(),
    init: XmlSerializer.() -> Unit
): String {
    startDocument(docName, true)
    xmlStringWriter.buffer.setLength(0) //  refreshing string writer due to reuse
    setOutput(xmlStringWriter)
    init()
    endDocument()
    return xmlStringWriter.toString()
}

//  element
fun XmlSerializer.element(name: String, init: XmlSerializer.() -> Unit) {
    startTag("", name)
    init()
    endTag("", name)
}

//  element
fun XmlSerializer.element(name: String, init1: XmlSerializer.() -> Unit, init2: XmlSerializer.() -> Unit) {
    startTag("", name)
    init1()
    init2()
    endTag("", name)
}


//  element with attribute & content
fun XmlSerializer.element(
    name: String,
    content: String,
    init: XmlSerializer.() -> Unit
) {
    startTag("", name)
    init()
    text(content)
    endTag("", name)
}

//  attribute
fun XmlSerializer.attribute(name: String, value: String): XmlSerializer = attribute("", name, value)

