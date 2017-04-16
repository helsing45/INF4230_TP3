package main.java.utils;

import main.java.model.Node;
import main.java.model.Transport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Created by j-c9 on 2017-04-16.
 */
public class BoardParser {
    private static final String BOARD_FILE_NAME = "/res/board_file.xml";

    public static Node[] getNodes() {
        Node[] nodes = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(new File(BoardParser.class.getResource(BOARD_FILE_NAME).getPath()));
            document.getDocumentElement().normalize();
            NodeList nList = document.getElementsByTagName("boardPosition");

            nodes = new Node[nList.getLength()];
            for (int i = 0; i < nList.getLength(); i++)
                nodes[i] = new Node(i);

            for (int i = 0; i < nList.getLength(); i++) {
                if (nList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    final Element boardPosition = (Element) nList.item(i);
                    int id = Integer.valueOf(boardPosition.getAttribute("id"));
                    //nodes[id] = new Node(id);
                    nodes[i].setBoardPosition(boardPosition.getElementsByTagName("X").item(0).getTextContent(), boardPosition.getElementsByTagName("Y").item(0).getTextContent());

                    final NodeList links = boardPosition.getElementsByTagName("action");

                    for (int j = 0; j < links.getLength(); j++) {
                        final Element link = (Element) links.item(j);
                        int destination = Integer.valueOf(link.getElementsByTagName("destination").item(0).getTextContent()) - 1;
                        Transport type = Transport.findByAbbreviation(link.getElementsByTagName("transportation").item(0).getTextContent());
                        nodes[i].addLink(nodes[destination], type);
                        nodes[destination].addLink(nodes[i], type);
                    }


                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }
}
