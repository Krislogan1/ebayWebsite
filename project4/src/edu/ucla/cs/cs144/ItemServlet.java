package edu.ucla.cs.cs144;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.ItemDataParser;
import myPackage.Item;

public class ItemServlet extends HttpServlet implements Servlet {
       
    public ItemServlet() {}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // your codes here
        String pageTitle = "Item page";
        request.setAttribute("title", pageTitle);

        String debug = "This is a debug message";
        String itemID = "";
        
        if (request.getParameter("id") != null) {
            itemID = request.getParameter("id");

            String xmlItemData = AuctionSearch.getXMLDataForItemId(itemID);
            
            //parse xml Item data
            if (xmlItemData != null ) {
                Item parsedItem = ItemDataParser.parseItemXMLString(xmlItemData);
                if (parsedItem != null) {
                   request.setAttribute("result", parsedItem);
                   request.setAttribute("debug", debug);
                }
            }
            
            
        }

        request.getRequestDispatcher("/item.jsp").forward(request, response);

    }
}
