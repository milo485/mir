package mir.session;

import java.io.InputStream;

public interface UploadedFile {
  InputStream getInputStream() throws SessionExc, SessionFailure;
  String getContentType();
}