import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BufferUtils 
{
	
	private BufferUtils() {}
	
	public static FloatBuffer createFloatBuffer(float[] arr)
	{
		FloatBuffer result = ByteBuffer.allocateDirect(arr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		result.put(arr);
		result.flip();
		return result;
	}
	
	public static ByteBuffer createByteBuffer(byte[] arr)
	{
		ByteBuffer result = ByteBuffer.allocateDirect(arr.length * 4).order(ByteOrder.nativeOrder());
		result.put(arr);
		result.flip();
		return result;
	}

}
