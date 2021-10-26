package org.firstinspires.ftc.teamcode.TeamCode_2021;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Button;
import org.firstinspires.ftc.teamcode.Utilities.FotR_ButtonToggle;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Controller;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Pathing;
import org.firstinspires.ftc.teamcode.Utilities.FotR_StickMin;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.firstinspires.ftc.teamcode.Utilities.MathFunctions.angleWrap;

@TeleOp (name = "Ringbearer TeleOp v4" , group = "TeleOp")
public class FotR_Ringbearer_TeleOp_v4 extends LinearOpMode {

    //Created needed objects for the code.
    FotR_Ringbearer_Hardware robot = new FotR_Ringbearer_Hardware();
    FotR_Ringbearer_AutoFunctions_v4 auto = new FotR_Ringbearer_AutoFunctions_v4(robot);
    FotR_ButtonToggle xToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle aToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle startP2Toggle = new FotR_ButtonToggle();
    FotR_ButtonToggle yToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle backToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle dpad2LToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle dpad2RToggle = new FotR_ButtonToggle();
    FotR_ButtonToggle lstick_P2 = new FotR_ButtonToggle();
    FotR_Button sl_P1 = new FotR_Button(true);
    FotR_Button sr_P1 = new FotR_Button(true);
    FotR_Button armCalibrate = new FotR_Button(true);
    FotR_ButtonToggle lStickToggle = new FotR_ButtonToggle();
    FotR_Controller PID = new FotR_Controller();
    ElapsedTime stickCheck = new ElapsedTime();
    ElapsedTime fireInterrupt = new ElapsedTime();
    ElapsedTime patternTimer = new ElapsedTime();
    ElapsedTime flywheelTimer = new ElapsedTime();
    ElapsedTime runTime = new ElapsedTime();

    //Array for colors related to the auto-aiming positions
    String positionPatterns[] = {"COLOR_WAVES_OCEAN_PALETTE","DARK_BLUE","SKY_BLUE","BLUE","COLOR_WAVES_OCEAN_PALETTE","COLOR_WAVES_LAVA_PALETTE","RED","RED_ORANGE","DARK_RED","COLOR_WAVES_LAVA_PALETTE"};

    //More objects, not sure why these two are down here instead of up with the rest
    FotR_Button du_P2 = new FotR_Button(true);
    FotR_Button dd_P2 = new FotR_Button(true);

    //Special objects, these are file objects that draw their contents from the following text files.
    File XInch = AppUtil.getInstance().getSettingsFile("XInch.txt");
    File YInch = AppUtil.getInstance().getSettingsFile("YInch.txt");
    File RotDegree = AppUtil.getInstance().getSettingsFile("RotDegree.txt");
    File AllianceSide = AppUtil.getInstance().getSettingsFile("AllianceSide.txt");
    double xIn, yIn, rotDeg;

    //Decimal Formatter for telemetry
    DecimalFormat df = new DecimalFormat("###,###.####");

    //Changing global variables
    private double powerMultiplier = 0;
    private double UpDown = 0;
    private double LeftRight = 0;
    private double Turn = 0;
    private double TrueAngle = 0;
    private double deckPos = 25;
    private double bucketPos = 0.0;
    private double prevFireRate = 0.0;
    private double wobblePos = 0;
    private int positionIndex = 0;
    private int aimingIndex = 0;
    private int wobbleIndex = 3;
    private double autoXPos = 0;
    private double autoYPos = 0;
    private double wheelSpd = 0.0;
    private double desiredSpd = 0.0;
    private double resetCalibrateTime = 0;
    private double currentPriority = 0;
    private String currentPattern = "BLACK";

    private double robotMecX = 0;
    private double robotMecY = 0;
    private double distToTarget = 0;

    //Changing global boolean variables
    private boolean gotoPointLock = false;
    private boolean autoAimRot = false;
    private boolean autoAimDeck = true;
    private boolean shooting = false;
    private boolean powerShotAuto = false;
    private boolean newPattern = true;
    private boolean endgameSignalled = false;

    private boolean blueAlliance = false;

    private boolean left2Pressed = false;
    private boolean right2Pressed = false;

    //Constant variables, usually only used in one place, but they are all gathered up here for convinence's sake.

    //Priority Constants for LED signals.  These are in orders of 100 so that any number of other signals can be inserted inbetween without needing to change all of the constants at once.
    private final int WOBBLE_GRABBER_PRIORITY = 100;
    private final int ENDGAME_PRIORITY = 200;
    private final int TARGET_SELECTOR_PRIORITY = 300;
    private final int FLYWHEEL_PRIORITY = 400;
    private final int AUTO_AIM_PRIORITY = 500;
    private final int RESTART_PRIORITY = 1000;



    //Robot Drive Speeds
    final private double medSpd = 0.7;
    final private double minSpd = 0.4;
    final private double maxSpd = 1.0;
    final private double aimSpd = 0.2;
    private double moveVar = 1;
    private double rotVar = 1;
    final private double conveyorSpd = 0.8;

    //Bucket Positions
    final private double dumpPos = /*0.65*/0.76;
    final private double intakePos = 0.04;
    final private double shootPos = 0.26;

    final private double fireRate = 200;
    final private double firePause = 100;

    //Variables for Auto-driving
    private double autoAimAngleRad = Math.toRadians(0);
    private double shotX = robot.goalShotPos[1][0];
    private double shotY = robot.goalShotPos[1][1];

    private boolean fireMode = false;
    private double patternDelay = 0;

    public void runOpMode() throws InterruptedException {
        //Initialize the Hardware map, set motor directions, and manually toggle any switches as needed.
        robot.init(hardwareMap);

        robot.leftIntake.setDirection(CRServo.Direction.FORWARD);
        robot.rightIntake.setDirection(CRServo.Direction.REVERSE);

        aToggle.setToggle(true);

        //Draw data from the files
        xIn = Double.parseDouble(ReadWriteFile.readFile(XInch).trim());
        yIn = Double.parseDouble(ReadWriteFile.readFile(YInch).trim());
        rotDeg = Double.parseDouble(ReadWriteFile.readFile(RotDegree).trim());

        blueAlliance = (0 > Double.parseDouble(ReadWriteFile.readFile(AllianceSide).trim()));

        //Init Wobble Arm
        robot.wobbleArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.wobbleArm.setTargetPosition(0);
        robot.wobbleArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.wobbleArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        xToggle.setToggle(blueAlliance);
        yToggle.setToggle(false);

        //Waiting screen, here you can toggle whether to use the stored Auto data and set the alliance color
        while (!opModeIsActive() && !isStopRequested()) {

            backToggle.toggle((gamepad1.back || gamepad2.back));
            telemetry.addData("Ignore Autonomous Position Data", backToggle.isToggled());

            xToggle.toggle((gamepad1.x || gamepad2.x));
            blueAlliance = xToggle.isToggled();
            if (blueAlliance) {telemetry.addData("Toggle X", "Blue Alliance");}
            else {telemetry.addData("Toggle X", "Red Alliance");}

            telemetry.update();

        }
        waitForStart();

        //Swing the wobble arm all the way towards it's limit switch, stop and reset once it's pressed
        robot.wobbleArm.setPower(0.7);
        robot.wobbleArm.setTargetPosition(1000);
        boolean calibrating = true;
        runTime.reset();
        while (calibrating && !gamepad2.dpad_up) {
            robot.bulkRead();
            calibrating = !robot.wobbleLimit.isPressed();
        }

        robot.wobbleArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.wobbleArm.setTargetPosition(0);
        robot.wobbleArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.wobbleArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        xToggle.setToggle(false);

        if (!blueAlliance) {
            aimingIndex = 9;
        }

        //Set last saved odo-pod positions if applicable
        if (!backToggle.isToggled()) {
            robot.odoPod.setFieldStart(xIn, yIn, rotDeg);
            robot.setAngleOffset(Math.toRadians(rotDeg));
        }

        //Reset timers for the first loop
        stickCheck.reset();
        fireInterrupt.reset();

        if (blueAlliance) {
            autoXPos = robot.goalShootingPos[0][0];
            autoYPos = robot.goalShootingPos[1][0];
        }
        else {
            autoXPos = robot.goalShootingPos[0][1];
            autoYPos = robot.goalShootingPos[1][1];
        }

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

        patternTimer.reset();
        while (opModeIsActive()) {

            //Bulk Read, this is the only time it is called in here
            robot.bulkRead();

            newPattern = false;

            //Function needed to use power shot volley, the control is read near the end of the loop so that a color signal can be sent
            if (powerShotAuto) {
                if (blueAlliance) {
                    auto.powerShotVolley(1, gamepad2);
                } else {
                    auto.powerShotVolley(2, gamepad2);
                }
                powerShotAuto = false;
            }

            //The basic speed of the robot wheels is calculated here.  A full pull of the right trigger should bring the speed to minSpd, while a full pull of the left should bring it to medSpd
            powerMultiplier = Math.abs(maxSpd - (gamepad1.right_trigger * (maxSpd - minSpd)) - (gamepad1.left_trigger * (maxSpd - medSpd)));

            //Button toggle to change the auto-driving position index
            sl_P1.updateState(gamepad1.left_bumper);
            sr_P1.updateState(gamepad1.right_bumper);
            if (sl_P1.returnState()) {
                positionIndex--;
            }
            if (sr_P1.returnState()) {
                positionIndex++;
            }

            //Calibrate Gyro
            if (gamepad1.back) {
                robot.setAngleOffset(robot.imuNeutral);
            }

            //Calibrate Odometry
            if (gamepad2.back) {
                robot.odoPod.setFieldStart(0, 0, 0);
            }

            //Gets the angle of the robot, TrueAngle is mostly a deprecated method of doing this, all calls to TrueAngle could be replaced by robot.imuRad
            TrueAngle = robot.imuRad;

            //Calcs distance to our target using trig
            distToTarget = Math.sqrt(Math.pow((robot.shooterX - robot.goalShotPos[0][aimingIndex]), 2) + Math.pow((robot.shooterY - robot.goalShotPos[1][aimingIndex]), 2));

            if (autoAimRot) {
                //This code tries to align the robot heading with a chosen auto-aim heading, setting the Turn variable to do so
                shotX = robot.goalShotPos[0][aimingIndex];
                shotY = robot.goalShotPos[1][aimingIndex];

                autoAimAngleRad = Math.atan((robot.shooterY-shotY) / (robot.shooterX-shotX));

                shotY += distToTarget*Math.sin(Math.toRadians(robot.boreSightError))
                        /
                        Math.cos(autoAimAngleRad + Math.toRadians(robot.boreSightError));

                autoAimAngleRad = Math.atan((robot.shooterY-shotY) / (robot.shooterX-shotX));

                double relAngle = angleWrap(TrueAngle - autoAimAngleRad);
                double hdgError = -Math.atan2(Math.sin(relAngle), Math.cos(relAngle));

                Turn = PID.limitedPID(hdgError, 0.9,0.05,0.01, Math.toRadians(23), Math.toRadians(10), Math.toRadians(30));
                addPattern("GOLD",1, AUTO_AIM_PRIORITY);
            }
            else {
                //Turn can also be manually set by a combination of stick inputs
                Turn = -((FotR_StickMin.StickMin(gamepad1.right_stick_x, 0.1)) + (aimSpd*FotR_StickMin.StickMin(gamepad2.right_stick_x,0.05)));
                if (gamepad2.right_stick_x != 0.0) {
                    addPattern("CP2_HEARTBEAT_FAST", 1000, AUTO_AIM_PRIORITY+1);
                }
            }

            //If any dpad buttons are pressed, use a if-else tree to set the X and Y movement variables manually
            if (gamepad1.dpad_up || gamepad1.dpad_down || gamepad1.dpad_left || gamepad1.dpad_right) {
                if (gamepad1.dpad_up && !gamepad1.dpad_down) {
                    robotMecX = -1;
                } else {
                    if (gamepad1.dpad_down) {
                        robotMecX = 1;
                    }
                    else {robotMecX = 0;}
                }
                if (gamepad1.dpad_left && !gamepad1.dpad_right) {
                    robotMecY = -1;
                } else {
                    if (gamepad1.dpad_right) {
                        robotMecY = 1;
                    }
                    else {robotMecY = 0;}
                }

                //Uses a non-field centric drive to move with the DPad
                robot.mecanum.WheelCalc(robotMecX, robotMecY, Turn, moveVar*powerMultiplier, rotVar*powerMultiplier);
            } else {

                //If we are auto-driving, use this code to set the X and Y variables according to how we need to move
                if (gotoPointLock) {
                    FotR_Pathing.goToPositionOneSpeed(robot.odoPod.returnFieldX(), robot.odoPod.returnFieldY(), TrueAngle, autoXPos, autoYPos, powerMultiplier, TrueAngle, true);
                    UpDown = FotR_Pathing.moveX;
                    LeftRight = FotR_Pathing.moveY;
                } else {
                    //Otherwise set the X and Y via manual stick inputs, reversing them if necessary for red alliance
                    UpDown = (blueAlliance ? -1 : 1) * FotR_StickMin.StickMin(-gamepad1.left_stick_x,0.1);
                    LeftRight = (blueAlliance ? -1 : 1) * FotR_StickMin.StickMin(gamepad1.left_stick_y,0.1);
                }

                //The ratio of power to rotation versus move is scaled based on how much lateral movement is wanted to create more manevurability in the robot
                rotVar = Math.abs(Turn) + Math.abs((1-aimSpd)*FotR_StickMin.StickMin(gamepad2.right_stick_x,0.05));
                moveVar = 1 - rotVar;

                //Takes the Turn, X, and Y move variables and calcs the wheel powers
                robot.mecanum.WheelCalcField(UpDown, LeftRight, Turn, moveVar*powerMultiplier, rotVar*powerMultiplier, TrueAngle);
            }

            //Uses Hardware helper function to set wheel speeds
            robot.setMecanumSpeeds(robot.mecanum);

            telemetry.addData("IMU Deg", df.format(robot.imuDeg));
            telemetry.addData("X Position", df.format(robot.odoPod.returnFieldX()));
            telemetry.addData("Y Position", df.format(robot.odoPod.returnFieldY()));

            //Player 2 -----------------------------------------------------------------------------
            //Trans-mode controls:
            //Shooter Wheels (toggle): A
            //Switch Fire/Intake Modes: X
            //Shooting Deck Angle: Right Stick
            //Enable Shooting Deck Lock: Right Stick Button
            //Wobble Arm: Dpad U and D
            //Wobble Claw (toggle): Start
            //Wobble Claw (partially open): Start (hold)
            //Wobble Grab (toggle): Left Stick Button

            //Fire Mode:
            //Shoot Ring: Left Trigger
            //Shoot Rings (with interrupter): Right Trigger (hold)

            //Intake Mode:
            //Intake: Right Trigger (hold)
            //Output: Left Trigger (hold)

            //Presets ------------------------------------------------------------------------------
            //Wobble Dump (toggle): Y
            //Cycle Targeting Point: Dpad L and R

            //Calibration --------------------------------------------------------------------------

            //Uses DPad toggles to cycle through targetting points for the auto-aim
            int aIndexRng = blueAlliance ? 0 : 5;
            dpad2LToggle.toggle(gamepad2.left_bumper);
            if (dpad2LToggle.isToggled() != left2Pressed && aimingIndex > aIndexRng) {
                left2Pressed = dpad2LToggle.isToggled();
                aimingIndex--;
                robot.fireAngle.setGoalHeight(robot.goalShotPos[2][aimingIndex]);
                addPattern(positionPatterns[aimingIndex], 1000, TARGET_SELECTOR_PRIORITY);
            }
            dpad2RToggle.toggle(gamepad2.right_bumper);
            if (dpad2RToggle.isToggled() != right2Pressed && aimingIndex < aIndexRng + 4) {
                right2Pressed = dpad2RToggle.isToggled();
                aimingIndex++;
                robot.fireAngle.setGoalHeight(robot.goalShotPos[2][aimingIndex]);
                addPattern(positionPatterns[aimingIndex], 1000, TARGET_SELECTOR_PRIORITY);
            }
            telemetry.addData("Aiming At", aimingIndex);

            //Toggle for Fire or Intake mode
            xToggle.toggle(gamepad2.x);
            fireMode = xToggle.isToggled();

            yToggle.toggle(gamepad2.y);
            if (yToggle.isToggled()) {
                bucketPos = dumpPos;
            }

            //Wobble Claw: -------------------------------------------------------------------------

            //Logic tree that sets the wobble claw to various constants from an array in Hardware
            startP2Toggle.toggle(gamepad2.start);
            if (startP2Toggle.isToggled() && !gamepad2.start) {
                robot.wobbleClaw.setPosition(robot.wobbleClawPos[2]);
            }
            else {
                if (!gamepad2.start) {
                    robot.wobbleClaw.setPosition(robot.wobbleClawPos[0]);
                }
            }
            if (gamepad2.start) {
                robot.wobbleClaw.setPosition(robot.wobbleClawPos[1]);
            }

            //Transmode settings
            //This is the optimal speed that rings need to leave the robot at to hit the target, equation was created through testing and a spreadsheet
            desiredSpd = /*( (0.0375 * distToTarget) + 18)*/((0.075 * distToTarget) + 14)*12.0;
            //telemetry.addData("Desired Spd", df.format(desiredSpd));
            //telemetry.addData("DistToTarget", df.format(distToTarget));

            //Toggle to convert desired speed to the actual speed the wheels need to be at, via a helper function in Trajectories
            aToggle.toggle(gamepad2.a);
            if (!aToggle.isToggled()) {
                wheelSpd = robot.fireAngle.ringToMotorSpeed(desiredSpd/12.0);
            }
            else {
                wheelSpd = 0;
            }
            //Flywheel Timer is just used for the color pattern setup, to find how long to hold the pattern up
            if (gamepad2.a && wheelSpd != 0) {
                flywheelTimer.reset();
            }
            if (flywheelTimer.milliseconds() < 3000) {
                if (Math.abs(wheelSpd - robot.aftShooter.getVelocity()) < 100) {
                    addPattern("DARK_GREEN",2000, FLYWHEEL_PRIORITY);
                }
                else {
                    addPattern("COLOR_WAVES_FOREST_PALETTE",1, FLYWHEEL_PRIORITY-1);
                }
            }

            //Calculating distance to target for shooting, a cut-off at X=24 is implemented to prevent the robot trying to auto-aim at impossible angles
            if (robot.odoPod.returnFieldX() < 24) {
                distToTarget = Math.sqrt(Math.pow((robot.odoPod.returnFieldX() - robot.goalShotPos[0][aimingIndex]), 2) + Math.pow((robot.odoPod.returnFieldY() - robot.goalShotPos[1][aimingIndex]), 2));
            }
            else {
                distToTarget = Math.sqrt(Math.pow((24 - robot.goalShotPos[0][aimingIndex]), 2) + Math.pow((robot.odoPod.returnFieldY() - robot.goalShotPos[1][aimingIndex]), 2));
            }

            robot.aftShooter.setVelocity(wheelSpd);
            robot.foreShooter.setVelocity(wheelSpd);
            telemetry.addData("Aft Shooter Wheel",df.format(robot.aftShooter.getVelocity()));

            //Uses helper functions from Trajectories to find an auto-aim angle
            if (autoAimDeck) {
                deckPos = Math.toDegrees(robot.fireAngle.findAngleFixedVelocity(desiredSpd, distToTarget));
            }

            //telemetry.addData("Deck Angle", df.format(deckPos));


            // Deckpos range should be between 0.25 and 0.80 to keep pulse width between 1000ms and 2000ms
            // whereas the REV hub default pulse width range is 500ms to 2500ms
            if (deckPos < 20.81571/*0.25*/) deckPos = 20.81571;
            if (deckPos > 32/*41.48243*//*0.48*/) deckPos = 32;

            //Sets both Bucket and Shooter Deck servos to position based on the variables set otherplaces in the code
            robot.shooterLift.setPosition(robot.degToServo(deckPos));
            robot.bucketArm.setPosition(bucketPos);

            //In firing Mode: ----------------------------------------------------------------------
            if (fireMode) {
                if (!yToggle.isToggled()) {bucketPos = shootPos;}

                //While the right trigger is held, use a timer to intermittently tell the servo to shoot
                if (gamepad2.right_trigger >= 0.9) {
                    shooting = (fireInterrupt.milliseconds() >= (prevFireRate + fireRate));
                    if (shooting && fireInterrupt.milliseconds() >= (prevFireRate + fireRate + firePause)) {
                        prevFireRate = fireInterrupt.milliseconds();
                    }
                }
                //Use the left trigger to manually tell the servo to shoot
                else {
                    if (gamepad2.left_trigger >= 0.1) {
                        shooting = true;
                    }
                    else {
                        shooting = false;
                        prevFireRate = fireInterrupt.milliseconds();
                    }
                }

                //React to the boolean set by the previous code to shoot the rings
                if (shooting) {
                    robot.flicker.setPosition(robot.flickShoot);
                }
                else {robot.flicker.setPosition(robot.flickDock);}

                //Shutdown leftovers from other mode
                robot.leftIntake.setPower(0.0);
                robot.rightIntake.setPower(0.0);
                robot.conveyor.setPower(0.0);
            }
            //In intake Mode: ----------------------------------------------------------------------
            else {
                if (!yToggle.isToggled()) {bucketPos = intakePos;}

                //Scales power to conveyor and intake wheels based on whether the left or right trigger is pressed
                if (gamepad2.left_trigger != 0) {
                    robot.leftIntake.setPower(-gamepad2.left_trigger);
                    robot.rightIntake.setPower(-gamepad2.left_trigger);
                    robot.conveyor.setPower(-gamepad2.left_trigger * conveyorSpd);
                }
                else {
                    robot.leftIntake.setPower(gamepad2.right_trigger);
                    robot.rightIntake.setPower(gamepad2.right_trigger);
                    robot.conveyor.setPower(gamepad2.right_trigger * conveyorSpd);
                }

                //Shutdown leftovers from other mode
                robot.flicker.setPosition(robot.flickDock);
            }

            //Manual Overrides: --------------------------------------------------------------------

            //Controls for moving the wobble arm up and down, uses Button helper to target a specific index point
            dd_P2.updateState(gamepad2.dpad_down);
            du_P2.updateState(gamepad2.dpad_up);

            if (du_P2.returnState() && wobbleIndex < robot.wobbleArmPos.length-1) {
                wobbleIndex++;
            }
            if (dd_P2.returnState() && wobbleIndex > 0) {
                wobbleIndex--;
                resetCalibrateTime = runTime.milliseconds();
            }
            if (!gamepad2.dpad_down || gamepad2.b || robot.wobbleLimit.isPressed()) {
                resetCalibrateTime = -1;
            }
            if (!gamepad2.b && !calibrating) {
                wobblePos = robot.wobbleArmPos[wobbleIndex];
            }

            robot.wobbleArm.setPower(1.0);
            robot.wobbleArm.setTargetPosition((int) wobblePos);

            //Sets wobble grabber position
            lStickToggle.toggle(gamepad2.left_stick_button && !gamepad2.b);
            if (lStickToggle.isToggled()) {
                robot.wobbleGrabber.setPosition(0.0);
            }
            else {
                robot.wobbleGrabber.setPosition(0.5);
            }
            if (robot.wobbleGrabber.getPosition() == 0.0) {
                addPattern("VIOLET", 1, WOBBLE_GRABBER_PRIORITY);
            }

            //ringCounting.updateState(robot.ringCounter.getDistance(DistanceUnit.CM) < 1.5);
            //ringCounting.returnState();

            //Reads Lidar for Color Pattern
            telemetry.addData("Distance", df.format(robot.ringCounter.getDistance(DistanceUnit.CM)));
            if (robot.lidar(1.7)) {
                addPattern("BEATS_PER_MINUTE_FOREST_PALETTE",1, FLYWHEEL_PRIORITY+1);
            }

            armCalibrate.updateState((runTime.milliseconds() - resetCalibrateTime) > 2000 && resetCalibrateTime != -1);

            //Stick Checkup ------------------------------------------------------------------------
            //Stick check is a way to read certain sensors only a few times a second, rather than being dependant on the random tick cycle
            if (stickCheck.milliseconds() >= 100) {

                //Adjust Deck Position
                deckPos += 0.5 * -gamepad2.right_stick_y;

                //Manually adjust wobble arm position
                if (gamepad2.dpad_down && !robot.wobbleLimit.isPressed() && gamepad2.b) {
                    wobblePos += 50;
                }
                if (gamepad2.dpad_up && gamepad2.b) {
                    wobblePos -= 50;
                }
                stickCheck.reset();
            }

            if (robot.wobbleLimit.isPressed() && calibrating) {
                //Recalibrate Wobble Arm when limit switch is pressed
                robot.wobbleArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                robot.wobbleArm.setTargetPosition(0);
                robot.wobbleArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                robot.wobbleArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                wobbleIndex = 2;
                resetCalibrateTime = -2;
            }

            //Code to auto recalibrate the arm, triggered by controls further up
            if (calibrating) {
                wobblePos = 10000;
                calibrating = !robot.wobbleLimit.isPressed() && !gamepad2.dpad_up;
            }
            else {
                if (resetCalibrateTime != -2) {
                    calibrating = ((runTime.milliseconds() - resetCalibrateTime) > 2000 && resetCalibrateTime != -1);
                }
            }

            //Several booleans that look at whether a button is pressed or a stick is moved to set boolean toggles
            if (Math.abs(gamepad1.right_stick_x) >= 0.1 || Math.abs(gamepad2.right_stick_x) >= 0.1) {
                autoAimRot = false;
            }
            if (gamepad1.right_stick_button) {
                autoAimRot = true;
            }

            if (Math.abs(gamepad2.right_stick_y) >= 0.15) {
                autoAimDeck = false;
            }
            if (gamepad2.right_stick_button) {
                autoAimDeck = true;
            }

            if (Math.abs(gamepad1.left_stick_x) >= 0.1 || Math.abs(gamepad1.left_stick_y) >= 0.1 || FotR_Pathing.distToTarget < 3) {
                gotoPointLock = false;
            }
            if (gamepad1.left_stick_button) {
                gotoPointLock = true;
            }

            //This code is a warning for endgame in the form of a color pattern
            if(runTime.milliseconds() > 90000 && !endgameSignalled) {
                if (runTime.milliseconds() > 100000) {
                    addPattern("ORANGE", 2000, ENDGAME_PRIORITY);
                    endgameSignalled = true;
                }
                else {
                    addPattern("CP1_2_COLOR_WAVES", 10000, ENDGAME_PRIORITY-1);
                }
            }

            //Code to start the PowerShotVolley process
            if (gamepad2.b && gamepad2.left_stick_button) {
                powerShotAuto = true;
                addPattern("CP1_BREATH_FAST",5000, WOBBLE_GRABBER_PRIORITY+1);
            }

            //Code to set the pattern setup however the previous code has set
            addPattern("BLACK", 1, RESTART_PRIORITY-1);
            if (newPattern) {
                patternTimer.reset();
                robot.setPattern(currentPattern);
            }
            if (patternTimer.milliseconds() > patternDelay) {
                //robot.setPattern(currentPattern);
                //patternTimer.reset();
                currentPriority = RESTART_PRIORITY;
            }

            telemetry.update();
        }
    }

    //Special method that the rest of the code can use to set a pattern for a period of time.  Colors of a higher priority are ignored, the priority will reset when the time set has expired.
    private void addPattern(String name, double MSdisplay, int priority) {
        if (priority <= currentPriority) {
            currentPriority = priority;
            currentPattern = name;
            patternDelay = MSdisplay;
            newPattern = true;
        }
    }
}
