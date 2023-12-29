package jonelo.jacksum.ui;

import jonelo.jacksum.util.Service;

public class Summary {
   private long files = 0L;
   private long errorFiles = 0L;
   private long modifiedFiles = 0L;
   private long removedFiles = 0L;
   private long addedFiles = 0L;
   private long dirs = 0L;
   private long begin = 0L;
   private long errorDirs = 0L;
   private long bytes = 0L;
   private boolean check = false;
   private boolean enabled = false;

   public void reset() {
      this.files = 0L;
      this.errorFiles = 0L;
      this.modifiedFiles = 0L;
      this.removedFiles = 0L;
      this.addedFiles = 0L;
      this.dirs = 0L;
      this.begin = 0L;
      this.errorDirs = 0L;
      this.bytes = 0L;
      this.check = false;
      this.enabled = false;
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
      this.begin = System.currentTimeMillis();
   }

   public void setFiles(long var1) {
      this.files = var1;
   }

   public void addFile() {
      ++this.files;
   }

   public long getFiles() {
      return this.files;
   }

   public void setErrorFiles(long var1) {
      this.errorFiles = var1;
   }

   public void addErrorFile() {
      ++this.errorFiles;
   }

   public long getErrorFiles() {
      return this.errorFiles;
   }

   public void setModifiedFiles(long var1) {
      this.modifiedFiles = var1;
   }

   public long getModifiedFiles() {
      return this.modifiedFiles;
   }

   public void setRemovedFiles(long var1) {
      this.removedFiles = var1;
   }

   public long getRemovedFiles() {
      return this.removedFiles;
   }

   public void setAddedFiles(long var1) {
      this.addedFiles = var1;
   }

   public long getAddedFiles() {
      return this.addedFiles;
   }

   public void setBytes(long var1) {
      this.bytes = var1;
   }

   public long getBytes() {
      return this.bytes;
   }

   public void addBytes(long var1) {
      this.bytes += var1;
   }

   public void setDirs(long var1) {
      this.dirs = var1;
   }

   public void addDir() {
      ++this.dirs;
   }

   public long getDirs() {
      return this.dirs;
   }

   public void setErrorDirs(long var1) {
      this.errorDirs = var1;
   }

   public void addErrorDir() {
      ++this.errorDirs;
   }

   public long getErrorDirs() {
      return this.errorDirs;
   }

   public void setCheck(boolean var1) {
      this.check = var1;
   }

   public boolean isCheck() {
      return this.check;
   }

   public void print() {
      if (this.enabled) {
         long var1 = System.currentTimeMillis();
         System.err.println();
         if (!this.isCheck()) {
            System.err.println("Jacksum: processed directories: " + this.dirs);
            System.err.println("Jacksum: directory read errors: " + this.errorDirs);
         }

         System.err.println("Jacksum: processed files: " + this.files);
         System.err.println("Jacksum: processed bytes: " + this.bytes);
         System.err.println("Jacksum: file read errors: " + this.errorFiles);
         if (this.isCheck()) {
            System.err.println("Jacksum: removed files:  " + this.removedFiles);
            System.err.println("Jacksum: modified files: " + this.modifiedFiles);
            System.err.println("Jacksum: added files:  " + this.addedFiles);
         }

         System.err.println("Jacksum: elapsed time: " + Service.duration(var1 - this.begin));
      }
   }
}
