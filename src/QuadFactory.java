import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class QuadFactory 
{
	
	private FloatBuffer vertices;
	private Vector3f color = new Vector3f(1.0f);
	
	private int vao, vbo;
	private int vertCount = 0;
	
	public QuadFactory(int numOfQuads) //a vertex will have a 2 position floats, and 3 color floats. Quad has 6 vertices, (2+3)*4 floats per quad.
	{
		vertices = MemoryUtil.memAllocFloat( ((2+3)*6)*numOfQuads );
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices.capacity()*Float.BYTES, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 5*4, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 5*4, 2*4);
		glEnableVertexAttribArray(1);
	}
	
	public void flush()
	{
		if(vertCount == 0) return;
		
		vertices.flip();//read
		
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
		
		glDrawArrays(GL_TRIANGLES, 0, vertCount);
		
		vertices.flip();//write
		vertices.clear();
		vertCount = 0;
	}
	
	public void addQuad(int x, int y, int width, int height)
	{
		try
		{
			vertices.put(x).put(y).put(color.x).put(color.y).put(color.z);
			vertices.put(x+width).put(y).put(color.x).put(color.y).put(color.z);
			vertices.put(x).put(y+height).put(color.x).put(color.y).put(color.z);
			
			vertices.put(x).put(y+height).put(color.x).put(color.y).put(color.z);
			vertices.put(x+width).put(y).put(color.x).put(color.y).put(color.z);
			vertices.put(x+width).put(y+height).put(color.x).put(color.y).put(color.z);
		}
		catch(BufferOverflowException e)
		{
			System.err.println("Error: tried adding more vertices than buffer had memory allocated for.");
			e.printStackTrace();
			System.exit(0);
		}
		vertCount+=6;
	}
	
	public void setColor(float r, float g, float b)
	{
		color.x = r/255.0f; color.y = g/255.0f; color.z = b/255.0f;
	}

}
