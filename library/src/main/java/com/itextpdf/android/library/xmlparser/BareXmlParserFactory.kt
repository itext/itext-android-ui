package com.itextpdf.android.library.xmlparser

import com.itextpdf.kernel.exceptions.PdfException
import com.itextpdf.kernel.utils.IXmlParserFactory
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory


class BareXmlParserFactory : IXmlParserFactory {
    override fun createDocumentBuilderInstance(namespaceAware: Boolean, ignoringComments: Boolean): DocumentBuilder {
        val factory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder = try {
            factory.newDocumentBuilder()
        } catch (e: ParserConfigurationException) {
            throw PdfException(e.message, e)
        }
        return db
    }

    override fun createXMLReaderInstance(namespaceAware: Boolean, validating: Boolean): XMLReader {
        val factory = SAXParserFactory.newInstance()
        val xmlReader: XMLReader = try {
            val saxParser = factory.newSAXParser()
            saxParser.xmlReader
        } catch (e: ParserConfigurationException) {
            throw PdfException(e.message, e)
        } catch (e: SAXException) {
            throw PdfException(e.message, e)
        }
        return xmlReader
    }
}