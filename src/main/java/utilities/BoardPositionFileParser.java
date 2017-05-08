package main.java.utilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoardPositionFileParser extends DefaultHandler {

    private final String fileName;
    private List<Point> boardPositions;
    private int temporaryX,temporaryY;
    private boolean isXLocation;
    private boolean isYLocation;

    public BoardPositionFileParser(String fileName) {
        this.fileName = fileName;
    }

    public List<Point> getParsedData() {
        SAXParser parser = createSaxParser();
        tryToParseDataToList(parser);
        return boardPositions;
    }

    private SAXParser createSaxParser() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        return tryToCreateSaxParser(factory);
    }

    private SAXParser tryToCreateSaxParser(SAXParserFactory factory) {
        try {
            return factory.newSAXParser();
        } catch (SAXException | ParserConfigurationException e) {
            throw new IllegalStateException("Error: " + e.getMessage(), e);
        }
    }

    private void tryToParseDataToList(SAXParser parser) {
        try {
            parser.parse(fileName, this);
        } catch (IOException | SAXException e) {
            throw new IllegalStateException("Error: " + e.getMessage(), e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        this.boardPositions = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String elementName, Attributes attributes)
            throws SAXException {
        if( elementName.equalsIgnoreCase("X")){
            isXLocation = true;
        }else if( elementName.equalsIgnoreCase("Y")){
            isYLocation = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String elementName) throws SAXException {
        if (elementName.equalsIgnoreCase("boardPosition"))
            boardPositions.add(new Point(temporaryX,temporaryY));
    }

    @Override
    public void characters(char[] character, int start, int length) throws SAXException {
        if (isXLocation) {
            temporaryX = Integer.valueOf(new String(character, start, length));
            isXLocation = false;
        }
        else if (isYLocation) {
            temporaryY = Integer.valueOf(new String(character, start, length));
            isYLocation = false;
        }
    }
}