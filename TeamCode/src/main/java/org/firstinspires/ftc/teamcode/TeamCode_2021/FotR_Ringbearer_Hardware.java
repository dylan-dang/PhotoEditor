package org.firstinspires.ftc.teamcode.TeamCode_2021;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Odometry.FotR_OdoGlobalPosition;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Button;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Mecanum;
import org.firstinspires.ftc.teamcode.Utilities.FotR_Trajectories;

import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit.CM;
import static org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit.MM;

public class FotR_Ringbearer_Hardware {

    //Ringbearer Config Setup:
    //Directions from Back
    //Hub 3 (Control Left):
    //Motors: P0: Left Front Drive, P1: Right Front Drive, P2: Left Back Drive, P3: Right Back Drive
    //Servos: P0: Bucket Flicker P2: Wobble Grabber P5: Wobble Claw
    //Sensors: P0:
    //Hub 2 (Expansion Right):
    //Motors: P0: Wobble Arm P1: Fore Shooter P2: Conveyor P3: Aft Shooter (Encoder)
    //Servos: P0: Bucket Rotate P1: Shooter Lift (Linear) P2: Right Intake P3: Left Intake
    //Sensors: P0: IMU(ZYX), LIDAR(Ring Counter)

    //Declare initial empty variables
    public DcMotor leftFrontDrive = null;
    public DcMotor leftBackDrive = null;
    public DcMotor rightFrontDrive = null;
    public DcMotor rightBackDrive = null;
    public DcMotorEx foreShooter = null;
    public DcMotorEx aftShooter = null;
    public DcMotor wobbleArm = null;
    public DcMotor odo1Pos = null;
    public DcMotor odo2Pos = null;
    public DcMotor odo3Pos = null;
    public Servo flicker = null;
    public DcMotor conveyor = null;
    public Servo wobbleClaw = null;
    public Servo bucketArm = null;
    public Servo shooterLift = null;
    public CRServo rightIntake = null;
    public CRServo leftIntake = null;
    public Servo wobbleGrabber = null;
    public ColorRangeSensor ringCounter = null;
    public TouchSensor wobbleLimit = null;
    FotR_Button ringCountButton = new FotR_Button(true);
    FotR_Mecanum mecanum = null;
    FotR_OdoGlobalPosition odoPod = null;
    FotR_Trajectories fireAngle = null;
    List<LynxModule> allHubs = null;
    RevBlinkinLedDriver blinkin;
    RevBlinkinLedDriver.BlinkinPattern pattern;
    ElapsedTime lidarTimer = new ElapsedTime();



    //Variables needed for certain functions
    public double imuDeg = 0;
    public double imuRad = 0;
    public double imuNeutral = 0;
    public double offsetDeg = 0;
    public double shooterX = 0;
    public double shooterY = 0;
    private double maxDistCM = 0;
    public boolean lidarSensed = false;

    public static final String VUFORIA_KEY =
            "AQxsxl//////AAABmTy+bXqV2UlOrwdONYTvqHGHZ39nF2Mecvi5Mr5kZVJBcO65dYNLe3ZW5VHqXwk8dCbJSMPM6ZIZGO/CyiYDzx/NpQ0JcirDXSxdOQ+o979t7vrn02/kfRUNlzuOeaUeDm3zEgjwRXd/oGYAGkE0HM8ToByzF2DxCPVY2JviOhw2e3fqZSbxAFUtYclYj7RK0ox1i5MpYHbmOjHFjWJ8rolYf0JrTGoBWBH1uL/pFwYvZgHXu3yCpVja7YFT8TCopz6HZ+AJHu1JMAzuoF5ysNloDvEFEEOjhio1Jw/s/+p71UerQKRNXZrCah4Ir0G0UfNNY/z5Ep/JMm3izl2mdITw8sRKD4stnTLpqcwbFTWt";

    public BNO055IMU imu = null;
    Orientation anglesDeg;
    Orientation anglesRad;

    //Arrays for field positioning
    public final String startPosNames[] = {"Blue Outer Pole","Blue Inner Pole","Red Inner Pole","Red Outer Pole"};
    //All arrays: first row is X [0][#], second row is Y [1][#], third row is Z [2][#] (height)
    //These are predefined points on the field, according to field layout (in inchs)
    public final double startPosCoor[][] = {{-63.000,-63.000,-63.000,-63.000}, //Slots correspond to names above
                                            { 58.800, 17.250,-17.250,-56.250}};
    public final double goalShotPos[][] = {{ 72.0, 72.0, 72.0, 72.0, 72.0, 72.0, 72.0, 72.0, 72.0, 72.0}, //Counts left to right, starting from blue side (inner-most two are the mid-goals)
                                           { 36.0, 18.5, 11.0, 03.5, 36.0, 36.0,-03.5,-11.0,-18.5,-36.0},
                                           { 33.0, 30.0, 30.0, 30.0, 27.0, 27.0, 30.0, 30.0, 30.0, 33.0}};

    //These are coordinates for the robot to align on, presets according to drive strategy
    public final double powerShootingPos[][] = {{-01.0,-01.0}, //0 is Blue, 1 is Red
                                                { 18.5,-18.5}};
    public final double goalShootingPos[][] =  {{-08.0,-08.0}, // 0 is Blue, 1 is Red
                                                { 36.0,-36.0}};

    public final double wobbleClawPos[] = {0.52,0.77,0.89}; //[0] is Open, [1] is Barely Open, [2] is Closed
    public final double wobbleArmPos[] = {-100,-350,-600,-750,-850}; //Low to Grab, Just Above Top, Raised to Lift, Raised to Block Shots

    //Extra constants used by both TeleOp and Auto
    public final double flickShoot = 0.78;
    public final double flickDock = 0.9;
    private final double maxRPM1to1 = 2380;


    //Coordinate points on the robot, relative to odopod origin
    private final double shooterPosition[] = {1.3,-6.15}; //[0] is X, [1] is Y
    public final double boreSightError = 0.5;

    /* local OpMode members. */
    HardwareMap hwMap =  null;

    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Define and Initialize Motors
        leftFrontDrive = hwMap.dcMotor.get("l_f_drive");
        leftBackDrive = hwMap.dcMotor.get("l_b_drive");
        rightFrontDrive = hwMap.dcMotor.get("r_f_drive");
        rightBackDrive = hwMap.dcMotor.get("r_b_drive");
        //Max RPM of motors is 2,380 ticks per second
        foreShooter = hwMap.get(DcMotorEx.class,"f_shooter");
        aftShooter = hwMap.get(DcMotorEx.class,"b_shooter");
        conveyor = hwMap.dcMotor.get("conveyor");
        wobbleArm = hwMap.dcMotor.get("w_arm");

        odo1Pos = hwMap.dcMotor.get("l_f_drive");
        odo2Pos = hwMap.dcMotor.get("r_f_drive");
        odo3Pos = hwMap.dcMotor.get("l_b_drive");

        //Define and Initialize Servos
        flicker = hwMap.servo.get("flicker");
        wobbleClaw = hwMap.servo.get("w_claw");
        bucketArm = hwMap.servo.get("b_rotate");
        shooterLift = hwMap.servo.get("s_lift");
        rightIntake = hwMap.crservo.get("r_intake");
        leftIntake = hwMap.crservo.get("l_intake");
        wobbleGrabber = hwMap.servo.get("w_grab");

        ringCounter = hwMap.get(ColorRangeSensor.class, "ring_counter");
        wobbleLimit = hwMap.get(TouchSensor.class, "w_limit");
        //wobbleLimit.setMode(DigitalChannel.Mode.INPUT);

        //Turn on blinkin and set it to Black (aka no light)
        blinkin = hwMap.get(RevBlinkinLedDriver.class, "blinkin");
        setPattern("BLACK");

        //Calibrate Gyro
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hwMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        IMUangles();

        //Set the directions to the correct direction because two are flipped
        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        odo1Pos.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        odo2Pos.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        odo3Pos.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        odo1Pos.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        odo2Pos.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        odo3Pos.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        aftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        foreShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //Creating all of the algorithm blocks
        fireAngle = new FotR_Trajectories(goalShotPos[2][0],14.5);
        mecanum = new FotR_Mecanum(0.19, 0.16, 0.01);
        odoPod = new FotR_OdoGlobalPosition(odo1Pos, odo2Pos, odo3Pos,75);

        //Sets the Hubs to BulkRead mode to increase odometry reliability
        allHubs = ahwMap.getAll(LynxModule.class);
        for (LynxModule module : allHubs) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

    }

    public void IMUangles () {
        //Refreshes angle measurements.
        anglesDeg = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.YZX, AngleUnit.DEGREES);
        anglesRad = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.YZX, AngleUnit.RADIANS);
    }

    //Two helper functions to skip a pile of lines when adjusting drive power.
    public void setDriveSpeeds (double m0, double m1, double m2, double m3) {
        leftFrontDrive.setPower(m0);
        rightFrontDrive.setPower(m1);
        leftBackDrive.setPower(m2);
        rightBackDrive.setPower(m3);
    }
    public void setMecanumSpeeds (FotR_Mecanum mecanum) {
        leftFrontDrive.setPower(mecanum.getPower(0));
        rightFrontDrive.setPower(mecanum.getPower(1));
        leftBackDrive.setPower(mecanum.getPower(2));
        rightBackDrive.setPower(mecanum.getPower(3));
    }

    //Converter functions for auto-aim
    public double pwrToRate (double pwr) {
        return pwr * maxRPM1to1;
    }

    public double degToServo(double angleDeg) {
        return (0.011129*angleDeg)+0.018342;
    }

    //Called once at the beginning of a loop to refresh current readings of sensors, every new reading needs to be added here once to properly update.
    public void bulkRead () {

        for (LynxModule module : allHubs) {
            module.clearBulkCache();
        }

        IMUangles();
        imuNeutral = anglesDeg.secondAngle;
        imuDeg = imuNeutral - offsetDeg;
        imuRad = Math.toRadians(imuDeg);

        ringCounter.getDistance(CM);
        ringCountButton.updateState(ringCounter.getDistance(CM) < maxDistCM);
        wobbleLimit.isPressed();

        aftShooter.getVelocity();
        foreShooter.getVelocity();

        wobbleArm.getCurrentPosition();
        wobbleClaw.getPosition();

        odoPod.updateSensors(odo1Pos.getCurrentPosition(),odo2Pos.getCurrentPosition(),odo3Pos.getCurrentPosition(),imuRad);
        odoPod.globalCoordinatePositionUpdate();

        //Calculate the location of the ring's exit location from the robot
        shooterX = odoPod.returnFieldX() + (shooterPosition[0]*Math.cos(imuRad) - shooterPosition[1]*Math.sin(imuRad));
        shooterY = odoPod.returnFieldY() + (shooterPosition[0]*Math.sin(imuRad) + shooterPosition[1]*Math.cos(imuRad));

    }

    //Used to reset gyro location
    public void setAngleOffset (double angleDeg) {offsetDeg = angleDeg;}

    //Special timed function that looks to see if the Lidar is under the limit for a certain amount of time.
    public boolean lidar (double maxDistCM) {
        this.maxDistCM = maxDistCM;
        if (ringCountButton.returnState()) {
            lidarTimer.reset();
            lidarSensed = true;
        }
        if (lidarTimer.milliseconds() > 2000) {
            lidarSensed = false;
        }
        return lidarSensed;
    }

    //Pattern setter for REV blinkin
    public void setPattern(String pattern_name) {
        pattern = RevBlinkinLedDriver.BlinkinPattern.valueOf(pattern_name);
        blinkin.setPattern(pattern);
        /*
        RAINBOW_RAINBOW_PALETTE,
        RAINBOW_PARTY_PALETTE,
        RAINBOW_OCEAN_PALETTE,
        RAINBOW_LAVA_PALETTE,
        RAINBOW_FOREST_PALETTE,
        RAINBOW_WITH_GLITTER,
        CONFETTI,
        SHOT_RED,
        SHOT_BLUE,
        SHOT_WHITE,
        SINELON_RAINBOW_PALETTE,
        SINELON_PARTY_PALETTE,
        SINELON_OCEAN_PALETTE,
        SINELON_LAVA_PALETTE,
        SINELON_FOREST_PALETTE,
        BEATS_PER_MINUTE_RAINBOW_PALETTE,
        BEATS_PER_MINUTE_PARTY_PALETTE,
        BEATS_PER_MINUTE_OCEAN_PALETTE,
        BEATS_PER_MINUTE_LAVA_PALETTE,
        BEATS_PER_MINUTE_FOREST_PALETTE,
        FIRE_MEDIUM,
        FIRE_LARGE,
        TWINKLES_RAINBOW_PALETTE,
        TWINKLES_PARTY_PALETTE,
        TWINKLES_OCEAN_PALETTE,
        TWINKLES_LAVA_PALETTE,
        TWINKLES_FOREST_PALETTE,
        COLOR_WAVES_RAINBOW_PALETTE,
        COLOR_WAVES_PARTY_PALETTE,
        COLOR_WAVES_OCEAN_PALETTE,
        COLOR_WAVES_LAVA_PALETTE,
        COLOR_WAVES_FOREST_PALETTE,
        LARSON_SCANNER_RED,
        LARSON_SCANNER_GRAY,
        LIGHT_CHASE_RED,
        LIGHT_CHASE_BLUE,
        LIGHT_CHASE_GRAY,
        HEARTBEAT_RED,
        HEARTBEAT_BLUE,
        HEARTBEAT_WHITE,
        HEARTBEAT_GRAY,
        BREATH_RED,
        BREATH_BLUE,
        BREATH_GRAY,
        STROBE_RED,
        STROBE_BLUE,
        STROBE_GOLD,
        STROBE_WHITE,
        CP1_END_TO_END_BLEND_TO_BLACK,
        CP1_LARSON_SCANNER,
        CP1_LIGHT_CHASE,
        CP1_HEARTBEAT_SLOW,
        CP1_HEARTBEAT_MEDIUM,
        CP1_HEARTBEAT_FAST,
        CP1_BREATH_SLOW,
        CP1_BREATH_FAST,
        CP1_SHOT,
        CP1_STROBE,
        CP2_END_TO_END_BLEND_TO_BLACK,
        CP2_LARSON_SCANNER,
        CP2_LIGHT_CHASE,
        CP2_HEARTBEAT_SLOW,
        CP2_HEARTBEAT_MEDIUM,
        CP2_HEARTBEAT_FAST,
        CP2_BREATH_SLOW,
        CP2_BREATH_FAST,
        CP2_SHOT,
        CP2_STROBE,
        CP1_2_SPARKLE_1_ON_2,
        CP1_2_SPARKLE_2_ON_1,
        CP1_2_COLOR_GRADIENT,
        CP1_2_BEATS_PER_MINUTE,
        CP1_2_END_TO_END_BLEND_1_TO_2,
        CP1_2_END_TO_END_BLEND,
        CP1_2_NO_BLENDING,
        CP1_2_TWINKLES,
        CP1_2_COLOR_WAVES,
        CP1_2_SINELON,
        HOT_PINK,
        DARK_RED,
        RED,
        RED_ORANGE,
        ORANGE,
        GOLD,
        YELLOW,
        LAWN_GREEN,
        LIME,
        DARK_GREEN,
        GREEN,
        BLUE_GREEN,
        AQUA,
        SKY_BLUE,
        DARK_BLUE,
        BLUE,
        BLUE_VIOLET,
        VIOLET,
        WHITE,
        GRAY,
        DARK_GRAY,
        BLACK;
        */
    }



}
