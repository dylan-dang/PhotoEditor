package org.firstinspires.ftc.teamcode.TeamCode_2021;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Utilities.FotR_ArrayFormat;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Button;
import org.firstinspires.ftc.teamcode.Utilities.FotR_ButtonToggle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//Playback autonomous
@Autonomous(name = "Ringbearer Auto v4" , group = "Autonomous")
public class FotR_Ringbearer_Auto_v4 extends LinearOpMode {

    FotR_Ringbearer_Hardware robot = new FotR_Ringbearer_Hardware();
    FotR_Ringbearer_AutoFunctions_v4 auto;
    FotR_ButtonToggle xToggle = new FotR_ButtonToggle();
    FotR_FileManager files = new FotR_FileManager();

    boolean doPowerShots = false;
    boolean flywheelOn = false;
    boolean bucketUp = false;
    boolean wobbleUp = true;
    boolean intakeOn = false;

    //Multiplier to flip Y coordinate for different alliances
    int BYF = 1;
    final double SP = 1.0; //Standard Power Level

    final double wobbleLowPos = 775;
    final double wobbleLiftPos = 500;

    double wheelSpd = 0;

    ElapsedTime runtime = new ElapsedTime();

    int startPos = 0;
    int aimingIndex = 9;

    private int wobbleSlot = 0;

    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;

    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";

    File XInch = AppUtil.getInstance().getSettingsFile("XInch.txt");
    File YInch = AppUtil.getInstance().getSettingsFile("YInch.txt");
    File RotDegree = AppUtil.getInstance().getSettingsFile("RotDegree.txt");
    File AllianceSide = AppUtil.getInstance().getSettingsFile("AllianceSide.txt");

    ArrayList<ArrayList<Double>> activeSlot = new ArrayList<ArrayList<Double>>();
    ArrayList<File> program;

    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap);

        initVuforia();
        initTfod();

        robot.wobbleArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.wobbleArm.setTargetPosition(0);
        robot.wobbleArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.wobbleArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        robot.wobbleClaw.setPosition(robot.wobbleClawPos[2]);
        robot.shooterLift.setPosition(0.32);

        if (tfod != null) {
            tfod.activate();
            tfod.setZoom(2.5, 1.78);
        }

        runtime.reset();

        xToggle.setToggle(true);

        program = files.findFile(gamepad1, gamepad2, telemetry);

        /*Start Positions:
          0: Blue Outer
          1: Blue Inner
          2: Red Inner
          3: Red Outer
        */

        activeSlot = FotR_ArrayFormat.readArray(ReadWriteFile.readFile(program.get(0)));
        ArrayList<Double> encoder = activeSlot.remove(0);
        doPowerShots = (encoder.get(0) == 1);
        double sPos = encoder.get(1);
        startPos = (int) sPos;


        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.addData("Attempt Power Shots", doPowerShots);

            telemetry.addData("Starting Position", robot.startPosNames[startPos]);
            switch (startPos) {
                case 0: {
                    telemetry.addData(" *  _ ", "_  _ ");
                    break;
                }
                case 1: {
                    telemetry.addData(" _  * ", "_  _ ");
                    break;
                }
                case 2: {
                    telemetry.addData(" _  _ ", "*  _ ");
                    break;
                }
                case 3: {
                    telemetry.addData(" _  _ ", "_  * ");
                    break;
                }
            }
            telemetry.addData(" |   | ", " |   | ");
            telemetry.update();
        }
        waitForStart();

        List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();

        auto = new FotR_Ringbearer_AutoFunctions_v4(robot);
        auto.robot.odoPod.setFieldStart(robot.startPosCoor[0][startPos],robot.startPosCoor[1][startPos],0);

        if (updatedRecognitions != null) {
            telemetry.addData("# Object Detected", updatedRecognitions.size());
            // step through the list of recognitions and display boundary info.
            int i = 0;
            for (Recognition recognition : updatedRecognitions) {
                telemetry.addData(String.format("label (%d)", i), recognition.getLabel());
                if (recognition.getLabel().equals(LABEL_FIRST_ELEMENT)) {
                    wobbleSlot = 2;
                }
                if (recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) {
                    wobbleSlot = 1;
                }
            }
            telemetry.update();
        }

        activeSlot = FotR_ArrayFormat.readArray(ReadWriteFile.readFile(program.get(wobbleSlot)));
        encoder = activeSlot.remove(0);
        //FotR_ArrayFormat.readArray(ReadWriteFile.readFile(StoredArray));
        doPowerShots = (encoder.get(0) == 1);
        sPos = encoder.get(1);
        startPos = (int) sPos;

        if (startPos < 2) {BYF = -1; aimingIndex = 0;}
        ReadWriteFile.writeFile(AllianceSide, String.valueOf(BYF));

        auto.setFlywheel(0.68);
        auto.wobbleMovement(0.7,0,false);
        auto.wobbleMovement(0.7,wobbleLiftPos-150,false);

        auto.shootRings(31.4,-1, -1);
        auto.moveTo(SP,-9, robot.startPosCoor[1][startPos],0,false,true);


        if (doPowerShots) {
            auto.powerShotVolley(startPos, gamepad2);
            flywheelOn = true;
        }
        else {
            flywheelOn = true;
        }

        auto.shootRings(27.1,-1,0);

        for (int i = 0; i < activeSlot.size(); i++) {
            ArrayList<Double> command = activeSlot.get(i);
            double commandCode = command.get(0);
            switch ((int) commandCode) {
                case 0: {
                    boolean skate = false;
                    boolean drift = false;
                    if (command.get(5) == 1) {
                        skate = true;
                        drift = true;
                    }
                    if (i != activeSlot.size() - 1) {
                        if (activeSlot.get(i+1).get(0) == 1) {
                            drift = true;
                        }
                    }
                    auto.moveTo(command.get(1), command.get(2), command.get(3), command.get(4), !drift, !skate);
                    break;
                }
                case 1: {
                    auto.turn(SP, command.get(1), 2);
                    break;
                }
                case 2: {
                    flywheelOn = !flywheelOn;
                    break;
                }
                case 3: {
                    double rings = command.get(2);
                    auto.shootRings(command.get(1), (int) rings, -1);
                    break;
                }
                case 4: {
                    bucketUp = !bucketUp;
                    if (bucketUp) {
                        auto.shootRings(command.get(1), -1, -1);
                    }
                    else {
                        auto.intakeDrive(true, -1);
                    }
                    break;
                }
                case 5: {
                    wobbleUp = !wobbleUp;
                    if (wobbleUp) {
                        auto.wobbleMovement(0.7, wobbleLowPos, true);
                        sleep(800);
                        auto.wobbleMovement(-1, wobbleLiftPos, false);
                    } else {
                        auto.wobbleMovement(0.7, wobbleLowPos, true);
                        sleep(500);
                        auto.wobbleMovement(-1, wobbleLiftPos, true);
                    }
                    break;
                }
                case 6: {
                    intakeOn = !intakeOn;
                    if (intakeOn) {
                        auto.intakeDrive(true,1.0);
                    }
                    else {
                        auto.intakeDrive(true,0.0);
                    }
                    break;
                }
                case 7: {
                    double delay = command.get(1);
                    sleep((long) delay);
                    break;
                }
            }
            if (flywheelOn) {
                double distToTarget = Math.sqrt(Math.pow((robot.odoPod.returnFieldX() - robot.goalShotPos[0][aimingIndex]), 2) + Math.pow((robot.odoPod.returnFieldY() - robot.goalShotPos[1][aimingIndex]), 2));
                double desiredSpd = /*( (0.0375 * distToTarget) + 18)*/((0.075 * distToTarget) + 14)*12.0;
                wheelSpd = robot.fireAngle.ringToMotorSpeed(desiredSpd / 12.0);
            }
            else {
                wheelSpd = 0;
            }
            auto.setFlywheel(wheelSpd);
        }

        //------------------------------------------------------------------------------------------
        telemetry.addData("Saving Position", "Working...");
        telemetry.update();
        robot.bulkRead();
        ReadWriteFile.writeFile(XInch, String.valueOf(robot.odoPod.returnFieldX()));
        ReadWriteFile.writeFile(YInch, String.valueOf(robot.odoPod.returnFieldY()));
        ReadWriteFile.writeFile(RotDegree, String.valueOf((double) robot.imuDeg));
        telemetry.addData("Saving Position", "Done");
        telemetry.update();

    }

    //----------------------------------------------------------------------------------------------

    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = robot.VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }

}
