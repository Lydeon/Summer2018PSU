package edu.pdx.cs410J.seung2;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for formatting messages on the server side.  This is mainly to enable
 * test methods that validate that the server returned expected strings.
 */
public class Messages
{
    public static String formatCallCount(int count )
    {
        return String.format( "%d PhoneCalls in the list", count );
    }

    public static String formatPrettyCall(PhoneCall call)
    {
        String format = "";

        long duration = call.getEndTime().getTime() - call.getStartTime().getTime();
        duration /= 60000;

        format += "Caller #: " + call.getCaller() + "\n";
        format += "Callee #: " + call.getCallee() + "\n";
        format += "Start Time: " + call.getStartTimeString() + "\n";
        format += "End Time: " + call.getEndTimeString() + "\n";
        format += "Duration: " + duration + " min\n";
        format += "\n****************************************************\n\n";

        return format;
    }

    public static String missingRequiredParameter( String parameterName )
    {
        return String.format("The required parameter \"%s\" is missing", parameterName);
    }

    public static String allPhoneCallListDeleted() {
        return "All PhoneCall entries have been deleted";
    }

    public static void formatPrettyBill(PrintWriter pw, Collection<PhoneCall> calls) {
        pw.println(Messages.formatCallCount(calls.size()));

        for (PhoneCall call : calls) {
            pw.println(Messages.formatPrettyCall(call));
        }
    }

    public static Collection<PhoneCall> parsePhoneBill(String content) {
        PhoneBill bill = new PhoneBill();

        String[] lines = content.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String[] args = lines[i].split(" ");

            if(args.length == 8) {
                Project4.checkNumber(args[0], "caller");
                String caller = args[0];
                Project4.checkNumber(args[1], "callee");
                String callee = args[1];
                Date start = Project4.initDate(args[2], args[3], args[4]);
                Date end = Project4.initDate(args[5], args[6], args[7]);

                PhoneCall tmp = new PhoneCall(caller, callee, start, end);

                bill.addPhoneCall(tmp);
            }
        }

        return bill.getPhoneCalls();
    }

}
