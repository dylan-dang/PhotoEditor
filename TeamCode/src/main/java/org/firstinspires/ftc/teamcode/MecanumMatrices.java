package org.firstinspires.ftc.teamcode;

import org.apache.commons.math3.linear.*;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;

public class MecanumMatrices {
    private RealMatrix transformation;

    public MecanumMatrices(double wheelRadius, double a, double b) {
        b = 0.2079625;
        a = 0.13335;
        transformation = createRealMatrix(new double[][] {
                {1, -1, -(a + b)},
                {1,  1,  (a + b)},
                {1,  1, -(a + b)},
                {1, -1,  (a + b)}
        }).scalarMultiply(1/wheelRadius);
    }

    public double[] getComponentVelocities(double velocityX, double velocityY, double angularVelocity) {
        return transformation.preMultiply(new double[] {velocityX, velocityY, angularVelocity});
    }
}
