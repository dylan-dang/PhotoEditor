package org.firstinspires.ftc.teamcode.Utilities;


public class FotR_Mecanum {

    double[] wheels = new double[4];
    double[] pwheels = new double[4];
    double lx = 0;
    double ly = 0;
    double vX = 0;
    double vY = 0;
    double r = 0;
    double turn = 0;
    double lTotal = 0;
    int wPrime = 0;

    //Units in Meters
    //lx is half of left/right track width in meters
    //ly is half of fore/aft track width in meters
    //r is radius of wheel in meters
    public FotR_Mecanum (double width, double height, double radius) {
        lx = width;
        ly = height;
        r = radius;
    }
    public void WheelCalc (double moveX, double moveY, double rotate, double moveVar, double rotVar) {
        vX = moveX;
        vY = moveY;
        turn = rotate;
        lTotal = lx + ly;
        double power = ((moveVar * Math.sqrt(Math.pow(moveX,2) + Math.pow(moveY,2))) + (rotVar * Math.abs(rotate)));

        wheels[0] = ((1 / r) * (vX - vY + (lTotal * turn)));
        wheels[1] = ((1 / r) * (vX + vY - (lTotal * turn)));
        wheels[2] = ((1 / r) * (vX + vY + (lTotal * turn)));
        wheels[3] = ((1 / r) * (vX - vY - (lTotal * turn)));
        sortArray();
        for (int c = 0; c < wheels.length; c++) {
            pwheels[c] = (wheels[c] / Math.abs(wheels[wPrime])) * Math.abs(power);
        }
        }
    public void WheelCalc (double moveX, double moveY, double rotate, double power) {
        vX = moveX;
        vY = moveY;
        turn = rotate;
        lTotal = lx + ly;
        power = power * ((Math.sqrt(Math.pow(moveX,2) + Math.pow(moveY,2))) + (Math.abs(rotate)));

        wheels[0] = ((1 / r) * (vX - vY + (lTotal * turn)));
        wheels[1] = ((1 / r) * (vX + vY - (lTotal * turn)));
        wheels[2] = ((1 / r) * (vX + vY + (lTotal * turn)));
        wheels[3] = ((1 / r) * (vX - vY - (lTotal * turn)));
        sortArray();
        for (int c = 0; c < wheels.length; c++) {
            pwheels[c] = (wheels[c] / Math.abs(wheels[wPrime])) * Math.abs(power);
        }
    }
    public void WheelCalcField (double moveX, double moveY, double rotate, double moveVar, double rotVar, double fieldGyro) {
        double rX = (moveX * Math.cos(fieldGyro)) + (moveY * Math.sin(fieldGyro));
        double rY = (moveY * Math.cos(fieldGyro)) - (moveX * Math.sin(fieldGyro));
        WheelCalc(rX,rY,rotate,moveVar,rotVar);
    }

    public void WheelCalcField (double moveX, double moveY, double rotate, double power, double fieldGyro) {
        double rX = (moveX * Math.cos(fieldGyro)) + (moveY * Math.sin(fieldGyro));
        double rY = (moveY * Math.cos(fieldGyro)) - (moveX * Math.sin(fieldGyro));
        WheelCalc(rX,rY,rotate,power);
    }

    public double getPower (int wheel) {
    if (wheels[wheel] != 0) {
        return pwheels[wheel];}
        else return 0;
    }

    //Not really an array sort, deprecated name. Really just finds the index of the largest value in an array
    private void sortArray() {
        double highest = 0;
        for (int c = 0; c < wheels.length; c++) {
            if (Math.abs(wheels[c]) > highest) {
                highest = Math.abs(wheels[c]);
                wPrime = c;
            }
        }
    }
    }