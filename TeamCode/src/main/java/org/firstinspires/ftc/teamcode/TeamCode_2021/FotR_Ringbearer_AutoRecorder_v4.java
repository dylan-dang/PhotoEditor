package org.firstinspires.ftc.teamcode.TeamCode_2021;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Button;
import org.firstinspires.ftc.teamcode.Utilities.FotR_ButtonToggle;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Controller;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Pathing;
import org.firstinspires.ftc.teamcode.Utilities.FotR_StickMin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.firstinspires.ftc.teamcode.Utilities.MathFunctions.angleWrap;

//Most of this code is a direct copy of the standard TeleOp v4, the only new part is the newCommand method, explained below
@TeleOp(name = "Ringbearer Recorder v2" , group = "Autonomous Recorders")
public class FotR_Ringbearer_AutoRecorder_v4 extends LinearOpMode {

    //Objects
    FotR_Ringbearer_Hardware robot = new FotR_Ringbearer_Hardware();
    FotR_Ringbearer_AutoFunctions_v4 auto;
    FotR_FileManager files = new FotR_FileManager();
    FotR_ButtonToggle xToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle backToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle lBumperToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle rBumperToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle leftToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle rightToggle = new FotR_ButtonToggle();

    FotR_Button b_P1 = new FotR_Button(true);
    FotR_Button x_P1 = new FotR_Button(true);
    FotR_Button rt_P2 = new FotR_Button(true);
    FotR_Button rts_P2 = new FotR_Button(true);
    FotR_Button ls_P2 = new FotR_Button(true);
    FotR_Button x_P2 = new FotR_Button(true);
    FotR_Button a_P2 = new FotR_Button(true);
    FotR_Button dd_PU = new FotR_Button(true);
    FotR_Button ud_PU = new FotR_Button(true);
    FotR_Button lt_P2 = new FotR_Button(true);
    FotR_Button y_P1 = new FotR_Button(true);

    FotR_Controller PID = new FotR_Controller();
    ElapsedTime fireInterrupt = new ElapsedTime();
    ElapsedTime stallTimer = new ElapsedTime();

    ArrayList<ArrayList<ArrayList<Double>>> routesList = new ArrayList<ArrayList<ArrayList<Double>>>();
    ArrayList<ArrayList<Double>> activeSlot = new ArrayList<ArrayList<Double>>();
    ArrayList<Double> newCommand = new ArrayList<Double>();

    DecimalFormat df = new DecimalFormat("###,###.####");

    private double powerMultiplier = 0;
    private double UpDown = 0;
    private double LeftRight = 0;
    private double Turn = 0;
    private double TrueAngle = 0;
    private double deckPos = 25;
    private double bucketPos = 0.0;
    private double prevFireRate = 0.0;
    private double wobblePos = 0;
    private int aimingIndex = 0;
    private int programSlot = 0;
    private double autoXPos = 0;
    private double autoYPos = 0;
    private double wheelSpd = 0.0;
    private double desiredSpd = 0.0;

    private double distToTarget = 0;
    private int fireCount = 0;

    private boolean gotoPointLock = false;
    private boolean autoAimRot = false;
    private boolean shooting = false;

    private boolean recordingDone = false;
    private boolean saveDone = false;

    private boolean blueAlliance = false;

    private boolean left2Pressed = false;
    private boolean right2Pressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    //Robot Constants

    //Robot Drive Speeds
    final private double medSpd = 0.7;
    final private double minSpd = 0.4;
    final private double maxSpd = 1.0;
    final private double conveyorSpd = 0.8;
    final double wobbleLowPos = 775;
    final double wobbleLiftPos = 500;

    //Bucket Positions
    final private double intakePos = 0.01;
    final private double shootPos = intakePos + 0.248;

    final private double fireRate = 200;
    final private double firePause = 100;

    //Variables for Auto-driving
    private double autoAimAngleRad;
    private double shotX;
    private double shotY;

    private boolean fireMode = false;

    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap);

        robot.leftIntake.setDirection(CRServo.Direction.FORWARD);
        robot.rightIntake.setDirection(CRServo.Direction.REVERSE);

        robot.wobbleArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.wobbleArm.setTargetPosition(0);
        robot.wobbleArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.wobbleArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        robot.wobbleClaw.setPosition(robot.wobbleClawPos[2]);
        robot.shooterLift.setPosition(0.32);
        int startPos = 0;

        backToggle.setToggle(true);

        while (!opModeIsActive() && !isStopRequested()) {

            backToggle.toggle(gamepad1.back || gamepad2.back);
            telemetry.addData("Start with Standard Powershot", backToggle.isToggled());


            leftToggle.toggle(gamepad1.dpad_left || gamepad2.dpad_left);
            if (leftToggle.isToggled() != leftPressed && startPos > 0) {
                leftPressed = leftToggle.isToggled();
                startPos--;
            }
            rightToggle.toggle(gamepad1.dpad_right || gamepad2.dpad_right);
            if (rightToggle.isToggled() != rightPressed && startPos < 3) {
                rightPressed = rightToggle.isToggled();
                startPos++;
            }
            /*Start Positions:
              0: Blue Outer
              1: Blue Inner
              2: Red Inner
              3: Red Outer
             */
            telemetry.addData("DPad Left or Right", "");
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
        robot.odoPod.setFieldStart(robot.startPosCoor[0][startPos],robot.startPosCoor[1][startPos],0);
        blueAlliance = (startPos < 2);

        auto = new FotR_Ringbearer_AutoFunctions_v4(robot);

        auto.wobbleMovement(0.7,0,false);
        auto.wobbleMovement(0.7,wobbleLiftPos-150,false);

        xToggle.setToggle(false);

        if (!blueAlliance) {
            aimingIndex = 7;
        }

        fireInterrupt.reset();

        if (blueAlliance) {
            autoXPos = robot.goalShootingPos[0][0];
            autoYPos = robot.goalShootingPos[1][0];
        }
        else {
            autoXPos = robot.goalShootingPos[0][1];
            autoYPos = robot.goalShootingPos[1][1];
        }


        if (backToggle.isToggled()) {
            auto.robot.odoPod.setFieldStart(robot.startPosCoor[0][startPos],robot.startPosCoor[1][startPos],0);
            auto.shootRings(31.4,-1,1666);
            auto.moveTo(1.0,-9, robot.startPosCoor[1][startPos],0,false,true);
            auto.powerShotVolley(startPos, gamepad2);
            sleep(1000);
            newCommand.add(1.0);
        }
        else {
            newCommand.add(0.0);
        }

        newCommand.add((double) startPos);
        activeSlot.add(newCommand);
        newCommand = new ArrayList<>();

        //Actions during TeleOp
        //Player 1 ---------------------------------------------------------------------------------
        //Field Mecanum Translation Left/Right = Left Stick X
        //Field Mecanum Translation Fore/Aft = Left Stick Y
        //Mecanum Rotation = Right Stick X
        //Robot Mecanum Translation Left/Right = Dpad Left/Right
        //Robot Mecanum Translation Fore/Aft = Dpad Up/Down

        //Functions --------------------------------------------------------------------------------
        //Right Trigger = Restrict Movement to MinSpd
        //Left Trigger = Restrict Movement to MedSpd

        //Calibration ------------------------------------------------------------------------------
        //Reset Gyro Zero = Back

        while (opModeIsActive()) {
            if (!recordingDone) {
                robot.bulkRead();

                powerMultiplier = Math.abs(maxSpd - (gamepad1.right_trigger * (maxSpd - minSpd)) - (gamepad1.left_trigger * (maxSpd - medSpd)));


                if (gamepad1.back) {
                    robot.setAngleOffset(robot.imuNeutral);
                }

                if (gamepad2.back) {
                    robot.odoPod.setFieldStart(0, 0, 0);
                }

                TrueAngle = robot.imuRad;

                distToTarget = Math.sqrt(Math.pow((robot.shooterX - robot.goalShotPos[0][aimingIndex]), 2) + Math.pow((robot.shooterY - robot.goalShotPos[1][aimingIndex]), 2));

                if (autoAimRot) {
                    shotX = robot.goalShotPos[0][aimingIndex];
                    shotY = robot.goalShotPos[1][aimingIndex];

                    autoAimAngleRad = Math.atan((robot.shooterY - shotY) / (robot.shooterX - shotX));

                    shotY += distToTarget * Math.sin(Math.toRadians(robot.boreSightError))
                            /
                            Math.cos(autoAimAngleRad + Math.toRadians(robot.boreSightError));

                    autoAimAngleRad = Math.atan((robot.shooterY - shotY) / (robot.shooterX - shotX));

                    double relAngle = angleWrap(TrueAngle - autoAimAngleRad);
                    double hdgError = -Math.atan2(Math.sin(relAngle), Math.cos(relAngle));

                    Turn = PID.limitedPID(hdgError, 0.9, 0.05, 0.01, Math.toRadians(23), Math.toRadians(10), Math.toRadians(30));
                } else {
                    Turn = -((FotR_StickMin.StickMin(gamepad1.right_stick_x, 0.1)) + (0.2 * FotR_StickMin.StickMin(gamepad2.right_stick_x, 0.05)));
                }

                UpDown = (blueAlliance ? -1 : 1) * FotR_StickMin.StickMin(-gamepad1.left_stick_x, 0.1);
                LeftRight = (blueAlliance ? -1 : 1) * FotR_StickMin.StickMin(gamepad1.left_stick_y, 0.1);

                robot.mecanum.WheelCalcField(UpDown, LeftRight, Turn, powerMultiplier, TrueAngle);

                robot.setMecanumSpeeds(robot.mecanum);

                telemetry.addData("IMU Deg", df.format(robot.imuDeg));
                telemetry.addData("X Position", df.format(robot.odoPod.returnFieldX()));
                telemetry.addData("Y Position", df.format(robot.odoPod.returnFieldY()));

                //Add a move command (press a to turn it into a skate)
                b_P1.updateState(gamepad1.b);
                if (b_P1.returnState()) {
                    newCommand.add(0.0);
                    newCommand.add(powerMultiplier);
                    newCommand.add(robot.odoPod.returnFieldX());
                    newCommand.add(robot.odoPod.returnFieldY());
                    newCommand.add(robot.imuDeg);
                    if (gamepad1.a) {
                        newCommand.add(1.0);
                    }
                    else {
                        newCommand.add(0.0);
                    }
                    //Might need to add a 0 in logic
                    activeSlot.add(newCommand);
                    newCommand = new ArrayList<>();
                }

                //Add a turn command
                x_P1.updateState(gamepad1.x);
                if (x_P1.returnState()) {
                    newCommand.add(1.0);
                    newCommand.add(robot.imuDeg);

                    activeSlot.add(newCommand);
                    newCommand = new ArrayList<>();
                }

                y_P1.updateState(gamepad1.y);

                if (y_P1.returnState()) {
                    stallTimer.reset();
                    boolean waiting = true;
                    double delay = 500;
                    double increment = 100;

                    while (waiting) {
                        ud_PU.updateState(gamepad1.dpad_up);
                        dd_PU.updateState(gamepad1.dpad_down);
                        y_P1.updateState(gamepad1.y);
                        if (gamepad1.dpad_left) {
                            increment = 50;
                        }
                        else if (gamepad1.dpad_right) {
                            increment = 1000;
                        }
                        else {
                            increment = 100;
                        }
                        if (ud_PU.returnState()) {
                            delay += increment;
                        }
                        if (dd_PU.returnState()) {
                            delay -= increment;
                        }
                        if (delay < 0) {
                            delay = 0;
                        }
                        telemetry.addData("Time", delay);
                        telemetry.update();
                        if (y_P1.returnState()) {
                            waiting = false;
                        }
                    }
                    newCommand.add(7.0);
                    newCommand.add(delay);
                    activeSlot.add(newCommand);
                    newCommand = new ArrayList<>();
                }


                //Player 2 -----------------------------------------------------------------------------
                //Trans-mode controls:
                //Shooter Wheels (toggle): A
                //Switch Fire/Intake Modes: X
                //Shooting Deck Angle: Right Stick
                //Enable Shooting Deck Lock: Right Stick Button

                //Fire Mode:
                //Shoot Ring: Left Trigger
                //Shoot Rings (with interrupter): Right Trigger (hold)

                //Intake Mode:
                //Turn On Intake: Right Trigger
                //Turn Off Intake: Left Trigger

                //Presets ------------------------------------------------------------------------------
                //Cycle Targeting Point: Dpad L and R

                //Calibration --------------------------------------------------------------------------


                int aIndexRng = blueAlliance ? 0 : 5;
                lBumperToggle.toggle(gamepad2.left_bumper);
                if (lBumperToggle.isToggled() != left2Pressed && aimingIndex > aIndexRng) {
                    left2Pressed = lBumperToggle.isToggled();
                    aimingIndex--;
                    robot.fireAngle.setGoalHeight(robot.goalShotPos[2][aimingIndex]);
                }
                rBumperToggle.toggle(gamepad2.right_bumper);
                if (rBumperToggle.isToggled() != right2Pressed && aimingIndex < aIndexRng + 4) {
                    right2Pressed = rBumperToggle.isToggled();
                    aimingIndex++;
                    robot.fireAngle.setGoalHeight(robot.goalShotPos[2][aimingIndex]);
                }
                telemetry.addData("Aiming At", aimingIndex);

                x_P2.updateState(gamepad2.x);
                fireMode = x_P2.returnAltState();

                if (x_P2.returnState()) {
                    newCommand.add(4.0);
                    newCommand.add(deckPos);
                    activeSlot.add(newCommand);
                    newCommand = new ArrayList<>();
                }

                //Wobble Claw: -------------------------------------------------------------------------

                //New Wobble Claw Code Needed-----------------------------------------------------------------------------------------------------------------

                //Transmode settings

                desiredSpd = ((0.0375 * distToTarget) + 18) * 12.0;

                a_P2.updateState(gamepad2.a);
                if (!a_P2.returnAltState()) {
                    wheelSpd = robot.fireAngle.ringToMotorSpeed(desiredSpd / 12.0);
                } else {
                    wheelSpd = 0;
                }
                if (a_P2.returnState()) {
                    newCommand.add(2.0);
                    newCommand.add(wheelSpd);
                    activeSlot.add(newCommand);
                    newCommand = new ArrayList<>();
                }

                distToTarget = Math.sqrt(Math.pow((robot.odoPod.returnFieldX() - robot.goalShotPos[0][aimingIndex]), 2) + Math.pow((robot.odoPod.returnFieldY() - robot.goalShotPos[1][aimingIndex]), 2));

                robot.aftShooter.setVelocity(wheelSpd);
                robot.foreShooter.setVelocity(wheelSpd);

                deckPos = Math.toDegrees(robot.fireAngle.findAngleFixedVelocity(desiredSpd, distToTarget));

                // Deckpos range should be between 0.25 and 0.80 to keep pulse width between 1000ms and 2000ms
                // whereas the REV hub default pulse width range is 500ms to 2500ms
                if (deckPos < 20.81571/*0.25*/) deckPos = 20.81571;
                if (deckPos > 32/*41.48243*//*0.48*/) deckPos = 32;

                robot.shooterLift.setPosition(robot.degToServo(deckPos));
                robot.bucketArm.setPosition(bucketPos);

                //In firing Mode: ----------------------------------------------------------------------
                if (fireMode) {

                    bucketPos = shootPos;



                    if (gamepad2.right_trigger >= 0.9) {
                        telemetry.addData("Ring Count", fireCount);
                        shooting = (fireInterrupt.milliseconds() >= (prevFireRate + fireRate));
                        if (shooting && fireInterrupt.milliseconds() >= (prevFireRate + fireRate + firePause)) {
                            prevFireRate = fireInterrupt.milliseconds();
                        }
                    } else {
                        //Add single shot command
                        lt_P2.updateState(gamepad2.left_trigger > 0.1);
                        if (lt_P2.returnState()) {
                            shooting = true;
                            newCommand.add(3.0);
                            newCommand.add(deckPos);
                            newCommand.add(1.0);
                            activeSlot.add(newCommand);
                            newCommand = new ArrayList<>();
                        } else {
                            shooting = false;
                            prevFireRate = fireInterrupt.milliseconds();
                        }
                    }
                    rts_P2.updateState(shooting);
                    if (shooting) {
                        //0.75 closed to 0.9 open
                        if (rts_P2.returnState()) {
                            fireCount++;
                        }
                        robot.flicker.setPosition(robot.flickShoot);
                    } else {
                        robot.flicker.setPosition(robot.flickDock);
                    }

                    if (fireCount > 0 && gamepad2.right_trigger < 0.9) {
                        newCommand.add(3.0);
                        newCommand.add(deckPos);
                        newCommand.add((double) fireCount);
                        activeSlot.add(newCommand);
                        newCommand = new ArrayList<>();
                        fireCount = 0;
                    }

                }
                //In intake Mode: ----------------------------------------------------------------------
                else {
                    bucketPos = intakePos;

                    rt_P2.updateState(gamepad2.right_trigger > 0.9);

                    if (rt_P2.returnAltState()) {
                        robot.leftIntake.setPower(1.0);
                        robot.rightIntake.setPower(1.0);
                        robot.conveyor.setPower(1.0*conveyorSpd);
                    }
                    else {
                        robot.leftIntake.setPower(0.0);
                        robot.rightIntake.setPower(0.0);
                        robot.conveyor.setPower(0.0);
                    }

                    if (rt_P2.returnState()) {
                        //Add intake toggle
                        newCommand.add(6.0);
                        activeSlot.add(newCommand);
                        newCommand = new ArrayList<>();

                    }

                    //Shutdown leftovers from other mode
                    robot.flicker.setPosition(robot.flickDock);
                }

                ls_P2.updateState(gamepad2.left_stick_button);
                if (ls_P2.returnState()) {
                    if (ls_P2.returnAltState()) {
                        auto.wobbleMovement(0.7, wobbleLowPos, true);
                        sleep(500);
                        auto.wobbleMovement(-1, wobbleLiftPos, true);
                    } else {
                        auto.wobbleMovement(0.7, wobbleLowPos, true);
                        sleep(800);
                        auto.wobbleMovement(-1, wobbleLiftPos, false);
                    }
                    newCommand.add(5.0);
                    activeSlot.add(newCommand);
                    newCommand = new ArrayList<>();
                }

                //Stick Checkup ------------------------------------------------------------------------
                if (Math.abs(gamepad1.right_stick_x) >= 0.1 || Math.abs(gamepad2.right_stick_x) >= 0.1) {
                    autoAimRot = false;
                }
                if (gamepad1.right_stick_button) {
                    autoAimRot = true;
                }

                if (Math.abs(gamepad1.left_stick_x) >= 0.1 || Math.abs(gamepad1.left_stick_y) >= 0.1 || FotR_Pathing.distToTarget < 3) {
                    gotoPointLock = false;
                }
                if (gamepad1.left_stick_button) {
                    gotoPointLock = true;
                }

                telemetry.update();
                recordingDone = (gamepad2.start && gamepad2.back && gamepad1.start && gamepad1.back);
            }

            //New Command is an array that a full command is stored in (see AutoFunctions for the key)
            //Each time it is created, added to the activeSlot array, then wiped again.
            //Once this loop is done, the activeSlot is taken and saved with the FileManager

            else {
                if (!saveDone) {
                    try {
                        files.saveFile(activeSlot, gamepad1, gamepad2, telemetry);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    saveDone = true;
                }
                else {
                    telemetry.addData("All Clear", "You may stop the OpMode Now");
                    telemetry.update();
                }
            }
        }


    }

}
