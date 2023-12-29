package l2e.gameserver.model.entity.mods.facebook;

import l2e.commons.annotations.Nullable;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public interface FacebookAction {
   String getId();

   FacebookActionType getActionType();

   FacebookProfile getExecutor();

   long getCreatedDate();

   long getExtractionDate();

   String getMessage();

   void changeMessage(String var1);

   FacebookAction getFather();

   default boolean hasSameFather(@Nullable FacebookAction father) {
      if (this.getFather() == null) {
         return father == null;
      } else {
         return this.getFather().equals(father);
      }
   }

   boolean canBeRemoved();

   void remove();
}
