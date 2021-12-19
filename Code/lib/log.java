package lib;

import java.io.PrintWriter;
import java.io.StringWriter;

public class log {
	public static final String RESET = "\u001B[0m";
	public static final String RED = "\u001B[31m";
	public static final String GREEN = "\u001B[32m";
	public static final String CYAN = "\u001B[36m";

	public static void e(String in) {
		System.out.println(RED + "[Error] " + in + RESET);
	}

	public static void s(String in) {
		System.out.println(GREEN + "[Success] " + in + RESET);
	}

	public static void i(String in) {
		System.out.println(CYAN + "[Info] " + in + RESET);
	}
	public static void e(Exception e, String className, String FuncName) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log.e(className+"."+FuncName+"(..)"+" : "+sw.toString());
	}
}
