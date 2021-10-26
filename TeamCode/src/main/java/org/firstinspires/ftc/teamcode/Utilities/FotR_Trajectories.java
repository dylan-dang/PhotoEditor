package org.firstinspires.ftc.teamcode.Utilities;

public class FotR_Trajectories {

    //All distances in Inches
    private double gHeight;
    private double iHeight;
    private final double g = 386.088; //Accel by gravity in in/s^2

    public FotR_Trajectories (double goalHeight_In, double deckHeight_In) {
        gHeight = goalHeight_In;
        iHeight = deckHeight_In;
    }

    //Trajectory equation that finds the angle of an arc needed so that with a specific velocity and distance the apex of the arc matches the goal height
    public double findAngleFixedVelocity (double velocity, double distanceToTarget) {
        double angle = 0;
        double v = velocity;

        double b = distanceToTarget;

        double a = -(g*Math.pow(b,2)) / (2*Math.pow(v,2));

        double c = iHeight - gHeight + a;

        double tanAngle = (-b + Math.sqrt(Math.pow(b,2) - 4 * a * c)) / (2*a);

        angle = Math.atan(tanAngle);

        return angle;
    }

    //Needed when changing which goal position is being used.
    public void setGoalHeight (double goalHeight) {
        gHeight = goalHeight;
    }

    //Converter method, equation found via testing and spreadsheet
    public double ringToMotorSpeed (double ringFtPerSec) {
        double value = ringFtPerSec;

        value = /*78.505682*value + 200 + 0*/98.588*value-226.83;

        return value;
    }

    /*public double wheelInitSpeed (double mBall, double iBall, double iWheel, double vFinal, double rB, double rS) {
        double kV = rS * Math.PI;
        double wsi;

        wsi = Math.pow((vFinal / kV), 2) + (mBall * Math.pow(vFinal,2) + iBall * Math.pow((vFinal * rS) / (kV * 2 * rB), 2)) / iWheel;

        return Math.sqrt(wsi);
    }

    public double wheelInitSpeed (double vFinal) {
        return wheelInitSpeed(0.002056, 0.001952, 0.00294, vFinal, 0.208, 0.158);
    }*/

}
