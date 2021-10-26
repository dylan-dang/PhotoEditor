package org.firstinspires.ftc.teamcode.Utilities;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Utilities.FotR_Controller;


public class StageMechanismPID {
    public int targetPosition = 0;
    public boolean isRetracted = false;
    private DcMotor stageReel = null;
    private int maxPosition = 4000;
    private double CountMultiplier = 50;
    private double Pgain = 0;
    private double Igain = 0;
    private double Dgain = 0;
    private double Fgain = 0;
    FotR_Controller stageCtrl = new FotR_Controller();

    public StageMechanismPID(DcMotor MyStageMotor, double P, double I, double D, double F) {
        stageReel = MyStageMotor;
        stageReel.setDirection(DcMotor.Direction.FORWARD);
        stageReel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        stageReel.setTargetPosition(0);
        stageReel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        Pgain = P;
        Igain = I;
        Dgain = D;
        Fgain = F;
    }

    public void MoveStagePID(double stickPosition, int currentPosition) {
        if (Math.abs(stickPosition) > 0.1) {
            targetPosition = targetPosition + (int) ((stickPosition) * CountMultiplier);
        }
        LimitStage();
        stageReel.setPower(stageCtrl.PID((targetPosition - currentPosition), Pgain, Igain, Dgain ));
        if (currentPosition <=0){isRetracted = true;}else{isRetracted = false;}
    }

    public void MoveStagePIDF(double stickPosition, int currentPosition) {
        if (Math.abs(stickPosition) > 0.1) {
            targetPosition = targetPosition + (int) ((stickPosition) * CountMultiplier);
        }
        LimitStage();
        stageReel.setPower(stageCtrl.PIDF((targetPosition - currentPosition), currentPosition, Pgain, Igain, Dgain, Fgain ));
        if (currentPosition <=0){isRetracted = true;}else{isRetracted = false;}
    }

    private void LimitStage(){
        if (targetPosition < 0) {
            targetPosition = 0;
        } else if (targetPosition > maxPosition) {
            targetPosition = maxPosition;
        }
    }

}
