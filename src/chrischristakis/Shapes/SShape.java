package chrischristakis.Shapes;

import chrischristakis.Scene;

public class SShape extends Shape
{	
	public SShape(int x, int y) {
		super(x, y);
		shape = rot0;
		color.x = Scene.SColor.x;
		color.y = Scene.SColor.y;
		color.z = Scene.SColor.z;
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
	
	public int[][] rot0 = {{0,6,6},
			   			   {6,6,0},
						   {0,0,0}};
	public int[][] rot1 = {{0,6,0},
			   			   {0,6,6},
						   {0,0,6}};
	public int[][] rot2 = {{0,0,0},
			   			   {0,6,6},
						   {6,6,0}};
	public int[][] rot3 = {{6,0,0},
			   			   {6,6,0},
						   {0,6,0}};
	
}