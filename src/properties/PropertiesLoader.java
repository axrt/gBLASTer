package properties;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import properties.jaxb.GBlasterProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by alext on 6/16/14.
 * TODO document class
 */
public final class PropertiesLoader {

    private PropertiesLoader() {
        throw new AssertionError("Non-instantiable");
    }

    public static GBlasterProperties load(InputStream inputStream) throws SAXException, JAXBException {

        final JAXBContext jc = JAXBContext.newInstance(GBlasterProperties.class);
        final Unmarshaller u = jc.createUnmarshaller();
        final XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                true);
        xmlreader.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                String file = null;
                if (systemId.contains("driver.dtd")) {
                    file = "driver.dtd";
                }
                return new InputSource(GBlasterProperties.class
                        .getResourceAsStream(file));
            }
        });
        final InputSource input = new InputSource(inputStream);
        final Source source = new SAXSource(xmlreader, input);
        return (GBlasterProperties) u.unmarshal(source);
    }
}

