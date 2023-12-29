package org.eclipse.jdt.core;

import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jdt.core.index.JavaIndexer;
import org.eclipse.jdt.internal.antadapter.AntAdapterMessages;

public class BuildJarIndex extends Task {
   private String jarPath;
   private String indexPath;

   public void execute() throws BuildException {
      if (this.jarPath == null) {
         throw new BuildException(AntAdapterMessages.getString("buildJarIndex.jarFile.cannot.be.null"));
      } else if (this.indexPath == null) {
         throw new BuildException(AntAdapterMessages.getString("buildJarIndex.indexFile.cannot.be.null"));
      } else {
         try {
            JavaIndexer.generateIndexForJar(this.jarPath, this.indexPath);
         } catch (IOException var2) {
            throw new BuildException(AntAdapterMessages.getString("buildJarIndex.ioexception.occured", var2.getLocalizedMessage()));
         }
      }
   }

   public void setJarPath(String path) {
      this.jarPath = path;
   }

   public void setIndexPath(String path) {
      this.indexPath = path;
   }
}
