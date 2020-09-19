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
		grid[19][1] = 1;
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
			moveBlock = true; //assume the block can move initially. Innocent until proven guilty right
			
			int firstBlockY = 0; //This variable gives the index of the first row with a block in a shape.
			outerloop:
			for(int shapeY = block.shape.length-1; shapeY >= 0; shapeY--)
				for(int shapeX = 0; shapeX < block.shape[shapeY].length; shapeX++)
					if(block.shape[shapeY][shapeX] != 0)
					{
						firstBlockY = shapeY;
						break outerloop;
					}
			
			if(block.y+firstBlockY == gridRows-1) //If block block reaches the bottom
			{
				moveBlock = false;
			}
			else //The block has not reached the bottom.
			{
				for(int shapeY = 0; shapeY < firstBlockY+1; shapeY++)
					for(int shapeX = 0; shapeX < block.shape[shapeY].length; shapeX++)
						if(grid[block.y+shapeY+1][block.x+shapeX] != 0 && block.shape[shapeY][shapeX] != 0)
							moveBlock = false;
			}
			
			if(moveBlock)
				block.y++;
			else //if the block cannot move, then it must be placed in position.
			{
				//Loop through the shape and put it into the grid.
				for(int y = 0; y < block.shape.length; y++)
					for(int x = 0; x < block.shape[y].length; x++)
						if(block.shape[y][x] != 0)
							grid[block.y+y][block.x+x] = 1;
				block.x = 0;
				block.y = 0;
			}
			
			timer = glfwGetTime();
		}
	}
	
	private void handleKeys()
	{
		moveBlock = true;
		//Key input / TODO: Make a cooldown so they can do one move per key press, also handle horizontal collision. 
		if(KeyInput.isPressed(GLFW_KEY_A)) //LEFT
		{
			if(canLeft)
			{
				for(int y = 0; y < block.shape.length; y++)
					for(int x = 0; x < block.shape[y].length; x++)
						if(block.shape[y][x] != 0 && grid[block.y+y][Math.max(0, block.x+x - 1)] != 0) //If the current shape element is a valid block, and space to left of current block isnt 0, dont move.
							moveBlock = false;
					
				if(moveBlock)
					block.x=Math.max(0, (block.x)-1);
				canLeft = false;
			}
		}
		else
			canLeft = true;
		
		moveBlock = true;
		if(KeyInput.isPressed(GLFW_KEY_D)) //RIGHT
		{
			if(canRight)
			{
				for(int y = 0; y < block.shape.length; y++)
					for(int x = 0; x < block.shape[y].length; x++)
						if(block.shape[y][x] != 0 && grid[block.y+y][Math.min(gridCols-block.shape[0].length, block.x+x + 1)] != 0)
							moveBlock = false;
				if(moveBlock)
					block.x=Math.min(gridCols-block.shape[0].length, block.x+1);
				canRight = false;
			}
		}
		else
			canRight = true;
		
		if(KeyInput.isPressed(GLFW_KEY_W)) //ROTATE
		{
			if(canRotate)
			{
				block.rotate();
				canRotate = false;
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
			shape = rot0;
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
