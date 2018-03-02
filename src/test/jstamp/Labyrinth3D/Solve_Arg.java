package jstamp.Labyrinth3D;

import transactionLib.Queue;

public class Solve_Arg {
        Router routerPtr;
        Maze mazePtr;
        Queue pathVectorListPtr;

        public Solve_Arg(Router r,Maze m,Queue q)
        {
            routerPtr = r;
            mazePtr = m;
            pathVectorListPtr = q;
        }
    }

