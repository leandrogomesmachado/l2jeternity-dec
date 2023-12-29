package l2e.gameserver.instancemanager;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.fake.FakePlayer;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.zone.ZoneId;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class BotCheckManager {
   private static final Logger _log = Logger.getLogger(BotCheckManager.class.getName());
   public static CopyOnWriteArrayList<BotCheckManager.BotCheckQuestion> _questions = new CopyOnWriteArrayList<>();

   protected BotCheckManager() {
      Document doc = null;
      File file = new File(Config.DATAPACK_ROOT, "data/stats/chars/botQuestions.xml");
      if (!file.exists()) {
         _log.warning(this.getClass().getSimpleName() + ": botQuestions.xml file is missing.");
      } else {
         try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(file);
         } catch (Exception var5) {
            var5.printStackTrace();
         }

         try {
            this.parseBotQuestions(doc);
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }

   protected void parseBotQuestions(Document doc) {
      for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("question".equalsIgnoreCase(d.getNodeName())) {
                  int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                  String question_ru = d.getAttributes().getNamedItem("question_ru").getNodeValue();
                  String question_en = d.getAttributes().getNamedItem("question_en").getNodeValue();
                  boolean answer = Integer.parseInt(d.getAttributes().getNamedItem("answer").getNodeValue()) == 0;
                  BotCheckManager.BotCheckQuestion question_info = new BotCheckManager.BotCheckQuestion(id, question_ru, question_en, answer);
                  _questions.add(question_info);
               }
            }
         }
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + _questions.size() + " bot questions.");
      this.ScheduleNextQuestion();
   }

   public CopyOnWriteArrayList<BotCheckManager.BotCheckQuestion> getAllAquisions() {
      return _questions == null ? null : _questions;
   }

   public boolean checkAnswer(int qId, boolean answer) {
      for(BotCheckManager.BotCheckQuestion info : _questions) {
         if (info._id == qId) {
            return info.getAnswer() == answer;
         }
      }

      return true;
   }

   public BotCheckManager.BotCheckQuestion generateRandomQuestion() {
      return _questions.get(Rnd.get(0, _questions.size() - 1));
   }

   private void ScheduleNextQuestion() {
      ThreadPoolManager.getInstance()
         .schedule(new BotCheckManager.BotQuestionAsked(), (long)Rnd.get(Config.MINIMUM_TIME_QUESTION_ASK * 60000, Config.MAXIMUM_TIME_QUESTION_ASK * 60000));
   }

   public static BotCheckManager getInstance() {
      return BotCheckManager.SingletonHolder._instance;
   }

   public class BotCheckQuestion {
      public final int _id;
      public final String _ruDescr;
      public final String _enDescr;
      public final boolean _answer;

      public BotCheckQuestion(int id, String ruDescr, String enDescr, boolean answer) {
         this._id = id;
         this._ruDescr = ruDescr;
         this._enDescr = enDescr;
         this._answer = answer;
      }

      public int getId() {
         return this._id;
      }

      public String getDescr(String lang) {
         return lang != null && !lang.equalsIgnoreCase("en") ? this._ruDescr : this._enDescr;
      }

      public boolean getAnswer() {
         return this._answer;
      }
   }

   private class BotQuestionAsked extends RunnableImpl {
      private BotQuestionAsked() {
      }

      @Override
      public void runImpl() throws Exception {
         for(Player player : World.getInstance().getAllPlayers()) {
            if (player != null
               && !(player instanceof FakePlayer)
               && !player.getFarmSystem().isAutofarming()
               && player.getUCState() <= 0
               && !player.isInFightEvent()
               && !player.inObserverMode()
               && !player.isInsideZone(ZoneId.PVP)
               && !player.isInSiege()
               && !player.isInDuel()
               && !player.isInStoreMode()
               && !player.isInOfflineMode()
               && player.getPvpFlag() == 0
               && !player.isInOlympiadMode()
               && (
                  !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                     || !AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())
               )
               && player.getBotRating() <= Rnd.get(Config.MINIMUM_BOT_POINTS_TO_STOP_ASKING, Config.MAXIMUM_BOT_POINTS_TO_STOP_ASKING)) {
               for(Npc mob : World.getInstance().getAroundNpc(player)) {
                  if (mob.isMonster()) {
                     player.requestCheckBot();
                     break;
                  }
               }
            }
         }

         BotCheckManager.this.ScheduleNextQuestion();
      }
   }

   private static class SingletonHolder {
      protected static final BotCheckManager _instance = new BotCheckManager();
   }
}
