package org.firstinspires.ftc.teamcode.Utilities;

import java.util.ArrayList;

//Program that can read and write a 2D ArrayList to and from a string
public abstract class FotR_ArrayFormat {

    public static String writeArray(ArrayList<ArrayList<Double>> array) {
        String data = "";
        for (int j = 0; j < array.size(); j++) {
            String tempStr = "";
            ArrayList<Double> tempArr = array.get(j);
            for (int i = 0; i < tempArr.size(); i++) {
                tempStr += tempArr.get(i);
                if (i < tempArr.size() - 1) {
                    tempStr += ",";
                }
            }
            data +="{" + tempStr + "}";
        }
        data += "End";
        return data;
    }

    public static ArrayList<ArrayList<Double>> readArray (String file) {
        ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();

        String remainStr = file;
        String actionStr;
        int first;
        int last;

        while (!remainStr.equals("End")) {

            ArrayList<Double> tempLine = new ArrayList<>();
            first = remainStr.indexOf("{");
            last = remainStr.indexOf("}");
            actionStr = remainStr.substring(first + 1, last);

            while ((actionStr.length() > 0)) {
                int last2 = actionStr.indexOf(",");
                if (last2 == -1) {
                    last2 = actionStr.length();
                }
                tempLine.add(Double.parseDouble(actionStr.substring(0, last2)));
                if (actionStr.contains(",")) {
                    actionStr = actionStr.substring(last2 + 1);
                }
                else {
                    actionStr = "";
                }
            }

            data.add(tempLine);
            remainStr = remainStr.substring(last + 1);

        }
        return data;
    }

}
