import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;

public class Scene 
{
	public int bgVao, bgVbo;
	public ShaderProgram blockShader, bgShader;

	QuadFactory qf;
	
	int offsetX, offsetY; //play area offset from 0,0
	int[][] grid; //0,0 is top left.
	final int gridRows = 20, gridCols = 10;
	int blockDim = 40;
	private boolean canLeft, canRight, canRotate, moveBlock;
	
	Block block;
	
	double timer; //Keeps track of when to update the game
	
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
		grid[19][0] = 1;
		grid[0][9] = 1;
	}
	
	public void render()
	{
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		//Play area
		bgShader.bind();
		glBindVertexArray(bgVao);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		
		qf.setColor(230, 62, 53);
		//Render placed blocks
		for(int y = 0; y < grid.length; y++)
			for(int x = 0; x < grid[y].length; x++)
				if(grid[y][x] != 0)
					qf.addQuad(x*blockDim + offsetX, (gridRows-1-y)*blockDim+offsetY, blockDim, blockDim);
		//Render active block
		for(int y = 0; y < block.shape.length; y++)
			for(int x = 0; x < block.shape[y].length; x++)
				if(block.shape[y][x] != 0)
					qf.addQuad((block.x+x)*blockDim+offsetX, (gridRows-1-block.y-y)*blockDim+offsetY, blockDim, blockDim);
		
		blockShader.bind();
		qf.flush();
	}
	
	public void update()
	{
		handleKeys();
			
		if(glfwGetTime() - timer >= 0.2) //game tick
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
			}
			// ----------------------------------------------
			
			timer = glfwGetTime();
		}
	}
	
	private void handleKeys()
	{
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

	private abstract class Block
	{
		int x, y; //location of topleft 'block'
		public int[][] shape = {{1,1},
				 				{1,1}};
		public int currentRotation = 0;
		
		public Block(int x, int y)
		{
			this.x=x; this.y=y;
		}
		
		abstract public void rotate();
	}
	
	private class TShape extends Block
	{

		public TShape(int x, int y) {
			super(x, y);
			shape = rot1;
		}

		@Override
		public void rotate() 
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
	
}
