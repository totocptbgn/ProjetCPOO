package iu;

import java.io.PrintStream;

/**
 *  Use an init() to be sure that the ouput is set to WHITE by default.
 *  Use set() to transform a string to a colored string for the terminal.
 */

public class ColoredOutput {
	public static Color defaultColor = Color.WHITE;

	public static String set(Color color, String s) {
		return (char) 27 + "[" + color.code + "m" + s + (char) 27 + "[" + defaultColor.code + "m";
	}

	public static void init(PrintStream output, Color defaultColor) {
		ColoredOutput.defaultColor = defaultColor;
		output.print((char) 27 + "[" + ColoredOutput.defaultColor.code + "m");
	}

	public static void init() {
		init(System.out, Color.WHITE);
	}
}
