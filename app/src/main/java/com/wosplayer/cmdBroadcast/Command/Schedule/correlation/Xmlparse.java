package com.wosplayer.cmdBroadcast.Command.Schedule.correlation;

import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2016/7/16.
 */
public class Xmlparse {

    public enum parseType {
        OnlyLeaf, AllLeaf,
    }
    ///////////////////

    private static void putattribute(HashMap mp, Element element) {
        List list = element.attributes();
        DefaultAttribute e = null;
        for (int i = 0; i < list.size(); i++) {
            e = (DefaultAttribute) list.get(i);
            mp.put(e.getName(), e.getText().trim());
        }
    }

    //////////////////

    private static void getElementList(String ParentPath, HashMap mp,
                                       Element element, parseType pt) {
        List elements = element.elements();
        if (elements.size() == 0) {
            String xpath = element.getPath();
            String value = element.getTextTrim();
            int start = xpath.lastIndexOf("/");
            int end = xpath.length();
            if (start != -1 && end != -1) {
                String key = xpath.substring(start + 1, end);
                if (pt == parseType.AllLeaf) {
                    mp.put(key, value);
                    putattribute(mp, element);
                } else {
                    if ((ParentPath + "/" + key).equals(xpath)) {
                        mp.put(key, value);
                        putattribute(mp, element);
                    }
                }
            }
        } else {
            for (Iterator it = elements.iterator(); it.hasNext();) {
                Element elem = (Element) it.next();
                getElementList(ParentPath, mp, elem, pt);
            }
        }
    }





    ///////////////////////

    private static void ParseXmlNode(String xmlPath, Element element,
                                     parseType pt, List<HashMap<String, String>> result) {
        List elements = element.elements();
        if (element.getPath().equals(xmlPath)) {
            String xpath = element.getPath();
            String value = element.getTextTrim();
            HashMap<String, String> mp = new HashMap<String, String>();
            result.add(mp);
            getElementList(xmlPath, mp, element, pt);
        }
        for (Iterator it = elements.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            ParseXmlNode(xmlPath, elem, pt, result);
        }
    }

    ////////////

    public static List<HashMap<String, String>> ParseXml(String xmlPath,
                                                         String xml, parseType parsetype) {
        List<HashMap<String, String>> result = new ArrayList();
        if (xml != null && xml.length() > 0) {
            Document doc = null;
            try {
                doc = DocumentHelper.parseText(xml);
            } catch (DocumentException e) {
                Log.i("ParseXml", e.getMessage());
            }
            Element root = doc.getRootElement();
            ParseXmlNode(xmlPath, root, parsetype, result);
        }
        return result;
    }













}
