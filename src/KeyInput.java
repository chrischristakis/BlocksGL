import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyInput extends GLFWKeyCallback
{

	static boolean[] keys = new boolean[65536];
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) 
	{		
		if(key < 0) return;
		keys[key] = (action != GLFW.GLFW_RELEASE);
		if(keys[GLFW.GLFW_KEY_ESCAPE])
			Main.running = false;
	}
	
	public static boolean isPressed(int key)
	{
		return keys[key];
	}
	
}
