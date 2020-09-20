package chrischristakis.Shapes;

import chrischristakis.Scene;

public class ZShape extends Shape
{

	public ZShape(int x, int y) {
		super(x, y);
		shape = rot0;
		color.x = Scene.ZColor.x;
		color.y = Scene.ZColor.y;
		color.z = Scene.ZColor.z;
	}

	@Override
	public void rotateCW() 
	{
		currentRotation = (currentRotation >= 3)? 0 : currentRotation + 1;
		switch(currentRotation)
		{
		case 0: shape = rot0; break;
		case 1: shape = rot1; break;
		case 2: shape = rot2; break;
		case 3: shape = rot3; break;
		default: shape = rot0; break;
		}
	}
	
	@Override
	public void rotateCCW()
	{
		currentRotation = (currentRotation < 0)? 3 : currentRotation - 1;
		switch(currentRotation)
		{
		case 0: shape = rot0; break;
		case 1: shape = rot1; break;
		case 2: shape = rot2; break;
		case 3: shape = rot3; break;
		default: shape = rot0; break;
		}
	}
	
	public int[][] rot0 = {{7,7,0},
			   			   {0,7,7},
						   {0,0,0}};
	public int[][] rot1 = {{0,0,7},
			   			   {0,7,7},
						   {0,7,0}};
	public int[][] rot2 = {{0,0,0},
			   			   {7,7,0},
						   {0,7,7}};
	public int[][] rot3 = {{0,7,0},
			   			   {7,7,0},
						   {7,0,0}};
	
}