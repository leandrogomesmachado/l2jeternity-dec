package l2e.gameserver.model.entity.mods.facebook;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.annotations.Nullable;

public final class ActionsAwaitingOwner {
   private final HashMap<FacebookAction, EnumMap<FacebookActionType, CopyOnWriteArrayList<FacebookAction>>> _actionsAwaitingOwner = new HashMap<>();

   private ActionsAwaitingOwner() {
   }

   public void addNewFather(OfficialPost father) {
      EnumMap<FacebookActionType, CopyOnWriteArrayList<FacebookAction>> rewardedTypes = new EnumMap<>(FacebookActionType.class);

      for(FacebookActionType rewardedType : father.getRewardedActionsForIterate()) {
         rewardedTypes.put(rewardedType, new CopyOnWriteArrayList<>());
      }

      this._actionsAwaitingOwner.put(father, rewardedTypes);
   }

   public void removeOldFather(FacebookAction father) {
   }

   public void addNewExtractedAction(FacebookAction action) {
      this._actionsAwaitingOwner.get(action.getFather()).get(action.getActionType()).add(action);
   }

   public void removeAction(FacebookAction action) {
      this._actionsAwaitingOwner.get(action.getFather()).get(action.getActionType()).remove(action);
   }

   public CopyOnWriteArrayList<FacebookAction> getActionsForIterate(@Nullable FacebookAction father, FacebookActionType type) {
      return this._actionsAwaitingOwner.get(father).get(type);
   }

   public ArrayList<FacebookAction> getActionsCopy(@Nullable FacebookAction father, FacebookActionType type) {
      return new ArrayList<>(this._actionsAwaitingOwner.get(father).get(type));
   }

   public static ActionsAwaitingOwner getInstance() {
      return ActionsAwaitingOwner.SingletonHolder.INSTANCE;
   }

   @Override
   public String toString() {
      return "ActionsAwaitingOwner{actionsAwaitingOwner=" + this._actionsAwaitingOwner + '}';
   }

   private static class SingletonHolder {
      private static final ActionsAwaitingOwner INSTANCE = new ActionsAwaitingOwner();
   }
}
