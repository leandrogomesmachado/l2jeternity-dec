package l2e.gameserver.model.actor.templates.reflection;

public class ReflectionNameTemplate {
   private final String _nameEn;
   private final String _nameRu;

   public ReflectionNameTemplate(String nameEn, String nameRu) {
      this._nameEn = nameEn;
      this._nameRu = nameRu;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }
}
