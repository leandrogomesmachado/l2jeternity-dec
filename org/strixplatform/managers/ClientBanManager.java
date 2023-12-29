package org.strixplatform.managers;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.strixplatform.configs.MainConfig;
import org.strixplatform.database.impl.BanDAO;
import org.strixplatform.logging.Log;
import org.strixplatform.utils.BannedHWIDInfo;
import org.strixplatform.utils.DetectionResponse;
import org.strixplatform.utils.FailedCheckResolve;
import org.strixplatform.utils.ServerResponse;
import org.strixplatform.utils.StrixClientData;

public class ClientBanManager {
   private Map<String, BannedHWIDInfo> bannedHWIDInfo = BanDAO.getInstance().loadAllBannedHWID();
   private Queue<String> bannedHWIDBlockInfo = new ConcurrentLinkedQueue<>();

   public static ClientBanManager getInstance() {
      return ClientBanManager.LazyHolder.INSTANCE;
   }

   public ClientBanManager() {
      Log.info("Loaded [" + this.bannedHWIDInfo.size() + "] banned client HWID data");
      if (this.bannedHWIDInfo.size() > 0) {
         for(String fullHwid : this.bannedHWIDInfo.keySet()) {
            this.addHWIDBlock(fullHwid);
         }

         Log.info("Loaded [" + this.bannedHWIDBlockInfo.size() + "] banned HWID block");
      }
   }

   private void addHWIDBlock(String fullHwid) {
      try {
         String firstBlock = fullHwid.substring(0, 8);
         String secondBlock = fullHwid.substring(8, 16);
         String thirdBlock = fullHwid.substring(16, 24);
         String fourthBlock = fullHwid.substring(24, 32);
         if (!this.bannedHWIDBlockInfo.contains(firstBlock)) {
            this.bannedHWIDBlockInfo.add(firstBlock);
         }

         if (!this.bannedHWIDBlockInfo.contains(secondBlock)) {
            this.bannedHWIDBlockInfo.add(secondBlock);
         }

         if (!this.bannedHWIDBlockInfo.contains(thirdBlock)) {
            this.bannedHWIDBlockInfo.add(thirdBlock);
         }

         if (!this.bannedHWIDBlockInfo.contains(fourthBlock)) {
            this.bannedHWIDBlockInfo.add(fourthBlock);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   private void removeHWIDBlock(String fullHwid) {
      try {
         String firstBlock = fullHwid.substring(0, 8);
         String secondBlock = fullHwid.substring(8, 16);
         String thirdBlock = fullHwid.substring(16, 24);
         String fourthBlock = fullHwid.substring(24, 32);
         if (this.bannedHWIDBlockInfo.contains(firstBlock)) {
            this.bannedHWIDBlockInfo.remove(firstBlock);
         }

         if (this.bannedHWIDBlockInfo.contains(secondBlock)) {
            this.bannedHWIDBlockInfo.remove(secondBlock);
         }

         if (this.bannedHWIDBlockInfo.contains(thirdBlock)) {
            this.bannedHWIDBlockInfo.remove(thirdBlock);
         }

         if (this.bannedHWIDBlockInfo.contains(fourthBlock)) {
            this.bannedHWIDBlockInfo.remove(fourthBlock);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   public void acceptResolve(FailedCheckResolve failedCheckResolve, StrixClientData clientData) {
      if (failedCheckResolve == FailedCheckResolve.BAN) {
         Log.audit("Server resolve [" + failedCheckResolve.toString() + "] for failed check [" + clientData.getServerResponse().toString() + "]");
         this.tryToStoreBan(clientData);
      }
   }

   public boolean checkEnterFullHWIDBanned(StrixClientData clientData) {
      String clientHWID = clientData.getClientHWID();
      if (clientHWID.length() != 32) {
         Log.error("Client HWID=[" + clientHWID + "] not correct for size! Please send this message to Strix-Platform support!");
         return true;
      } else {
         if (this.bannedHWIDInfo.containsKey(clientHWID)) {
            BannedHWIDInfo bhi = this.bannedHWIDInfo.get(clientHWID);
            if (bhi.getTimeExpire() > System.currentTimeMillis()) {
               Log.audit("Client HWID=[" + clientHWID + "] attemp to enter from full banned HWID");
               return true;
            }

            this.tryToDeleteBan(clientData);
            Log.audit("Client HWID=[" + clientHWID + "] ban expired and deleted from database and cache");
         }

         return false;
      }
   }

   public boolean checkEnterBlockHWIDBanned(StrixClientData clientData) {
      String clientHWID = clientData.getClientHWID();
      if (clientHWID.length() != 32) {
         Log.error("Client HWID=[" + clientHWID + "] not correct for size! Please send this message to Strix-Platform support!");
         return true;
      } else {
         String firstBlock = clientHWID.substring(0, 8);
         String secondBlock = clientHWID.substring(8, 16);
         String thirdBlock = clientHWID.substring(16, 24);
         String fourthBlock = clientHWID.substring(24, 32);
         int blockFindedInBan = 0;
         if (this.bannedHWIDBlockInfo.contains(firstBlock)) {
            ++blockFindedInBan;
         }

         if (this.bannedHWIDBlockInfo.contains(secondBlock)) {
            ++blockFindedInBan;
         }

         if (this.bannedHWIDBlockInfo.contains(thirdBlock)) {
            ++blockFindedInBan;
         }

         if (this.bannedHWIDBlockInfo.contains(fourthBlock)) {
            ++blockFindedInBan;
         }

         if (blockFindedInBan > 0 && blockFindedInBan >= MainConfig.STRIX_PLATFORM_HWID_BLOCK_TO_LOCK) {
            Log.audit("Client HWID=[" + clientHWID + "] attemp to enter from banned count=[" + blockFindedInBan + "] block");
            clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_HWID_BLOCK_BLOCKED);
            return true;
         } else {
            return false;
         }
      }
   }

   public void tryToStoreBan(StrixClientData clientData) {
      if (this.bannedHWIDInfo != null && this.bannedHWIDBlockInfo != null) {
         String clientHWID = clientData.getClientHWID();
         if (!this.bannedHWIDInfo.containsKey(clientData.getClientHWID())) {
            BannedHWIDInfo bhi = new BannedHWIDInfo(
               clientData.getClientHWID(),
               System.currentTimeMillis() + MainConfig.STRIX_PLATFORM_AUTOMATICAL_BAN_TIME * 60L * 1000L,
               clientData.getDetectionResponse() != null && clientData.getDetectionResponse() != DetectionResponse.RESPONSE_OK
                  ? clientData.getDetectionResponse().getDescription()
                  : clientData.getServerResponse().toString(),
               "AUTOMATICAL_BAN"
            );
            if (BanDAO.getInstance().insert(bhi)) {
               this.bannedHWIDInfo.put(clientHWID, bhi);
               this.addHWIDBlock(clientHWID);
               Log.audit("Client HWID=[" + clientHWID + "] added in cache and stored to database");
            }
         } else {
            Log.audit("Client HWID=[" + clientHWID + "] finded in cache and not store in database");
         }
      }
   }

   public void tryToStoreBan(BannedHWIDInfo bhi) {
      if (this.bannedHWIDInfo != null && this.bannedHWIDBlockInfo != null) {
         if (!this.bannedHWIDInfo.containsKey(bhi.getHWID())) {
            if (BanDAO.getInstance().insert(bhi)) {
               this.bannedHWIDInfo.put(bhi.getHWID(), bhi);
               this.addHWIDBlock(bhi.getHWID());
               Log.audit("Client HWID=[" + bhi.getHWID() + "] added in cache and stored to database");
            }
         } else {
            Log.audit("Client HWID=[" + bhi.getHWID() + "] finded in cache and not store in database");
         }
      }
   }

   public void tryToDeleteBan(StrixClientData clientData) {
      if (this.bannedHWIDInfo != null && this.bannedHWIDBlockInfo != null) {
         String clientHWID = clientData.getClientHWID();
         if (BanDAO.getInstance().delete(clientHWID)) {
            this.bannedHWIDInfo.remove(clientHWID);
            this.removeHWIDBlock(clientHWID);
         }
      }
   }

   public void tryToDeleteBan(String clientHWID) {
      if (this.bannedHWIDInfo != null && this.bannedHWIDBlockInfo != null) {
         if (BanDAO.getInstance().delete(clientHWID)) {
            this.bannedHWIDInfo.remove(clientHWID);
            this.removeHWIDBlock(clientHWID);
         }
      }
   }

   private static class LazyHolder {
      private static final ClientBanManager INSTANCE = new ClientBanManager();
   }
}
