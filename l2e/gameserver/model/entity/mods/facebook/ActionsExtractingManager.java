package l2e.gameserver.model.entity.mods.facebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.entity.mods.facebook.extractions.ExtractComments;
import l2e.gameserver.model.entity.mods.facebook.extractions.ExtractLikes;
import l2e.gameserver.model.entity.mods.facebook.extractions.ExtractOfficialPosts;
import l2e.gameserver.model.entity.mods.facebook.extractions.ExtractShares;

public class ActionsExtractingManager {
   protected static final Logger _log = Logger.getLogger(ActionsExtractingManager.class.getName());
   private final List<ActionsExtractor> _extractors = new ArrayList<>(FacebookActionType.values().length + 1);
   private ScheduledFuture<?> _extractionThread;

   private ActionsExtractingManager() {
   }

   public void load() {
      this.checkExtractionThreadStatus();
   }

   public void onActionExtracted(FacebookAction action) {
      boolean foundOwner = ActiveTasksHandler.getInstance().checkTaskCompleted(action);
      if (!foundOwner) {
         ActionsAwaitingOwner.getInstance().addNewExtractedAction(action);
      }
   }

   public void addExtractor(ActionsExtractor extractor) {
      this._extractors.add(extractor);
   }

   public void addExtractor(ActionsExtractor extractor, boolean extractImmediately) {
      this._extractors.add(extractor);
      if (extractImmediately && Config.ALLOW_FACEBOOK_SYSTEM) {
         this.extractSpecific(extractor);
      }
   }

   public ActionsExtractor getExtractor(String extractorName) {
      for(ActionsExtractor extractor : this._extractors) {
         if (extractor.getClass().getSimpleName().equalsIgnoreCase(extractorName)) {
            return extractor;
         }
      }

      return null;
   }

   private void extractAll() {
      String token = Config.FACEBOOK_TOKEN;

      for(ActionsExtractor extractor : this._extractors) {
         try {
            extractor.extractData(token);
         } catch (IOException var5) {
            _log.warning(extractor.getClass().getSimpleName() + ": Parsing error with token: " + Config.FACEBOOK_TOKEN);
         }
      }
   }

   private void extractSpecific(ActionsExtractor extractor) {
      try {
         extractor.extractData(Config.FACEBOOK_TOKEN);
      } catch (IOException var3) {
         _log.warning(extractor.getClass().getSimpleName() + ": Parsing error with token: " + Config.FACEBOOK_TOKEN);
      }
   }

   public static void onActionDisappeared(FacebookAction removedAction, boolean completed) {
      if (completed) {
         CompletedTasksHistory.getInstance().removeCompletedTask(removedAction, true);
         removedAction.getExecutor().addNegativePoint(removedAction.getActionType(), true);
      } else {
         ActionsAwaitingOwner.getInstance().removeAction(removedAction);
      }
   }

   private void checkExtractionThreadStatus() {
      if (Config.ALLOW_FACEBOOK_SYSTEM) {
         this.addExtractor(new ExtractLikes());
         this.addExtractor(new ExtractComments());
         this.addExtractor(new ExtractOfficialPosts(), true);
         this.addExtractor(new ExtractShares());
         if (this._extractionThread == null) {
            this._extractionThread = ThreadPoolManager.getInstance()
               .scheduleAtFixedDelay(new ActionsExtractingManager.ExtractionThread(), 0L, (long)Config.FACEBOOK_EXTRACTION_DELAY);
         }
      } else if (this._extractionThread != null) {
         this._extractionThread.cancel(false);
      }
   }

   public static ActionsExtractingManager getInstance() {
      return ActionsExtractingManager.SingletonHolder.INSTANCE;
   }

   private static class ExtractionThread extends RunnableImpl {
      private ExtractionThread() {
      }

      @Override
      public void runImpl() {
         ActionsExtractingManager.getInstance().extractAll();
      }
   }

   private static class SingletonHolder {
      private static final ActionsExtractingManager INSTANCE = new ActionsExtractingManager();
   }
}
