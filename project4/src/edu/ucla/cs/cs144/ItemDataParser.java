/* CS144
 *
 * Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 * At the point noted below, an individual XML file has been parsed into a
 * DOM Document node. You should fill in code to process the node. Java's
 * interface for the Document Object Model (DOM) is in package
 * org.w3c.dom. The documentation is available online at
 *
 * http://java.sun.com/j2se/1.5.0/docs/api/index.html
 *
 * A tutorial of Java's XML Parsing can be found at:
 *
 * http://java.sun.com/webservices/jaxp/
 *
 * Some auxiliary methods have been written for you. You may find them
 * useful.
 */

package edu.ucla.cs.cs144;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import myPackage.Item;
import myPackage.Bid;
import org.xml.sax.InputSource;

class ItemDataParser {

    private Map<String, String> m_itemData = new HashMap<String, String>();

    static final String columnSeparator = "|*|";
    static DocumentBuilder builder;

    static final String[] typeName = {
            "none",
            "Element",
            "Attr",
            "Text",
            "CDATA",
            "EntityRef",
            "Entity",
            "ProcInstr",
            "Comment",
            "Document",
            "DocType",
            "DocFragment",
            "Notation",
    };

    static class MyErrorHandler implements ErrorHandler {

        public void warning(SAXParseException exception)
                throws SAXException {
            fatalError(exception);
        }

        public void error(SAXParseException exception)
                throws SAXException {
            fatalError(exception);
        }

        public void fatalError(SAXParseException exception)
                throws SAXException {
            exception.printStackTrace();
            System.out.println("There should be no errors " +
                    "in the supplied XML files.");
            System.exit(3);
        }

    }

    /* Non-recursive (NR) version of Node.getElementsByTagName(...)
     */
    static Element[] getElementsByTagNameNR(Element e, String tagName) {
        Vector< Element > elements = new Vector< Element >();
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
            {
                elements.add( (Element)child );
            }
            child = child.getNextSibling();
        }
        Element[] result = new Element[elements.size()];
        elements.copyInto(result);
        return result;
    }

    /* Returns the first subelement of e matching the given tagName, or
     * null if one does not exist. NR means Non-Recursive.
     */
    static Element getElementByTagNameNR(Element e, String tagName) {
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }

    /* Returns the text associated with the given element (which must have
     * type #PCDATA) as child, or "" if it contains no text.
     */
    static String getElementText(Element e) {
        if (e.getChildNodes().getLength() == 1) {
            Text elementText = (Text) e.getFirstChild();
            return elementText.getNodeValue();
        }
        else
            return "";
    }

    /* Returns the text (#PCDATA) associated with the first subelement X
     * of e with the given tagName. If no such X exists or X contains no
     * text, "" is returned. NR means Non-Recursive.
     */
    static String getElementTextByTagNameNR(Element e, String tagName) {
        Element elem = getElementByTagNameNR(e, tagName);
        if (elem != null)
            return getElementText(elem);
        else
            return "";
    }

    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try { am = nf.parse(money).doubleValue(); }
            catch (ParseException e) {
                System.out.println("This method should work for all " +
                        "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }
    
    static void getCategory(Element item, Item parsedItem){
        if (item.getNodeType() == Node.ELEMENT_NODE) {
            NodeList nodeList = item.getElementsByTagName("Category");
            List<String> categoryList = new ArrayList<String>();
            for(int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element eElement = (Element)node;
                    String category = eElement.getTextContent();
                    categoryList.add(category);                    
                }
            }

            parsedItem.categories = categoryList;
        }
    }
    
    static String convertToSqlDateFormat(String xmlFormattedDate){
        SimpleDateFormat xmlFormat = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //'1970-01-01 00:00:01' to '2038-01-19 03:14:07'
        String sqlDate = "";
        try {
            Date xmlDate = xmlFormat.parse(xmlFormattedDate);
            sqlDate = sqlDateFormat.format(xmlDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sqlDate;
    }

    /**@var sellerID string cannot be casted into an integer */
    static void getItem(Element eElement, Item parsedItem){
        String ItemID = eElement.getAttribute("ItemID");

        String Name = eElement.getElementsByTagName("Name").item(0).getTextContent();
        String Currently = strip(eElement.getElementsByTagName("Currently").item(0).getTextContent());
        String Buy_price = ""; // default for buy price
        if(eElement.getElementsByTagName("Buy_Price").getLength() > 0) {
            Buy_price = strip(eElement.getElementsByTagName("Buy_Price").item(0).getTextContent());
        }

        String First_bid = strip(eElement.getElementsByTagName("First_Bid").item(0).getTextContent());
        String Number_of_bids = eElement.getElementsByTagName("Number_of_Bids").item(0).getTextContent();

        String Started_xml = eElement.getElementsByTagName("Started").item(0).getTextContent();
        String Ends_xml = eElement.getElementsByTagName("Ends").item(0).getTextContent();

        String Started = convertToSqlDateFormat(Started_xml);
        String Ends = convertToSqlDateFormat(Ends_xml);

        String Country = getElementByTagNameNR( eElement, "Country").getTextContent(); 
        String Description = eElement.getElementsByTagName("Description").item(0).getTextContent();

        ArrayList<String> data = new ArrayList<String>();
        parsedItem.setItemID(ItemID);
        parsedItem.setName(Name);
        parsedItem.setCurrently(Currently);
        parsedItem.setBuyPrice(Buy_price);
        parsedItem.setFirstBid(First_bid);
        parsedItem.setNumberOfBids(Number_of_bids);
        parsedItem.setStarted(Started);
        parsedItem.setEnds(Ends);
        parsedItem.setCountry(Country);
        parsedItem.setDescription(Description.substring(0, Math.min(4000, Description.length())));

    }

    // appends row/tuple to a file
    static void writeToFile(String fileName, ArrayList<String> data){
        StringBuilder str = new StringBuilder();
        int length = data.size();
        for(int i = 0; i < length; i++){
            String tempStr = data.get(i);
            if(tempStr == ""){
                tempStr = "\\N";
            }
            str.append(tempStr);
            if(i != length - 1){
                str.append(columnSeparator);
            }
        }

        // append string to file and create file if file doesnt
        try{
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(str.toString());
            out.close();
        } catch (IOException e) {
        }
    }

    static void getData(Document doc, Item parsedItem ){ //Root element should be Item
        Map<String, Integer> sellerMap = new HashMap<String, Integer>();
        Map<String, Integer> bidderMap = new HashMap<String, Integer>();
        int[] anArray = new int[2];

        Node item =  doc.getDocumentElement();

        if (item.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) item;

            
            // populates the item location info
            getLocation(eElement, parsedItem);

            
            //populates the seller info
            getSeller(eElement, parsedItem);
            

            //populates the item info
            getItem(eElement, parsedItem); // gets a row/tuple of data for Item table
            
            //populates the item's category info
            getCategory(eElement, parsedItem); // gets a row/tuple of data for Category table

            
            //populates the item's bid(s) info
            Element bids = (Element) eElement.getElementsByTagName("Bids").item(0);
            NodeList bidList = bids.getElementsByTagName("Bid");
            for(int j = 0; j < bidList.getLength(); j++){
                Node bid = bidList.item(j);
                if (bid.getNodeType() == Node.ELEMENT_NODE){
                    Element bidElement = (Element) bid;
                    getBid(bidElement, parsedItem); 
                }
            }
            
        } 

    }

    
    static void getBid(Element bid, Item parsedItem) {
        if (bid.getNodeType() == Node.ELEMENT_NODE) {
            String Time_xml = bid.getElementsByTagName("Time").item(0).getTextContent();
            String Time = convertToSqlDateFormat(Time_xml);

            String Amount = strip(bid.getElementsByTagName("Amount").item(0).getTextContent());

            Element bidderElement = (Element)bid.getElementsByTagName("Bidder").item(0);
            String bidderUserID = bidderElement.getAttribute("UserID");
            String bidderRating = bidderElement.getAttribute("Rating");
            String bidderLatitude = getElementByTagNameNR( bidderElement, "Location").getAttribute("Latitude"); 
            String bidderLongitude = getElementByTagNameNR( bidderElement, "Location").getAttribute("Longitude");
            String bidderCountry = getElementByTagNameNR( bidderElement, "Country").getTextContent();

            Bid oneBid = new Bid(bidderUserID, bidderRating, Time, Amount);
            if (bidderLatitude != null)
                oneBid.bidderLatitude = bidderLatitude;
            if (bidderLongitude != null)
                oneBid.bidderLongitude = bidderLongitude;
            if (bidderCountry != null)
                oneBid.bidderCountry = bidderCountry;
            
            parsedItem.bids.add(oneBid);
                
        }
    }
    
    
    static void getSeller(Element item, Item parsedItem) {
        if (item.getNodeType() == Node.ELEMENT_NODE) {
            Node sellerNode = item.getElementsByTagName("Seller").item(0);
            if (sellerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) sellerNode;
                String sellerID = eElement.getAttribute("UserID");
                String rating = eElement.getAttribute("Rating");

                parsedItem.sellerID = sellerID;
                parsedItem.sellerRating = rating;

            }
        }
    }
    
    static void getLocation(Element item, Item parsedItem) {
        if (item.getNodeType() == Node.ELEMENT_NODE) {
            //need to use NON-recursive, else will get element(s) of children
            Element locationElement = getElementByTagNameNR( item, "Location"); 
            String location = locationElement.getTextContent();
            String latitude = locationElement.getAttribute("Latitude");
            String longitude = locationElement.getAttribute("Longitude");

            parsedItem.location = location;
            parsedItem.latitude = latitude;
            parsedItem.longitude = longitude;
        }
    }
    

    /* Process one item xml data string.
     */
    static void processXMLString (String xmlItemData, Item parsedItem) {
        Document doc = null;

        try {
            InputSource is = new InputSource(new StringReader(xmlItemData));
            doc = builder.parse(is);                    
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
        catch (SAXException e) {
            System.out.println("Parsing error on string " + xmlItemData);
            e.printStackTrace();
            System.exit(3);
        }
        
        /* At this point 'doc' contains a DOM representation of an 'Items' XML
         * string. Use doc.getDocumentElement() to get the root Element. */

        /* Fill in code here (you will probably need to write auxiliary
            methods). */
            
        try {

            doc.getDocumentElement().normalize();

            //Root elemnt should be 'Item'
            System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
            getData(doc, parsedItem);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public static Item parseItemXMLString(String xmlItemData) {
        /* Initialize parser. */
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new MyErrorHandler());
        }
        catch (FactoryConfigurationError e) {
            System.out.println("unable to get a document builder factory");
            System.exit(2);
        }
        catch (ParserConfigurationException e) {
            System.out.println("parser was unable to be configured");
            System.exit(2);
        }

        // Process itemXML data given as a string
        if (xmlItemData != null) {
            Item parsedItem = new Item(); 
            processXMLString(xmlItemData, parsedItem);
            return parsedItem;
        }
        return null;
    }
}
