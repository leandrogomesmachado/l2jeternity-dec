package l2e.fake;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.holder.CharNameHolder;

public class FakePlayerNameManager {
   public static final Logger _log = Logger.getLogger(FakePlayerNameManager.class.getName());
   private static final FakePlayerNameManager _instance = new FakePlayerNameManager();
   private List<String> _fakePlayerNames;

   public static final FakePlayerNameManager getInstance() {
      return _instance;
   }

   public FakePlayerNameManager() {
      this.loadWordlist();
   }

   public String getRandomAvailableName() {
      String name = this.getRandomNameFromWordlist();

      while(this.nameAlreadyExists(name)) {
         name = this.getRandomNameFromWordlist();
      }

      return name;
   }

   private String getRandomNameFromWordlist() {
      return this._fakePlayerNames.get(Rnd.get(0, this._fakePlayerNames.size() - 1));
   }

   public List<String> getFakePlayerNames() {
      return this._fakePlayerNames;
   }

   private void loadWordlist() {
      try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("./config/mods/fakes/fakenamewordlist.txt"))))) {
         ArrayList<String> playersList = new ArrayList<>();

         String line;
         while((line = lnr.readLine()) != null) {
            if (line.trim().length() != 0 && !line.startsWith("#")) {
               playersList.add(line);
            }
         }

         this._fakePlayerNames = playersList;
         _log.log(Level.INFO, String.format("Loaded %s fake player names.", this._fakePlayerNames.size()));
      } catch (Exception var15) {
         var15.printStackTrace();
      }
   }

   private boolean nameAlreadyExists(String name) {
      return CharNameHolder.getInstance().getIdByName(name) > 0;
   }
}
