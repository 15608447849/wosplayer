package com.wosplayer.cmdBroadcast.Command.Schedule.correlation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by user on 2016/7/16.
 */
public class XmlHelper {

    public static Element getDomXml(InputStream inStream) {
        Element root = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(inStream);
            inStream.close();
            root = dom.getDocumentElement();
        } catch (Exception e) {

        }
        return root;
    }
    //////
    public static String getFirstChildNodeValue(Element e, String s) {
        Node node = e.getElementsByTagName(s).item(0);
        if (node == null)
            return "";
        String result = node.getTextContent();
        if (result == null)
            return "";
        return result;
    }
    ////////////////

    public static String getNodeToString(Node e) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        }
        Document document = builder.newDocument();
        document.setXmlVersion("1.0");
        Node aa = document.adoptNode(e);
        document.appendChild(aa);

        StringWriter writer = new StringWriter();
        StreamResult sResult = new StreamResult(writer);
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer tformer = null;
        try {
            tformer = tfactory.newTransformer();
        } catch (TransformerConfigurationException e1) {
            e1.printStackTrace();
        }
        DOMSource source = new DOMSource(document);
        try {
            tformer.transform(source, sResult);
        } catch (TransformerException e1) {
            e1.printStackTrace();
        }
        String result = writer.toString();
        if (result == null)
            return "";
        return result;
    }
    /////////////////
    public static String getFirstChildToString(Element e, String s) {
        Node node = e.getElementsByTagName(s).item(0);
        if (node == null)
            return "";
        return getNodeToString(node);
    }
    //////////////////////////

    public static String getXmlDataFromUrl(String urlString) {
        URL url;
        try {
            url = new URL(urlString);

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return "";
        }

        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException ioe) {
            return "";
        }

        InputStreamReader in = null;
        BufferedReader br = null;
        try {
            in = new InputStreamReader(urlConnection.getInputStream());
            br = new BufferedReader(in);
            StringBuilder xmlData = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null)
                xmlData.append(temp).append("\n");
            return xmlData.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (br != null)
                    br.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
