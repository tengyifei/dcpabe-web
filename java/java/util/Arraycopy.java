package java.util;

public class Arraycopy {
	private static int MAX_ELEMENTS_PER_COPY = 4000;

	static public void arraycopy_custom(byte[] src, int srcPos, byte[] dest, int destPost,
			int length) {
    	while (length > MAX_ELEMENTS_PER_COPY ) {
      	  System.arraycopy(src, srcPos, dest, destPost, Math.min(length, MAX_ELEMENTS_PER_COPY));
      	  srcPos += MAX_ELEMENTS_PER_COPY;
      	  destPost += MAX_ELEMENTS_PER_COPY;
      	  length -= MAX_ELEMENTS_PER_COPY;
    	}
    	System.arraycopy(src, srcPos, dest, destPost, length);
	}
}
