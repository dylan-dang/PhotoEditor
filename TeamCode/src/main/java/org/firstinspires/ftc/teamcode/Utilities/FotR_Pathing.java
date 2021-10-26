package org.firstinspires.ftc.teamcode.Utilities;

import com.qualcomm.robotcore.util.Range;

import static org.firstinspires.ftc.teamcode.Utilities.MathFunctions.angleWrap;

public class FotR_Pathing {

    //Static variables so they can be accessed by outside OpModes (note that because of this, there can only be one active version of Pathing at a time
    public static double moveX;
    public static double moveY;
    public static double moveTurn;
    public static double distToTarget;

    public static void goToPositionOneSpeed(double worldX, double worldY, double worldAngleRad, double x, double y, double speed, double angle, boolean endPoint) {

        //Find the distance to our chosen target
        double distanceToTarget = Math.hypot(x-worldX, y-worldY);
        distToTarget = distanceToTarget;

        //Finds the absolute value of the angle to the target
        double absAngleToTarget = Math.atan2(y-worldY,x-worldX);
        //Finds the vector pointing towards our target
        double moveXPow = (-1*Math.cos(absAngleToTarget));
        double moveYPow = (-1*Math.sin(absAngleToTarget));

        //To slow us down as we get closer, raise a ratio value as we get closer to the target
        double ratio = distanceToTarget / 15;

        //Find an angle and turn power from our current rotation
        double relAngleToTarget = angleWrap(worldAngleRad - angle);
        double relTurnPow = -relAngleToTarget;

        //If we're going to park, make sure to slow down as we get closer
        if (endPoint) {
            moveX = Range.clip(moveXPow * ratio, -speed, speed);
            moveY = Range.clip(moveYPow * ratio, -speed, speed);

            if (Math.sqrt(Math.pow(moveX, 2) + Math.pow(moveY,2)) < 0.35) {
                moveX = moveXPow * 0.35;
                moveY = moveYPow * 0.35;
                //Limit the turn power so that it is a set maximum beyond a certain level, and scale down as you get closer
                moveTurn = Range.clip(relTurnPow/Math.toRadians(10), -1, 1) * speed;
            }
        }
        else {
            //Go at full power otherwise
            moveX = moveXPow * speed;
            moveY = moveYPow * speed;
            //Limit the turn power so that it is a set maximum beyond a certain level, and scale down as you get closer
            moveTurn = Range.clip(relTurnPow/Math.toRadians(30), -1, 1) * speed;
        }

    }

}
