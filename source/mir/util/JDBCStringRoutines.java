package mir.util;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class JDBCStringRoutines {
  private JDBCStringRoutines() {
  }

  public static String escapeStringLiteral(String aText) {
    final char[] CHARACTERS_TO_ESCAPE = { '\'', '\\', '%', '_', '?' };
    final String[] ESCAPE_CODES = { "\'\'", "\\\\", "\\%", "\\_", "\\?" };

    return StringRoutines.replaceStringCharacters(aText, CHARACTERS_TO_ESCAPE, ESCAPE_CODES);
  }

}