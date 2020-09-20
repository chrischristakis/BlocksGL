package chrischristakis.Shapes;

import chrischristakis.Scene;

public class SquareShape extends Shape
{
	public SquareShape(int x, int y) {
		super(x, y);
		shape = rot0;
		color.x = Scene.SquareColor.x;
		color.y = Scene.SquareColor.y;
		color.z = Scene.SquareColor.z;
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
	
	public int[][] rot0 = {{3,3},
			   			   {3,3}};
	public int[][] rot1 = {{3,3},
						   {3,3}};
	public int[][] rot2 = {{3,3},
						   {3,3}};
	public int[][] rot3 = {{3,3},
						   {3,3}};
	
}
