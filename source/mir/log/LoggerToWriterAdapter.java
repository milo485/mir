package mir.log;

import java.io.Writer;

public class LoggerToWriterAdapter extends Writer {
  private LoggerWrapper logger;
  private int messageType;
  private StringBuffer lineBuffer;
  private String lineSeparator;

  public LoggerToWriterAdapter(LoggerWrapper aLogger, int aMessageType) {
    lineBuffer = new StringBuffer();
    logger = aLogger;
    messageType = aMessageType;
    lineSeparator = System.getProperty("line.separator");
  }

  public LoggerToWriterAdapter(Logger aLogger, int aMessageType) {
    this(new LoggerWrapper(aLogger), aMessageType);
  }

  public void close() {
    flush();
  }

  public void flush() {
    if (lineBuffer.length()>0) {
      logger.message(messageType, lineBuffer.toString());
      lineBuffer.delete(0, lineBuffer.length());
    }
  }

  protected void checkBuffer() {
    int from = 0;
    int until = lineBuffer.toString().indexOf(lineSeparator, from);

    while (until>-1) {
      String line = lineBuffer.substring(from, until);
      logger.message(messageType, line);
      from = until + lineSeparator.length();
      until = lineBuffer.toString().indexOf(lineSeparator, from);
    }

    lineBuffer.delete(0, from);
  };

  public void write(char[] aBuffer, int anOffset, int aLength)  {
    lineBuffer.append(aBuffer, anOffset, aLength);
    checkBuffer();
  }
}