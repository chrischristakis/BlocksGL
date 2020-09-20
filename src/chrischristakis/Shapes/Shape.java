package chrischristakis.Shapes;
import org.joml.Vector3f;

public abstract class Shape
{
		public int x; //location of topleft 'block'
		public int y;
		public int[][] shape = {{1,1},
				 				{1,1}};
		public int currentRotation = 0;
		public Vector3f color = new Vector3f(1,1,1);
		
		public Shape(int x, int y)
		{
			this.x=x; this.y=y;
		}
		
		abstract public void rotateCW();
		abstract public void rotateCCW();
		
		//Using 'wallkick' data using SRS system (https://tetris.wiki/Super_Rotation_System)
		//Each value describes an offset attempt from the center of the shape, 5 attempts per rotation scenario.
		public final int[][] JLSTZ_WALLKICK = {
			{0,0}, {-1,0}, {-1,1}, {0,-2}, {-1,-2}, //0->1
			{0,0},  {1,0}, {1,-1},  {0,2},   {1,2}, //1->2
			{0,0},  {1,0},  {1,1}, {0,-2},  {1,-2}, //2->3
			{0,0}, {-1,0},{-1,-1},  {0,2}, {-1,2}, //3->0			
		};
		
		public final int[][] I_WALLKICK = {
			{0,0}, {-2,0}, {1,0}, {-2,-1},  {1,2}, //0->1
			{0,0}, {-1,0}, {2,0},  {-1,2}, {2,-1}, //1->2
			{0,0},  {2,0},{-1,0},   {2,1},{-1,-2}, //2->3	
			{0,0},  {1,0},{-2,0},  {1,-2},{-2,-1}, //3->0		
		};
}
