package org.firstinspires.ftc.teamcode.TeamCode_2021;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Utilities.FotR_ArrayFormat;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Button;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

//A File Manager mostly created for the Auto-Recorder
public class FotR_FileManager extends LinearOpMode {

    private ArrayList<ArrayList<File>> fileList;
    private ArrayList<File> program;
    private ArrayList<Double> idList;
    private ArrayList<ArrayList<Double>> idList2;
    private File currentFile;
    private File idLibrary = AppUtil.getInstance().getSettingsFile("idLibrary.txt");
    private FotR_Button ld_PU = new FotR_Button(true);
    private FotR_Button rd_PU = new FotR_Button(true);
    private FotR_Button ud_PU = new FotR_Button(true);
    private FotR_Button dd_PU = new FotR_Button(true);
    private FotR_Button a_PU = new FotR_Button(true);
    private FotR_Button b_PU = new FotR_Button(true);
    private char[] letters = {'A','B','C','D','E','F','G','H','I','J'};

    //Needs to exist so I can use controller inputs
    public void runOpMode() throws InterruptedException {

    }

    public void saveFile (ArrayList<ArrayList<Double>> file, Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) throws IOException {
        //Finds the index library and loads it so the known programs can be found
        if (idLibrary.exists()) {
            idList2 = FotR_ArrayFormat.readArray(ReadWriteFile.readFile(idLibrary));
        }
        else {
            ReadWriteFile.writeFile(idLibrary,"End");
            idList2 = FotR_ArrayFormat.readArray(ReadWriteFile.readFile(idLibrary));
        }

        //Makes sure the list has something in it and pull it into a 1D array (since it should only have one line)
        if (idList2.size() > 0) {
            idList = idList2.remove(0);
        }
        else {
            idList = new ArrayList<>();
        }

        int[] name = new int[7];
        int ringNum = name[6];
        String displayName = "";
        boolean progress = false;
        int index = 0;
        int menuIndex = 0;

        //Loop through a menu so the user can name their program with a five digit number and a single letter
        while (!progress) {
            a_PU.updateState(gamepad1.a || gamepad2.a);
            b_PU.updateState(gamepad1.b || gamepad2.b);
            if (a_PU.returnState()) {
                menuIndex++;
            }
            if (b_PU.returnState() && menuIndex > 0) {
                menuIndex--;
            }
            switch (menuIndex) {
                case 0: {
                    telemetry.addData("Saving Program", displayName);
                    telemetry.addData("Number of Rings", Math.pow(ringNum, 2));

                    ld_PU.updateState(gamepad1.dpad_left || gamepad2.dpad_left);
                    rd_PU.updateState(gamepad1.dpad_right || gamepad2.dpad_right);
                    ud_PU.updateState(gamepad1.dpad_up || gamepad2.dpad_up);
                    dd_PU.updateState(gamepad1.dpad_down || gamepad2.dpad_down);

                    if (ld_PU.returnState() && index > 0) {
                        index--;
                    }
                    if (rd_PU.returnState() && index < 7) {
                        index++;
                    }
                    if (ud_PU.returnState()) {
                        name[index]++;
                    }
                    if (dd_PU.returnState()) {
                        name[index]--;
                    }
                    if (name[index] == 10) {
                        name[index] = 0;
                    }
                    if (name[index] == -1) {
                        name[index] = 9;
                    }

                    displayName = "";
                    for (int num : name) {
                        if (displayName.length() < 6) {
                            if (displayName.length() < 5) {
                                displayName += num;
                            } else {
                                displayName += letters[num];
                            }
                        } else {
                            ringNum = num;
                        }
                    }

                    String dots = "______";
                    telemetry.addData(dots.substring(0, index) + "*" + dots.substring(index), "Up");
                    telemetry.addData(displayName + "-" + Math.pow(ringNum, 2), "");
                    telemetry.addData(dots.substring(0, index) + "*" + dots.substring(index), "Down");
                    telemetry.update();
                    break;
                }
                case 1: {
                    //Confirmation review
                    telemetry.addData("Confirm Data", "?");
                    telemetry.addData("Program Name",displayName.substring(0,5));
                    telemetry.addData("Ring Stack", Math.pow(ringNum, 2));
                    telemetry.addData("Version Letter", displayName.substring(5));
                    telemetry.update();
                    break;
                }
                case 2: {
                    //Finished with the menu
                    progress = true;
                    break;
                }
            }
        }

        //Save our id to the library index and create a file under the name we chose
        currentFile = AppUtil.getInstance().getSettingsFile(displayName + "_" + ringNum + ".txt");
        ReadWriteFile.writeFile(currentFile,FotR_ArrayFormat.writeArray(file));

        /*double savedId = 0;*/
        double savedId = Double.parseDouble(displayName.substring(0,5) + name[5]);
        /*for (int i = 0; i < name.length-1; i++) {
            savedId = name[i] * Math.pow(10, (6-i));
        }*/

        //Use this to avoid adding duplicate IDs when re-saving a program
        boolean found = false;
        for (int i = 0; i < idList.size(); i++) {
            if (!found) {
                found = (idList.get(i) == savedId);
            }
        }
        if (!found) {
            idList.add(savedId);
        }
        while (!(gamepad1.y || gamepad2.y)) {
            telemetry.addData("ID's", idList);
            telemetry.addData("Found", found);
            telemetry.addData("This ID", savedId);
            telemetry.update();
        }

        idList2.add(idList);
        ReadWriteFile.writeFile(idLibrary,FotR_ArrayFormat.writeArray(idList2));
    }


    public ArrayList<File> findFile (Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        //Retrieve the IDList, no checks necessary for this one since it is assumed the list is already there from saveFile
        idList2 = FotR_ArrayFormat.readArray(ReadWriteFile.readFile(idLibrary));
        idList = idList2.get(0);

        boolean searching = true;
        int index = 0;
        int menuIndex = 0;
        String displayName = "";

        //Begin another menu to scroll through known programs
        while (searching) {
            a_PU.updateState(gamepad1.a || gamepad2.a);
            b_PU.updateState(gamepad1.b || gamepad2.b);
            if (a_PU.returnState()) {
                menuIndex++;
            }
            if (b_PU.returnState() && menuIndex > 0) {
                menuIndex--;
            }
            switch (menuIndex) {
                case 0: {
                    ud_PU.updateState(gamepad1.dpad_up || gamepad2.dpad_up);
                    dd_PU.updateState(gamepad1.dpad_down || gamepad2.dpad_down);
                    if (ud_PU.returnState() && index < idList.size()-1) {
                        index++;
                    }
                    if (dd_PU.returnState() && index > 0) {
                        index--;
                    }
                    //Converting the six-digit stored number into 5 digits and a letter
                    telemetry.addData(String.valueOf((int) (idList.get(index) / 10)), letters[(int) (idList.get(index) % 10)]);
                    telemetry.addData("Index", index);
                    telemetry.addData("Size", idList.size());
                    displayName = String.valueOf((int) (idList.get(index) / 10)) + letters[(int) (idList.get(index) % 10)];
                    while (displayName.length() < 6) {
                        displayName = 0 + displayName;
                    }

                    telemetry.update();
                    break;
                }
                case 1: {
                    //Confirmation screen
                    telemetry.addData("Program Name", displayName);
                    currentFile = AppUtil.getInstance().getSettingsFile(displayName + "_" + 0 + ".txt");
                    telemetry.addData("Zero Stack Available", currentFile.exists());
                    currentFile = AppUtil.getInstance().getSettingsFile(displayName + "_" + 1 + ".txt");
                    telemetry.addData("One Stack Available", currentFile.exists());
                    currentFile = AppUtil.getInstance().getSettingsFile(displayName + "_" + 2 + ".txt");
                    telemetry.addData("Four Stack Available", currentFile.exists());
                    telemetry.update();
                    break;
                }
                case 2: {
                    //Done, load and return the 2D ArrayList
                    searching = false;
                    break;
                }
            }
        }
        program = new ArrayList<File>();
        for (int i = 0; i <= 2; i++) {
            currentFile = AppUtil.getInstance().getSettingsFile(displayName + "_" + i + ".txt");
            if (currentFile.exists()) {
                program.add(currentFile);
            }
            else {
                ReadWriteFile.writeFile(currentFile,"End");
                program.add(currentFile);
            }
        }
        return program;
    }
}
