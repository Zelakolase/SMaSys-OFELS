package lib;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class IO {
	/*
	 * Reads file as byte array
	 */
	public static byte[] read(String filename) {
		try {
			return Files.readAllBytes(Paths.get(filename));
		} catch (Exception e) {
			log.e(e,IO.class.getName(),"read");
			return null;
		}
	}

	/*
	 * Writes on file
	 */
	public static void write(String filename, String content, boolean append) {

		try {
			StandardOpenOption set = null;
			if (append)
				set = StandardOpenOption.APPEND;
			if (!append)
				set = StandardOpenOption.WRITE;
			Files.write(Paths.get(filename), content.getBytes(), set);
		} catch (Exception e) {
			log.e(e,IO.class.getName(),"write");
		}
	}
	public static void write(String filename, byte[] content, boolean append) {

		try {
			StandardOpenOption set = null;
			if (append)
				set = StandardOpenOption.APPEND;
			if (!append)
				set = StandardOpenOption.WRITE;
			Files.write(Paths.get(filename), content, set);
		} catch (Exception e) {
			log.e(e,IO.class.getName(),"write");
		}
	}
}