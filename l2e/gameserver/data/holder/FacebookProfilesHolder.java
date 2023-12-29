package l2e.gameserver.data.holder;

import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.annotations.Nullable;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public class FacebookProfilesHolder {
   private final CopyOnWriteArrayList<FacebookProfile> _profiles = new CopyOnWriteArrayList<>(FacebookDAO.loadFacebookProfiles());

   private FacebookProfilesHolder() {
   }

   public FacebookProfile loadOrCreateProfile(String facebookId, String facebookName) {
      FacebookProfile loadedProfile = this.getProfileById(facebookId);
      if (loadedProfile != null) {
         return loadedProfile;
      } else {
         FacebookProfile createdProfile = new FacebookProfile(facebookId, facebookName);
         this.addNewProfile(createdProfile, true);
         return createdProfile;
      }
   }

   public void addNewProfile(FacebookProfile profile, boolean saveInDatabase) {
      this._profiles.add(profile);
      if (saveInDatabase) {
         FacebookDAO.replaceFacebookProfile(profile);
      }
   }

   @Nullable
   public FacebookProfile getProfileById(@Nullable String facebookId) {
      if (facebookId != null && !facebookId.isEmpty()) {
         for(FacebookProfile profile : this._profiles) {
            if (profile.getId().equals(facebookId)) {
               return profile;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Nullable
   public FacebookProfile getProfileByName(@Nullable String facebookName, boolean ignoreSpaces, boolean ignoreCase) {
      if (facebookName != null && !facebookName.isEmpty()) {
         String nameToCompare = facebookName;
         if (ignoreSpaces) {
            nameToCompare = facebookName.replace(" ", "");
         }

         if (ignoreCase) {
            nameToCompare = nameToCompare.toLowerCase();
         }

         for(FacebookProfile profile : this._profiles) {
            String profileNameToCompare = profile.getName();
            if (ignoreSpaces) {
               profileNameToCompare = profileNameToCompare.replace(" ", "");
            }

            if (ignoreCase) {
               profileNameToCompare = profileNameToCompare.toLowerCase();
            }

            if (profileNameToCompare.equals(nameToCompare)) {
               return profile;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public String toString() {
      return "FacebookProfilesHolder{profiles=" + this._profiles + '}';
   }

   public static FacebookProfilesHolder getInstance() {
      return FacebookProfilesHolder.SingletonHolder.INSTANCE;
   }

   private static class SingletonHolder {
      private static final FacebookProfilesHolder INSTANCE = new FacebookProfilesHolder();
   }
}
