package main.java.utilities.clone;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;


public class DeepCopy {
    public static <T> T copy(T orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in. 
            ObjectInputStream in = 
                new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return (T)obj;
    }

}