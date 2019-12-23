/**
 *  Enumeration of colors for the ColoredOutput class.
 *  You can add value from there (FG Code) :
 *  https://en.wikipedia.org/wiki/ANSI_escape_code#3/4_bit
 */

public enum Color {

	WHITE(97),
	RED(31),
	GREEN(32),
	YELLOW(33),
	BLUE(34);

	public final int code;
	private Color(int n) {
		this.code = n;
	}
}
