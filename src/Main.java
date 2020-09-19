import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

public class Main implements Runnable
{
	
	/*
	 * IMPROVE MOVEMENT WITH GLFW_REPEAT
	 */
	
	long window;
	static int WIDTH = 700, HEIGHT = 900;
	private Scene scene;
	public static boolean running = false;
	
	public Main()
	{
		Thread t = new Thread(this);
		t.start();
	}
	
	public void init()
	{
		glfwInit();
		
		window = glfwCreateWindow(WIDTH, HEIGHT, "PongGL", 0, 0); 
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width()-WIDTH)/2, (vidmode.height()-HEIGHT)/2);
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);  
		glfwShowWindow(window);
		glfwSwapInterval(0);
		glfwSetKeyCallback(window, new KeyInput());
		
		scene = new Scene();
	}
	
	public void run()
	{
		init();
		running = true;

		float ups = 60.0f;
		double interval = 1/ups;
		
		double delta = 0;
		double now;
		double last = glfwGetTime();
		
		int frames = 0, updates = 0;
		double timer = glfwGetTime();
		
		GL30.glClearColor(30/255.0f, 45/255.0f, 64/255.0f, 1.0f);
		while(!glfwWindowShouldClose(window) && running)
		{
			glfwPollEvents();
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
			
			now = glfwGetTime();
			delta += (now-last);
			last = now;
			if(delta >= interval)
			{
				delta -= interval;
				scene.update();
				updates++;
			}
			scene.render();
			frames++;
			
			if(glfwGetTime() - timer >= 1)
			{
				glfwSetWindowTitle(window, "UPS: " + updates + " | FPS: " + frames);
				updates = 0;
				frames = 0;
				timer = glfwGetTime();
			}
			
			glfwSwapBuffers(window);
		}
		System.out.println("Cleaning up...");
		glfwTerminate();
		System.out.println("Terminated.");
	}
	
	public static void main(String[] args) 
	{
		new Main();
	}
	
}
