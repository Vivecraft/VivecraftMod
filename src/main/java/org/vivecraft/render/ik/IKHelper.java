package org.vivecraft.render.ik;

public class IKHelper
{
    public static IKInfo CalcIK_2D_TwoBoneAnalytic(boolean solvePosAngle2, double length1, double length2, double targetX, double targetY)
    {
        double d0 = 1.0E-4D;
        IKInfo ikinfo = new IKInfo();
        ikinfo.foundValidSolution = true;
        double d1 = targetX * targetX + targetY * targetY;
        double d3 = 2.0D * length1 * length2;
        double d2;
        double d8;

        if (d3 > 1.0E-4D)
        {
            d8 = (d1 - length1 * length1 - length2 * length2) / d3;

            if (d8 < -1.0D || d8 > 1.0D)
            {
                ikinfo.foundValidSolution = false;
            }

            d8 = Math.max(-1.0D, Math.min(1.0D, d8));
            ikinfo.angle2 = Math.acos(d8);

            if (!solvePosAngle2)
            {
                ikinfo.angle2 = -ikinfo.angle2;
            }

            d2 = Math.sin(ikinfo.angle2);
        }
        else
        {
            double d4 = (length1 + length2) * (length1 + length2);

            if (d1 < d4 - 1.0E-4D || d1 > d4 + 1.0E-4D)
            {
                ikinfo.foundValidSolution = false;
            }

            ikinfo.angle2 = 0.0D;
            d8 = 1.0D;
            d2 = 0.0D;
        }

        double d9 = length1 + length2 * d8;
        double d5 = length2 * d2;
        double d6 = targetY * d9 - targetX * d5;
        double d7 = targetX * d9 + targetY * d5;
        ikinfo.angle1 = Math.atan2(d6, d7);
        return ikinfo;
    }
}
