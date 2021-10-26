package org.firstinspires.ftc.teamcode.Utilities;


import com.qualcomm.robotcore.util.Range;

/**
 * Created by Jack on 11/13/2017.
 */

//Class that holds all of the PID variations
public class FotR_Controller {

    private double Kp = 0;
    private double Ki = 0;
    private double Kd = 0;
    private double Kf = 0;
    private double Op = 0;
    private double Oi = 0;
    private double Od = 0;
    private double Ok = 0;
    public double Error = 0;
    public double Sum_Error = 0;
    private double Last_Error = 0;

    //Standard basic PID
    public double PID(double e, double p, double i, double d) {
        Error = e;
        Kp = p;
        Ki = i;
        Kd = d;

        Op = Kp * Error;
        Oi = Ki * Sum_Error;
        Sum_Error = Error + Sum_Error;
        Od = Kd * (Error - Last_Error);
        Last_Error = Error;
        return (Op + Oi + Od);
    }

    //PID with a correction F factor added
    public double PIDF(double e, double position, double p, double i, double d, double f) {
        Error = e;
        Kp = p;
        Ki = i;
        Kd = d;
        Kf = f;

        Op = Kp * Error;
        Oi = Ki * Sum_Error;
        Sum_Error = Error + Sum_Error;
        Od = Kd * (Error - Last_Error);
        Last_Error = Error;
        Ok = Kf * position;

        return (Op + Oi + Od + Ok);
    }

    //Essentially unused, just PID with a few extra variables to help me remember
    public double RunToPosition (double cpos, double dpos, double p, double i, double d) {
        return PID((cpos - dpos), p, i, d);
    }

    //PID version that only applies each of P, I, and D when under a certain error value
    public double limitedPID(double e, double maxP, double i, double d, double p_break, double i_break, double d_break) {
        double tempI = i;
        double tempD = d;
        double p;
        double tempE = e;

        tempE = Range.clip(e/Math.abs(p_break), -1, 1);

        p = maxP;

        //calculate appropriate i gain value. value is fixed below IBreak and
        //zero above IBreak
        if (Math.abs(e)>i_break){
            tempI = 0.0;
            //reset PID Error sum so it doesn't keep accumulating outside of IBreak range
            Sum_Error = 0;
        }
        else{
            tempI = i;
        }
        if (Math.abs(e) > d_break) {
            tempD = 0.0;
        }
        else {
            tempD = d;
        }

        return PID(tempE, p, tempI, tempD);
    }

}
