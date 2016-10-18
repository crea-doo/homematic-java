package at.creadoo.homematic.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public final class PacketUtil {

    private static final Logger log = Logger.getLogger(PacketUtil.class);

	private PacketUtil() {
		//
	}

    /**
     * Reads a "line" from an {@link InputStream}.
     *
     * @return The read bytes
     */
    public static byte[] readLine(final InputStream inputStream) {
    	final int bufferMax = 2048;
        final byte[] buff = new byte[bufferMax];
        
    	final int dataMax = 1000000;
        final byte[] data = new byte[dataMax];
        
        try {
            int offset = 0;
            int readBytes;
            do {
            	// Read data in. Maximum bufferMax
            	readBytes = inputStream.read(buff, 0, bufferMax);
            	//log.debug("Read bytes: Len: " + readBytes + ", Offset: " + offset);
            	if (readBytes == -1) {
            		log.error("Error while reading input stream: No data available");
            		break;
            	}
            	
            	// Check if size would exceed dataMax
            	if (offset + readBytes >= dataMax) {
            		log.error("Error while reading input stream: Too much data");
            		return null;
            	}

            	// Copy bytes
                System.arraycopy(buff, 0, data, offset, readBytes);
                
                // Calculate next offset
            	offset = offset + readBytes;
            } while(readBytes == bufferMax);
            
            if (offset > 0) {
	            final byte[] result = new byte[offset];
	            System.arraycopy(data, 0, result, 0, offset);
	            return result;
            }
		} catch (final IOException ex) {
			log.error("Error while reading input stream, throwing away current frame [" + Util.toString(buff) + "]", ex);
		} catch (final Throwable ex) {
			log.error("Error while reading input stream, throwing away current frame [" + Util.toString(buff) + "]", ex);
		}
        
        return null;
    }
    
}
