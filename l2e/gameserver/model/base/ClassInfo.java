package l2e.gameserver.model.base;

import java.util.regex.Matcher;

public final class ClassInfo {
   private final ClassId _classId;
   private final String _className;
   private final String _classServName;
   private final ClassId _parentClassId;

   public ClassInfo(ClassId classId, String className, String classServName, ClassId parentClassId) {
      this._classId = classId;
      this._className = className;
      this._classServName = classServName;
      this._parentClassId = parentClassId;
   }

   public ClassId getClassId() {
      return this._classId;
   }

   public String getClassName() {
      return this._className;
   }

   private int getClassClientId() {
      int classClientId = this._classId.getId();
      if (classClientId >= 0 && classClientId <= 57) {
         classClientId += 247;
      } else if (classClientId >= 88 && classClientId <= 118) {
         classClientId += 1071;
      } else if (classClientId >= 123 && classClientId <= 136) {
         classClientId += 1438;
      }

      return classClientId;
   }

   public String getClientCode() {
      return "&$" + this.getClassClientId() + ";";
   }

   public String getEscapedClientCode() {
      return Matcher.quoteReplacement(this.getClientCode());
   }

   public String getClassServName() {
      return this._classServName;
   }

   public ClassId getParentClassId() {
      return this._parentClassId;
   }
}
