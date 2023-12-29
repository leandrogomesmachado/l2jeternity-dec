package l2e.gameserver.model.strings.client;

final class BuilderContainer extends Builder {
   private final Builder[] _builders;

   BuilderContainer(Builder[] builders) {
      this._builders = builders;
   }

   @Override
   public final String toString(Object param) {
      return this.toString(param);
   }

   @Override
   public final String toString(Object... params) {
      int buildersLength = this._builders.length;
      int paramsLength = params.length;
      String[] builds = new String[buildersLength];
      int buildTextLen = 0;
      String build;
      if (paramsLength != 0) {
         for(int i = buildersLength; i-- > 0; builds[i] = build) {
            Builder builder = this._builders[i];
            int paramIndex = builder.getIndex();
            build = paramIndex != -1 && paramIndex < paramsLength ? builder.toString(params[paramIndex]) : builder.toString();
            buildTextLen += build.length();
         }
      } else {
         for(int i = buildersLength; i-- > 0; builds[i] = build) {
            build = this._builders[i].toString();
            buildTextLen += build.length();
         }
      }

      FastStringBuilder fsb = new FastStringBuilder(buildTextLen);

      for(int var13 = 0; var13 < buildersLength; ++var13) {
         fsb.append(builds[var13]);
      }

      return fsb.toString();
   }

   @Override
   public final int getIndex() {
      return -1;
   }
}
