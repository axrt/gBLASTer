//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.06 at 12:20:21 PM EDT 
//


package properties.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pathToMakeBlastDb",
    "pathToBlastP",
    "expect",
    "minORFLength",
    "maxORFLength"
})
@XmlRootElement(name = "BlastProperties")
public class BlastProperties {

    @XmlElement(name = "PathToMakeBlastDb", required = true)
    protected PathToMakeBlastDb pathToMakeBlastDb;
    @XmlElement(name = "PathToBlastP", required = true)
    protected PathToBlastP pathToBlastP;
    @XmlElement(name = "Expect", required = true)
    protected Expect expect;
    @XmlElement(name = "MinORFLength", required = true)
    protected MinORFLength minORFLength;
    @XmlElement(name = "MaxORFLength", required = true)
    protected MaxORFLength maxORFLength;

    /**
     * Gets the value of the pathToMakeBlastDb property.
     * 
     * @return
     *     possible object is
     *     {@link PathToMakeBlastDb }
     *     
     */
    public PathToMakeBlastDb getPathToMakeBlastDb() {
        return pathToMakeBlastDb;
    }

    /**
     * Sets the value of the pathToMakeBlastDb property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathToMakeBlastDb }
     *     
     */
    public void setPathToMakeBlastDb(PathToMakeBlastDb value) {
        this.pathToMakeBlastDb = value;
    }

    /**
     * Gets the value of the pathToBlastP property.
     * 
     * @return
     *     possible object is
     *     {@link PathToBlastP }
     *     
     */
    public PathToBlastP getPathToBlastP() {
        return pathToBlastP;
    }

    /**
     * Sets the value of the pathToBlastP property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathToBlastP }
     *     
     */
    public void setPathToBlastP(PathToBlastP value) {
        this.pathToBlastP = value;
    }

    /**
     * Gets the value of the expect property.
     * 
     * @return
     *     possible object is
     *     {@link Expect }
     *     
     */
    public Expect getExpect() {
        return expect;
    }

    /**
     * Sets the value of the expect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expect }
     *     
     */
    public void setExpect(Expect value) {
        this.expect = value;
    }

    /**
     * Gets the value of the minORFLength property.
     * 
     * @return
     *     possible object is
     *     {@link MinORFLength }
     *     
     */
    public MinORFLength getMinORFLength() {
        return minORFLength;
    }

    /**
     * Sets the value of the minORFLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link MinORFLength }
     *     
     */
    public void setMinORFLength(MinORFLength value) {
        this.minORFLength = value;
    }

    /**
     * Gets the value of the maxORFLength property.
     * 
     * @return
     *     possible object is
     *     {@link MaxORFLength }
     *     
     */
    public MaxORFLength getMaxORFLength() {
        return maxORFLength;
    }

    /**
     * Sets the value of the maxORFLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link MaxORFLength }
     *     
     */
    public void setMaxORFLength(MaxORFLength value) {
        this.maxORFLength = value;
    }

}
