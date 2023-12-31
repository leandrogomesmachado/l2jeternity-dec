package org.apache.commons.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class FileSystemUtils {
   private static final FileSystemUtils INSTANCE = new FileSystemUtils();
   private static final int INIT_PROBLEM = -1;
   private static final int OTHER = 0;
   private static final int WINDOWS = 1;
   private static final int UNIX = 2;
   private static final int POSIX_UNIX = 3;
   private static final int OS;

   /** @deprecated */
   public static long freeSpace(String path) throws IOException {
      return INSTANCE.freeSpaceOS(path, OS, false);
   }

   public static long freeSpaceKb(String path) throws IOException {
      return INSTANCE.freeSpaceOS(path, OS, true);
   }

   long freeSpaceOS(String path, int os, boolean kb) throws IOException {
      if (path == null) {
         throw new IllegalArgumentException("Path must not be empty");
      } else {
         switch(os) {
            case 0:
               throw new IllegalStateException("Unsupported operating system");
            case 1:
               return kb ? this.freeSpaceWindows(path) / 1024L : this.freeSpaceWindows(path);
            case 2:
               return this.freeSpaceUnix(path, kb, false);
            case 3:
               return this.freeSpaceUnix(path, kb, true);
            default:
               throw new IllegalStateException("Exception caught when determining operating system");
         }
      }
   }

   long freeSpaceWindows(String path) throws IOException {
      path = FilenameUtils.normalize(path);
      if (path.length() > 2 && path.charAt(1) == ':') {
         path = path.substring(0, 2);
      }

      String[] cmdAttribs = new String[]{"cmd.exe", "/C", "dir /-c " + path};
      List lines = this.performCommand(cmdAttribs, Integer.MAX_VALUE);

      for(int i = lines.size() - 1; i >= 0; --i) {
         String line = (String)lines.get(i);
         if (line.length() > 0) {
            return this.parseDir(line, path);
         }
      }

      throw new IOException("Command line 'dir /-c' did not return any info for path '" + path + "'");
   }

   long parseDir(String line, String path) throws IOException {
      int bytesStart = 0;
      int bytesEnd = 0;

      int j;
      for(j = line.length() - 1; j >= 0; --j) {
         char c = line.charAt(j);
         if (Character.isDigit(c)) {
            bytesEnd = j + 1;
            break;
         }
      }

      while(j >= 0) {
         char c = line.charAt(j);
         if (!Character.isDigit(c) && c != ',' && c != '.') {
            bytesStart = j + 1;
            break;
         }

         --j;
      }

      if (j < 0) {
         throw new IOException("Command line 'dir /-c' did not return valid info for path '" + path + "'");
      } else {
         StringBuffer buf = new StringBuffer(line.substring(bytesStart, bytesEnd));

         for(int k = 0; k < buf.length(); ++k) {
            if (buf.charAt(k) == ',' || buf.charAt(k) == '.') {
               buf.deleteCharAt(k--);
            }
         }

         return this.parseBytes(buf.toString(), path);
      }
   }

   long freeSpaceUnix(String path, boolean kb, boolean posix) throws IOException {
      if (path.length() == 0) {
         throw new IllegalArgumentException("Path must not be empty");
      } else {
         path = FilenameUtils.normalize(path);
         String flags = "-";
         if (kb) {
            flags = flags + "k";
         }

         if (posix) {
            flags = flags + "P";
         }

         String[] cmdAttribs = flags.length() > 1 ? new String[]{"df", flags, path} : new String[]{"df", path};
         List lines = this.performCommand(cmdAttribs, 3);
         if (lines.size() < 2) {
            throw new IOException("Command line 'df' did not return info as expected for path '" + path + "'- response was " + lines);
         } else {
            String line2 = (String)lines.get(1);
            StringTokenizer tok = new StringTokenizer(line2, " ");
            if (tok.countTokens() < 4) {
               if (tok.countTokens() != 1 || lines.size() < 3) {
                  throw new IOException("Command line 'df' did not return data as expected for path '" + path + "'- check path is valid");
               }

               String line3 = (String)lines.get(2);
               tok = new StringTokenizer(line3, " ");
            } else {
               tok.nextToken();
            }

            tok.nextToken();
            tok.nextToken();
            String freeSpace = tok.nextToken();
            return this.parseBytes(freeSpace, path);
         }
      }
   }

   long parseBytes(String freeSpace, String path) throws IOException {
      try {
         long bytes = Long.parseLong(freeSpace);
         if (bytes < 0L) {
            throw new IOException("Command line 'df' did not find free space in response for path '" + path + "'- check path is valid");
         } else {
            return bytes;
         }
      } catch (NumberFormatException var5) {
         throw new IOException("Command line 'df' did not return numeric data as expected for path '" + path + "'- check path is valid");
      }
   }

   List performCommand(String[] cmdAttribs, int max) throws IOException {
      List lines = new ArrayList(20);
      Process proc = null;
      InputStream in = null;
      OutputStream out = null;
      InputStream err = null;
      BufferedReader inr = null;

      Object var10;
      try {
         proc = this.openProcess(cmdAttribs);
         in = proc.getInputStream();
         out = proc.getOutputStream();
         err = proc.getErrorStream();
         inr = new BufferedReader(new InputStreamReader(in));

         for(String line = inr.readLine(); line != null && lines.size() < max; line = inr.readLine()) {
            line = line.toLowerCase().trim();
            lines.add(line);
         }

         proc.waitFor();
         if (proc.exitValue() != 0) {
            throw new IOException("Command line returned OS error code '" + proc.exitValue() + "' for command " + Arrays.asList(cmdAttribs));
         }

         if (lines.size() == 0) {
            throw new IOException("Command line did not return any info for command " + Arrays.asList(cmdAttribs));
         }

         var10 = lines;
      } catch (InterruptedException var14) {
         throw new IOException("Command line threw an InterruptedException '" + var14.getMessage() + "' for command " + Arrays.asList(cmdAttribs));
      } finally {
         IOUtils.closeQuietly(in);
         IOUtils.closeQuietly(out);
         IOUtils.closeQuietly(err);
         IOUtils.closeQuietly(inr);
         if (proc != null) {
            proc.destroy();
         }
      }

      return (List)var10;
   }

   Process openProcess(String[] cmdAttribs) throws IOException {
      return Runtime.getRuntime().exec(cmdAttribs);
   }

   static {
      int os = 0;

      try {
         String osName = System.getProperty("os.name");
         if (osName == null) {
            throw new IOException("os.name not found");
         }

         osName = osName.toLowerCase();
         if (osName.indexOf("windows") != -1) {
            var3 = 1;
         } else if (osName.indexOf("linux") != -1
            || osName.indexOf("sun os") != -1
            || osName.indexOf("sunos") != -1
            || osName.indexOf("solaris") != -1
            || osName.indexOf("mpe/ix") != -1
            || osName.indexOf("freebsd") != -1
            || osName.indexOf("irix") != -1
            || osName.indexOf("digital unix") != -1
            || osName.indexOf("unix") != -1
            || osName.indexOf("mac os x") != -1) {
            var3 = 2;
         } else if (osName.indexOf("hp-ux") == -1 && osName.indexOf("aix") == -1) {
            var3 = 0;
         } else {
            var3 = 3;
         }
      } catch (Exception var2) {
         var3 = -1;
      }

      OS = var3;
   }
}
