package org.firstinspires.ftc.teamcode.Odometry;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.TeamCode_2021.FotR_Ringbearer_Hardware;

import java.io.File;

public class FotR_OdoGlobalPosition implements Runnable{
    //Odometry wheels
    private DcMotor A1Encoder, A2Encoder, A3Encoder;
    private BNO055IMU imu;

    //Thead run condition
    private boolean isRunning = true;

    public double getA1Pos() {
        return A1Pos;
    }

    public double getA2Pos() {
        return A2Pos;
    }

    public double getA3Pos() {
        return A3Pos;
    }

    //Position variables used for storage and calculations
    double A1Pos = 0, A2Pos = 0, A3Pos = 0;
    private double robotX, robotY, angleRad, fieldX, fieldY;
    private double startX, startY, startAngle;
    private double lastA1, lastA2, lastA3, lastAngleRad;
    double avg = 0;
    double k1, k2, k3;
    File K1 = AppUtil.getInstance().getSettingsFile("K1.txt");
    File K2 = AppUtil.getInstance().getSettingsFile("K2.txt");
    File K3 = AppUtil.getInstance().getSettingsFile("K3.txt");

    //Algorithm constants
    // 57.26016374;  //encoder counts per deg robot rotation
    private double linearMMConversion = 68.6209102 / 1.020175;  //encoder counts per mm * calibration


    //Sleep time interval (milliseconds) for the position update thread
    private int sleepTime;


    /**
     * Constructor for GlobalCoordinatePosition Thread
     * @param verticalEncoderLeft left odometry encoder, facing the vertical direction
     * @param verticalEncoderRight right odometry encoder, facing the vertical direction
     * @param horizontalEncoder horizontal odometry encoder, perpendicular to the other two odometry encoder wheels
     * @param threadSleepDelay delay in milliseconds for the GlobalPositionUpdate thread (50-75 milliseconds is suggested)
     */

    public FotR_OdoGlobalPosition(DcMotor verticalEncoderRight, DcMotor verticalEncoderLeft, DcMotor horizontalEncoder /*BNO055IMU gyro*/, int threadSleepDelay){
        this.A1Encoder = verticalEncoderRight;
        this.A2Encoder = verticalEncoderLeft;
        this.A3Encoder = horizontalEncoder;
        sleepTime = threadSleepDelay;

        robotX = 0;
        robotY = 0;
        angleRad = 0;
        fieldX = 0;
        fieldY = 0;
        lastA1 = 0;
        lastA2 = 0;
        lastA3 = 0;
        lastAngleRad = angleRad;

        //Call in correction constants
        k1 = Double.parseDouble(ReadWriteFile.readFile(K1).trim());
        k2 = Double.parseDouble(ReadWriteFile.readFile(K2).trim());
        k3 = Double.parseDouble(ReadWriteFile.readFile(K3).trim());

    }

    /**
     * Updates the global (x, y, theta) coordinate position of the robot using the odometry encoders
     */
    public void globalCoordinatePositionUpdate(){

        //Call in correction by finding the average value of the three encoders
        avg = (A1Pos + A2Pos + A3Pos) / 3;

        //Corrects the encoder values to account for the rolling radius
        A1Pos = A1Pos - (k1*avg);
        A2Pos = A2Pos - (k2*avg);
        A3Pos = A3Pos - (k3*avg);

        //Finds out how much we've moved since the last clock cycle
        double deltaA1 = A1Pos - lastA1;
        double deltaA2 = A2Pos - lastA2;
        double deltaA3 = A3Pos - lastA3;

        //Uses two lines of an inverted matrix to calculate how much we've moved in X and Y terms based on the readings
        //The constant values come from an inverted matrix equation found via a spreadsheet
        robotX = ((deltaA1 * -0.57735 + deltaA2 * 0 + deltaA3 * 0.57735) / linearMMConversion);
        robotY = ((deltaA1 * 0.333333 + deltaA2 * -0.666667 + deltaA3 * 0.333333) / linearMMConversion);

        //Finds our angle by going for the average between the last and the current, which helps deal with acceleration
        double avgAngleRad = (angleRad + lastAngleRad) / 2;

        //Uses the rotation to translate the robot vector into a field vector, which is summed up with all previous vectors for addtional accuracy
        fieldX += (robotX * Math.cos(avgAngleRad)) - (robotY * Math.sin(avgAngleRad));
        fieldY += (robotY * Math.cos(avgAngleRad)) + (robotX * Math.sin(avgAngleRad));

        //Old code to keep track for the moment
        /*robotX = ((A1Pos * -0.57735 + A2Pos * 0 + A3Pos * 0.57735) / linearMMConversion) + startX;
        robotY = ((A1Pos * 0.333333 + A2Pos * -0.666667 + A3Pos * 0.333333) / linearMMConversion) + startY;

        fieldX = (robotX * Math.cos(angleRad)) - (robotY * Math.sin(angleRad));
        fieldY = (robotY * Math.cos(angleRad)) + (robotX * Math.sin(angleRad));*/

        //Save the current positions for the next cycle
        lastA1 = A1Pos;
        lastA2 = A2Pos;
        lastA3 = A3Pos;
        lastAngleRad = angleRad;

    }

    //Needed to pass encoder values from the Hardware
    public void updateSensors(double A1, double A2, double A3, double AngleRadians) {
        A1Pos = -A1;
        A2Pos = A2;
        A3Pos = -A3;
        angleRad = AngleRadians + startAngle;
    }

    /**
     * Returns the robot's global x coordinate
     * @return global x coordinate (Reported in Inches)
     */
    public double returnXCoordinate(){ return (robotX /25.4); }

    /**
     * Returns the robot's global y coordinate
     * @return global y coordinate (Reported in Inches)
     */
    public double returnYCoordinate(){ return (robotY /25.4); }

    //X and Y are stored in mm here because that's what I started in, but converted into inches for sake of ease
    public double returnFieldX() { return (fieldX/25.4);}

    public double returnFieldY() {return (fieldY/25.4);}

    //Function to set a field start if we're not starting on (0,0)
    public void setFieldStart(double xIn, double yIn, double rotDeg) {
        startX = xIn * 25.4;
        startY = yIn * 25.4;
        fieldX = startX;
        fieldY = startY;
        startAngle = Math.toRadians(rotDeg);
    }

    /**
     * Returns the robot's global orientation
     * @return global orientation, in radians
     */
    public double returnOrientation(){ return angleRad % (2*Math.PI); }

    /**
     * Stops the position update thread
     */
    public void stop(){ isRunning = false; }

    /**
     * Runs the thread
     */
    @Override
    public void run() {
        while(isRunning) {
            globalCoordinatePositionUpdate();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
