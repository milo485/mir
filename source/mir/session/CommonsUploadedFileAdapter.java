package mir.session;

import org.apache.commons.fileupload.*;
import java.io.*;

public class CommonsUploadedFileAdapter implements UploadedFile {
  private FileItem fileItem;
  private Object container;

  public CommonsUploadedFileAdapter(Object aContainer, FileItem aFileItem) {
    container = aContainer;
    fileItem = aFileItem;
  }

  public InputStream getInputStream() throws SessionExc, SessionFailure{
    try {
      return fileItem.getInputStream();
    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
  };

  public String getContentType() {
    return fileItem.getContentType();
  };
}