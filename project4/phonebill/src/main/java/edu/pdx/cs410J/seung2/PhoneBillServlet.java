package edu.pdx.cs410J.seung2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This servlet ultimately provides a REST API for working with an
 * <code>PhoneBill</code>.  However, in its current state, it is an example
 * of how to use HTTP and Java servlets to store Map of PhoneBill and
 * Collection of PhoneCalls within that PhoneBill
 */
public class PhoneBillServlet extends HttpServlet
{
    // Map collection to store all PhoneBills
    private final Map<String, PhoneBill> billMap = new HashMap<>();

    // Parameter names
    private static String CUSTOMER_PARA = "customer";
    private static String CALLER_PARA = "caller";
    private static String CALLEE_PARA = "callee";
    private static String START_PARA = "startTime";
    private static String END_PARA = "endTime";

    /**
     * Handles an HTTP GET request from a client by retrieving PhoneBill data through
     * word specified in the "customer" HTTP parameter to the HTTP response.  If it
     * has empty time parameter then all of the PhoneBill associated with the name
     * are written to the HTTP response. else if time is there then search for that time interval.
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        // Retrieve parameters
        String customerName = getParameter(CUSTOMER_PARA, request );
        String startT = getParameter(START_PARA, request);
        String endT = getParameter(END_PARA, request);

        // If customer name wasn't passed by
        if(customerName == null){
            missingRequiredParameter(response, "customer name");
            return;
        }
        // If no time data then write all
        if(startT == null && endT == null) {
            writeAllPhoneBillPretty(customerName, response);
        }
        // If time data exist then use that for search
        else if(startT != null && endT != null){
            writeSearchPhoneBillPretty(customerName, startT, endT, response);
        }
        else{
            missingRequiredParameter(response, "one of dates");
            return;
        }
    }

    /**
     * Handles an HTTP POST request by storing the PhoneBill data in the map
     * via using the customer parameter as the key.  It writes the initialized PhoneBill
     * entry to the HTTP response.
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        // Retrieve and check error for all parameter
        String customerName = getParameter(CUSTOMER_PARA, request );
        if (customerName == null) {
            missingRequiredParameter(response, CUSTOMER_PARA);
            return;
        }

        String callerNum = getParameter(CALLER_PARA, request );
        if ( callerNum == null) {
            missingRequiredParameter( response, CALLER_PARA );
            return;
        }

        String calleeNum = getParameter(CALLEE_PARA, request );
        if ( calleeNum == null) {
            missingRequiredParameter(response, CALLEE_PARA);
            return;
        }

        String sDate = getParameter(START_PARA, request);
        if(sDate == null){
            missingRequiredParameter(response, START_PARA);
            return;
        }

        String eDate = getParameter(END_PARA, request);
        if(eDate == null){
            missingRequiredParameter(response, END_PARA);
            return;
        }

        // Initialize PhoneCall with the retrieved parameter
        String[] startDate = sDate.split(" ");
        String[] endDate = eDate.split(" ");

        Date Starting = Project4.initDate(startDate[0], startDate[1], startDate[2]);
        Date Ending = Project4.initDate(endDate[0], endDate[1], endDate[2]);

        PhoneCall call = new PhoneCall(callerNum, calleeNum, Starting, Ending);

        // Check if the customer entry already exists
        boolean checkContain = this.billMap.containsKey(customerName);

        // if it doesn't then make new and add
        if(!checkContain) {
            PhoneBill bill = new PhoneBill();

            bill.setCustomer(customerName.split(" "));
            bill.addPhoneCall(call);

            this.billMap.put(customerName, bill);
        }
        // if it does then check for duplicate PhoneCall and add
        else{
            int checkDupli = Project4.checkPrettyCallDupli(this.billMap.get(customerName).getPhoneCalls(), call);
            if(checkDupli == 1){
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, call.toString() + " already exists");
                return;
            }
            this.billMap.get(customerName).addPhoneCall(call);
        }

        response.setStatus( HttpServletResponse.SC_OK);
    }

    /**
     * Handles an HTTP DELETE request by removing all PhoneBill
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");

        this.billMap.clear();

        PrintWriter pw = response.getWriter();
        pw.println(Messages.allPhoneCallListDeleted());
        pw.flush();

        response.setStatus(HttpServletResponse.SC_OK);

    }

    /**
     * Writes an error message about a missing parameter to the HTTP response.
     *
     * The text of the error message is created by {@link Messages#missingRequiredParameter(String)}
     */
    private void missingRequiredParameter( HttpServletResponse response, String parameterName )
            throws IOException
    {
        String message = Messages.missingRequiredParameter(parameterName);
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
    }

    /**
     * Writes all of the corresponding PhoneBill's call collection to the HTTP response.
     *
     * @param name
     *          name of the customer
     *
     * The text of the message is formatted with
     * {@link Messages#formatPrettyBill(PrintWriter, String name, java.util.Collection<PhoneCall>)}
     */
    private void writeAllPhoneBillPretty(String name, HttpServletResponse response ) throws IOException
    {
        PrintWriter pw = response.getWriter();

        // Retrieve the PhoneBill with customer name
        PhoneBill bill = billMap.get(name);

        // If the bill doesn't exist then it's an error
        if(bill == null){
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Customer does not exist");
            return;
        }

        // Use Messages to export data
        Messages.formatPrettyBill(pw, name, bill.getPhoneCalls());

        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }

    /**
     * Search and compare all the data corresponding with the customer's name and export it
     *
     * @param name
     *          Name of the customer
     * @param start
     *          Comparison date for start
     * @param end
     *          Comparison date for end
     */
    private void writeSearchPhoneBillPretty(String name, String start, String end, HttpServletResponse response) throws IOException
    {
        PrintWriter pw = response.getWriter();

        PhoneBill bill = billMap.get(name);

        if(bill == null){
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Customer does not exist");
            return;
        }

        // Initialize Date value with the given String
        Date startT = null;
        Date endT = null;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        try{
            startT = sdf.parse(start);
            endT = sdf.parse(end);
        }catch(ParseException e){
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Unable to parse date while searching PhoneBill");
            return;
        }

        // Create collection to retrieve
        Collection<PhoneCall> billcall = bill.getPhoneCalls();

        PhoneBill temp = new PhoneBill();
        temp.setCustomer(bill.getCustomer().split(" "));

        // Evaluate and compare with the given search comparison
        for(PhoneCall call : billcall){
            if(call.getStartTime().getTime() >= startT.getTime() && call.getEndTime().getTime() <= endT.getTime()){
                temp.addPhoneCall(call);
            }
        }

        // Export it via Message class's formatPrettyBill method
        Messages.formatPrettyBill(pw, name ,temp.getPhoneCalls());

        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }

    /**
     * Returns the value of the HTTP request parameter with the given name.
     *
     * @return <code>null</code> if the value of the parameter is
     *         <code>null</code> or is the empty string
     */
    private String getParameter(String name, HttpServletRequest request) {
        String value = request.getParameter(name);
        if (value == null || "".equals(value)) {
            return null;

        } else {
            return value;
        }
    }
}
