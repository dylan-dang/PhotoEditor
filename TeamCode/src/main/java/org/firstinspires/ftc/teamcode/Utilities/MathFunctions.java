package org.firstinspires.ftc.teamcode.Utilities;

//Contains all random re-usable manth function for FotR
public class MathFunctions {
    //Method that makes sure the angle of a turn is not over 180, wrapping it around the axis of rotation
    public static double angleWrap (double angleRad) {
        while (angleRad < -Math.PI) {
            angleRad += 2 * Math.PI;
        }
        while (angleRad > Math.PI) {
            angleRad -= 2 * Math.PI;
        }
        return angleRad;
    }

}
