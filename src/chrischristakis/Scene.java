package chrischristakis;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import chrischristakis.Shapes.IShape;
import chrischristakis.Shapes.JShape;
import chrischristakis.Shapes.LShape;
import chrischristakis.Shapes.SShape;
import chrischristakis.Shapes.Shape;
import chrischristakis.Shapes.SquareShape;
import chrischristakis.Shapes.TShape;
import chrischristakis.Shapes.ZShape;

public class Scene 
{
	//----- VARIABLES --------
	public int bgVao, bgVbo;
	public ShaderProgram blockShader, bgShader;

	QuadFactory qf;
	QuadFactory backgroundf;
	Shape block;
	
	int offsetX, offsetY; //play area offset from 0,0
	int[][] grid; //0,0 is top left.
	final int gridRows = 20, gridCols = 10;
	int blockDim = 40;
	int queueOffsetX, queueOffsetY, queueBlockDim;
	
	private boolean canLeft, canRight, canRotate, moveBlock, canMode;
	
	double timer; //Keeps track of when to update the game
	private float gameTickInterval = 0.5f; //How fast each block falls
	
	private float outline;
	private boolean quadMode = false;
	private ArrayList<Shape> shapeBag = new ArrayList<Shape>();
	private ArrayList<Shape> shapeQueue = new ArrayList<Shape>();
	
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
		
		timer = glfwGetTime();
		
		qf = new QuadFactory(gridRows*gridCols);
		backgroundf = new QuadFactory(2);
		
		grid = new int[20][10];
		outline = blockDim/20;
		queueBlockDim = blockDim-(int)(blockDim/4.5);
		queueOffsetY = Main.HEIGHT-(queueBlockDim*16)-offsetY;
		queueOffsetX =offsetX+blockDim*gridCols+queueBlockDim;
		
		//Populate shapes
		refillShapes();
		for(int i = 0; i < 4; i++)
			shapeQueue.add(shapeBag.remove(random.nextInt(shapeBag.size())));
		block = nextShape();
	}
	
	private Shape nextShape()
	{
		if(shapeBag.size() <= 0) refillShapes();
		shapeQueue.add(shapeBag.remove(random.nextInt(shapeBag.size())));
		return shapeQueue.remove(0);
	}
	
	private void refillShapes()
	{
		shapeBag.add(new IShape(0,0));
		shapeBag.add(new LShape(0,0));
		shapeBag.add(new SShape(0,0));
		shapeBag.add(new JShape(0,0));
		shapeBag.add(new ZShape(0,0));
		shapeBag.add(new TShape(0,0));
		shapeBag.add(new SquareShape(0,0));
	}
	
	public void render()
	{
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		//Play area
		bgShader.bind();
		backgroundf.setColor(0, 0.03f, 0.14f);
		backgroundf.addQuad(offsetX, offsetY, blockDim*gridCols, blockDim*gridRows);
		backgroundf.addQuad(queueOffsetX, queueOffsetY, queueBlockDim * 6, queueBlockDim * 16);
		backgroundf.flush();
		
		if(quadMode)
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	
		//Render queued shapes
		
		for(int i = 0; i < shapeQueue.size(); i++)
			for(int y = 0; y < shapeQueue.get(i).shape.length; y++)
				for(int x = 0; x < shapeQueue.get(i).shape[y].length; x++)
					if(shapeQueue.get(i).shape[y][x] != 0)
					{
						qf.setColor(shapeQueue.get(i).color);
						qf.addQuad(x*queueBlockDim + (int)outline + queueOffsetX + queueBlockDim, (gridRows-1-y)*queueBlockDim + (int)outline - 4*queueBlockDim*i + queueOffsetY-queueBlockDim*5, queueBlockDim - (int)outline*2, queueBlockDim - (int)outline*2);
					}
		
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
		//Render active shape
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
						{
							grid[y+block.y][x+block.x] = block.shape[y][x];
							if(y+block.y <= 0) //GAME OVER!
								System.exit(0);
						}
				
			
				boolean emptySpace;
				ArrayList<Integer> rowsRemoved = new ArrayList<Integer>();
				//Check if a row of blocks may be removed.
				for(int y = 0; y < gridRows; y++)
				{
					emptySpace = false;
					for(int x = 0; x < gridCols; x++)
						if(grid[y][x] == 0) //If any space in a row is empty
							emptySpace = true;
					
					if(!emptySpace) //No empty spaces in a row? Remove it!
					{
						rowsRemoved.add(y);
						for(int x = 0; x < gridCols; x++)
							grid[y][x] = 0;
					}
				}
				
				//Shift all blocks to accont for the now empty rows. Tetris gravity!
				for(int i = 0; i < rowsRemoved.size(); i++)
				{
					int currentRow = rowsRemoved.get(i);
					for(int y = currentRow; y >= 0; y--)
						for(int x = 0; x < gridCols; x++)
							grid[y][x] = grid[Math.max(0,y-1)][x];
				}		
				
				//Select new block
				block = nextShape();
				block.x = 0; block.y = 0; //Reset position of shape to the top.
			}
			// ----------------------------------------------
			
			timer = glfwGetTime();
		}
	}
	
	private void handleKeys()
	{
		//QUAD MODE!
		if(KeyInput.isPressed(GLFW_KEY_M))
		{
			if(canMode)
			{
				quadMode = !quadMode;
				canMode = false;
			}
		}
		else
			canMode = true;
		
		//SPEED
		if(KeyInput.isPressed(GLFW_KEY_S))
			gameTickInterval = 0.08f;
		else
			gameTickInterval = 0.6f;
		
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
	
}
