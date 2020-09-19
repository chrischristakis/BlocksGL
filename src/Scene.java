import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Scene 
{
	
	//----- VARIABLES --------
	public int bgVao, bgVbo;
	public ShaderProgram blockShader, bgShader;

	QuadFactory qf;
	Shape block;
	
	int offsetX, offsetY; //play area offset from 0,0
	int[][] grid; //0,0 is top left.
	final int gridRows = 20, gridCols = 10;
	int blockDim = 40;
	
	private boolean canLeft, canRight, canRotate, moveBlock;

	double timer; //Keeps track of when to update the game
	private float gameTickInterval = 0.5f; //How fast each block falls
	
	private float outline;
	
	private Random random = new Random();
	
	public static final Vector3f TColor = new Vector3f( 236/255.0f, 61/255.0f,255/255.0f);
	public static final Vector3f IColor = new Vector3f( 79/255.0f, 255/255.0f,232/255.0f);
	public static final Vector3f SquareColor = new Vector3f( 255/255.0f, 246/255.0f,72/255.0f);
	public static final Vector3f LColor = new Vector3f( 255/255.0f, 165/255.0f,31/255.0f);
	public static final Vector3f JColor = new Vector3f( 77/255.0f, 97/255.0f, 255/255.0f);
	public static final Vector3f SColor = new Vector3f( 59/255.0f, 255/255.0f, 98/255.0f);
	public static final Vector3f ZColor = new Vector3f( 255/255.0f, 54/255.0f, 54/255.0f);
	//--------------------------
	
	public Scene()
	{	
		//Shader init
		blockShader = new ShaderProgram("block.vert", "block.frag");
		blockShader.bind();
		blockShader.setMat4f("mvp", new Matrix4f().ortho(0.0f, Main.WIDTH, 0.0f, Main.HEIGHT, 0.0f, 0.1f));
		
		bgShader = new ShaderProgram("bgShader.vert", "bgShader.frag");
		bgShader.bind();
		bgShader.setMat4f("mvp", new Matrix4f().ortho(0.0f, Main.WIDTH, 0.0f, Main.HEIGHT, 0.0f, 0.1f));
		
		//Other inits
		offsetX = Main.WIDTH/20;
		offsetY = (Main.HEIGHT-blockDim*gridRows)/2;
		initBackground(offsetX, offsetY, blockDim*gridCols, blockDim*gridRows);
		
		block = new TShape(0, 0);
		timer = glfwGetTime();
		
		qf = new QuadFactory(gridRows*gridCols);
		
		grid = new int[20][10];
		
		outline = blockDim/20;
	}
	
	public void render()
	{
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		//Play area
		bgShader.bind();
		glBindVertexArray(bgVao);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	
		//Render placed blocks
		for(int y = 0; y < grid.length; y++)
			for(int x = 0; x < grid[y].length; x++)
				if(grid[y][x] != 0)
				{
					if(grid[y][x] == 1) qf.setColor(TColor);
					if(grid[y][x] == 2) qf.setColor(IColor);
					if(grid[y][x] == 3) qf.setColor(SquareColor);
					if(grid[y][x] == 4) qf.setColor(LColor);
					if(grid[y][x] == 5) qf.setColor(JColor);
					if(grid[y][x] == 6) qf.setColor(SColor);
					if(grid[y][x] == 7) qf.setColor(ZColor);
					
					qf.addQuad(x*blockDim + offsetX + (int)outline, (gridRows-1-y)*blockDim+offsetY + (int)outline, blockDim - (int)outline*2, blockDim - (int)outline*2);
				}
		//Render active block
		qf.setColor(block.color);
		for(int y = 0; y < block.shape.length; y++)
			for(int x = 0; x < block.shape[y].length; x++)
				if(block.shape[y][x] != 0)
					qf.addQuad((block.x+x)*blockDim+offsetX + (int)outline, (gridRows-1-block.y-y)*blockDim+offsetY + (int)outline, blockDim - (int)outline*2, blockDim - (int)outline*2);
		
		blockShader.bind();
		qf.flush();
	}
	
	public void update()
	{
		handleKeys();
			
		if(glfwGetTime() - timer >= gameTickInterval) //game tick
		{
			// ----- Check if block has landed ontop of something -----
			moveBlock = true;
			outerloop:
			for(int y = 0; y < block.shape.length; y++)
				for(int x = 0; x < block.shape[y].length; x++)
					if(block.shape[y][x] != 0) //We only care about the part of the shape that is composed of solid blocks, not 0's
					{
						if(block.y+y+1 >= gridRows) //block has landed at the bottom
						{
							moveBlock = false;
							break outerloop;
						}
						if(grid[block.y+y+1][block.x+x] != 0) //Block had landed on another block
						{
							moveBlock = false;
							break outerloop;
						}
					}
			
			if(moveBlock) //Block has not landed
				block.y++;
			else //if Block has landed
			{
				//Duplicate shape onto the grid.
				for(int y = 0; y < block.shape.length; y++)
					for(int x = 0; x < block.shape[y].length; x++)
						if(block.shape[y][x] != 0) //Incase some 0's that are part of the shape are clipping out of the grid.
							grid[y+block.y][x+block.x] = block.shape[y][x];
				
				block.x = 0; block.y = 0; //Reset position of shape to the top.
				
				//Select new block
				switch(random.nextInt(7))
				{
					case 0: block = new IShape(block.x,block.y); break;
					case 1: block = new LShape(block.x,block.y); break;
					case 2: block = new JShape(block.x,block.y); break;
					case 3: block = new TShape(block.x,block.y); break;
					case 4: block = new SShape(block.x,block.y); break;
					case 5: block = new ZShape(block.x,block.y); break;
					case 6: block = new SquareShape(block.x,block.y); break;
					default: block = new SquareShape(block.x,block.y); break;
				}
				
			}
			// ----------------------------------------------
			
			timer = glfwGetTime();
		}
	}
	
	private void handleKeys()
	{
		//SPEED
		if(KeyInput.isPressed(GLFW_KEY_S))
			gameTickInterval = 0.1f;
		else
			gameTickInterval = 0.5f;
		
		//RIGHT
		moveBlock = true;
		if(KeyInput.isPressed(GLFW_KEY_A))
		{
			if(canLeft)
			{
				canLeft = false; //So input is handled one keystroke at a time.
			
				//Make sure no blocks in the shape are exiting the grid.
				outerloop:
				for(int y = 0; y < block.shape.length; y++)
					for(int x = 0; x < block.shape[y].length; x++)
						if(block.shape[y][x] != 0)
						{
							if(block.x+x-1 < 0)
							{
								moveBlock = false;
								break outerloop;
							}
							if(grid[block.y+y][block.x+x-1] != 0)
							{
								moveBlock = false;
								break outerloop;
							}
						}
				
				if(moveBlock)
					block.x--;
			}
		}
		else
			canLeft = true;
		
		//RIGHT
		moveBlock = true;
		if(KeyInput.isPressed(GLFW_KEY_D))
		{
			if(canRight)
			{
				canRight = false; //So input is handled one keystroke at a time.
			
				//Make sure no blocks in the shape are exiting the grid.
				outerloop:
				for(int y = 0; y < block.shape.length; y++)
					for(int x = 0; x < block.shape[y].length; x++)
						if(block.shape[y][x] != 0)
						{
							if(block.x+x+1 >= gridCols)
							{
								moveBlock = false;
								break outerloop;
							}
							if(grid[block.y+y][block.x+x+1] != 0)
							{
								moveBlock = false;
								break outerloop;
							}
						}
				
				if(moveBlock)
					block.x++;
			}
		}
		else
			canRight = true;
		
		//ROTATE
		if(KeyInput.isPressed(GLFW_KEY_W))
		{
			if(canRotate)
			{
				canRotate = false;
				block.rotateCW();
				
				if(block instanceof SquareShape) //Squares will never need to wallkick.
					return;
				
				int[][] kickArr;
				if(block instanceof IShape)
					kickArr = block.I_WALLKICK;
				else
					kickArr = block.JLSTZ_WALLKICK;

				//WALLKICK POSSIBILITIES LOOP
				boolean collided = false;
				for(int i = block.currentRotation*5; i < block.currentRotation*5 + 5; i++) //Try all 5 offset possbilities after the rotation
				{
					collided = false;
					for(int y = 0; y < block.shape.length; y++)
						for(int x = 0; x < block.shape[y].length; x++)
							if(block.shape[y][x] != 0)
							{	
								//kickArr[i][0] is X offset, kickArr[i][1] is Y offset
								
								//Check if any blocks are sticking out of the grid first
								if(block.x+x+kickArr[i][0] < 0 || block.x+x+kickArr[i][0] >= gridCols
								|| block.y+y+kickArr[i][1] < 0 || block.y+y+kickArr[i][1] >= gridRows) 
									collided = true;
								else if(grid[block.y+y+kickArr[i][1]][block.x+x+kickArr[i][0]] != 0)
									collided = true;
							}
					
					if(!collided) //If one possbility works, then use it and permit the rotation.
					{
						block.x += kickArr[i][0];
						block.y += kickArr[i][1];
						break;
					}
				}
				if(collided) //If all possibilities end up in a collision, don't permit the rotation.
					block.rotateCCW();
			}
		}
		else
			canRotate = true;
	}
	
	public void initBackground(int x, int y, int width, int height) //The playground area
	{
		float verts[] = {
			x, y,
			x + width, y,
			x, y + height,
			x+width, y+height
		};
		
		bgVao = glGenVertexArrays();
		bgVbo = glGenBuffers();
		glBindVertexArray(bgVao);
		glBindBuffer(GL_ARRAY_BUFFER, bgVbo);
		glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(verts), GL_STATIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);
	}

	private abstract class Shape
	{
		int x, y; //location of topleft 'block'
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
	
	private class TShape extends Shape
	{		
		public TShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = TColor.x;
			color.y = TColor.y;
			color.z = TColor.z;
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
		
		public int[][] rot0 = {{0,1,0},
				   			   {1,1,1},
							   {0,0,0}};
		public int[][] rot1 = {{0,1,0},
				   			   {0,1,1},
							   {0,1,0}};
		public int[][] rot2 = {{0,0,0},
				   			   {1,1,1},
							   {0,1,0}};
		public int[][] rot3 = {{0,1,0},
				   			   {1,1,0},
							   {0,1,0}};
		
	}
	
	private class IShape extends Shape
	{	
		public IShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = IColor.x;
			color.y = IColor.y;
			color.z = IColor.z;
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
		
		public int[][] rot0 = {{0,0,0,0},
				   			   {2,2,2,2},
							   {0,0,0,0},
							   {0,0,0,0}};
		public int[][] rot1 = {{0,0,2,0},
				   			   {0,0,2,0},
							   {0,0,2,0},
							   {0,0,2,0}};
		public int[][] rot2 = {{0,0,0,0},
				   			   {0,0,0,0},
							   {2,2,2,2},
							   {0,0,0,0}};
		public int[][] rot3 = {{0,2,0,0},
				   			   {0,2,0,0},
							   {0,2,0,0},
							   {0,2,0,0}};
		
	}
	
	private class SquareShape extends Shape
	{
		public SquareShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = SquareColor.x;
			color.y = SquareColor.y;
			color.z = SquareColor.z;
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
	
	private class LShape extends Shape
	{
		public LShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = LColor.x;
			color.y = LColor.y;
			color.z = LColor.z;
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
		
		public int[][] rot0 = {{0,0,4},
				   			   {4,4,4},
							   {0,0,0}};
		public int[][] rot1 = {{0,4,0},
				   			   {0,4,0},
							   {0,4,4}};
		public int[][] rot2 = {{0,0,0},
				   			   {4,4,4},
							   {4,0,0}};
		public int[][] rot3 = {{4,4,0},
				   			   {0,4,0},
							   {0,4,0}};
		
	}
	
	private class JShape extends Shape
	{
		public JShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = JColor.x;
			color.y = JColor.y;
			color.z = JColor.z;
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
		
		public int[][] rot0 = {{5,0,0},
				   			   {5,5,5},
							   {0,0,0}};
		public int[][] rot1 = {{0,5,5},
				   			   {0,5,0},
							   {0,5,0}};
		public int[][] rot2 = {{0,0,0},
				   			   {5,5,5},
							   {0,0,5}};
		public int[][] rot3 = {{0,5,0},
				   			   {0,5,0},
							   {5,5,0}};
		
	}
	
	private class SShape extends Shape
	{	
		public SShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = SColor.x;
			color.y = SColor.y;
			color.z = SColor.z;
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
	
	private class ZShape extends Shape
	{

		public ZShape(int x, int y) {
			super(x, y);
			shape = rot0;
			color.x = ZColor.x;
			color.y = ZColor.y;
			color.z = ZColor.z;
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
	
}
