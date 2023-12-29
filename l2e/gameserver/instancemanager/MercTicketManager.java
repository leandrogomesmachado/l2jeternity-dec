package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DefenderInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.instance.ItemInstance;

public class MercTicketManager {
   private static final Logger _log = Logger.getLogger(MercTicketManager.class.getName());
   private static final List<ItemInstance> _droppedTickets = new CopyOnWriteArrayList<>();
   private static final int[] MAX_MERC_PER_TYPE = new int[]{
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      15,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      10,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20,
      20
   };
   protected static final int[] MERCS_MAX_PER_CASTLE = new int[]{100, 150, 200, 300, 400, 400, 400, 400, 400};
   private static final int[] ITEM_IDS = new int[]{
      3960,
      3961,
      3962,
      3963,
      3964,
      3965,
      3966,
      3967,
      3968,
      3969,
      6115,
      6116,
      6117,
      6118,
      6119,
      6120,
      6121,
      6122,
      6123,
      6124,
      6038,
      6039,
      6040,
      6041,
      6042,
      6043,
      6044,
      6045,
      6046,
      6047,
      6175,
      6176,
      6177,
      6178,
      6179,
      6180,
      6181,
      6182,
      6183,
      6184,
      6235,
      6236,
      6237,
      6238,
      6239,
      6240,
      6241,
      6242,
      6243,
      6244,
      6295,
      6296,
      3973,
      3974,
      3975,
      3976,
      3977,
      3978,
      3979,
      3980,
      3981,
      3982,
      6125,
      6126,
      6127,
      6128,
      6129,
      6130,
      6131,
      6132,
      6133,
      6134,
      6051,
      6052,
      6053,
      6054,
      6055,
      6056,
      6057,
      6058,
      6059,
      6060,
      6185,
      6186,
      6187,
      6188,
      6189,
      6190,
      6191,
      6192,
      6193,
      6194,
      6245,
      6246,
      6247,
      6248,
      6249,
      6250,
      6251,
      6252,
      6253,
      6254,
      6297,
      6298,
      3986,
      3987,
      3988,
      3989,
      3990,
      3991,
      3992,
      3993,
      3994,
      3995,
      6135,
      6136,
      6137,
      6138,
      6139,
      6140,
      6141,
      6142,
      6143,
      6144,
      6064,
      6065,
      6066,
      6067,
      6068,
      6069,
      6070,
      6071,
      6072,
      6073,
      6195,
      6196,
      6197,
      6198,
      6199,
      6200,
      6201,
      6202,
      6203,
      6204,
      6255,
      6256,
      6257,
      6258,
      6259,
      6260,
      6261,
      6262,
      6263,
      6264,
      6299,
      6300,
      3999,
      4000,
      4001,
      4002,
      4003,
      4004,
      4005,
      4006,
      4007,
      4008,
      6145,
      6146,
      6147,
      6148,
      6149,
      6150,
      6151,
      6152,
      6153,
      6154,
      6077,
      6078,
      6079,
      6080,
      6081,
      6082,
      6083,
      6084,
      6085,
      6086,
      6205,
      6206,
      6207,
      6208,
      6209,
      6210,
      6211,
      6212,
      6213,
      6214,
      6265,
      6266,
      6267,
      6268,
      6269,
      6270,
      6271,
      6272,
      6273,
      6274,
      6301,
      6302,
      4012,
      4013,
      4014,
      4015,
      4016,
      4017,
      4018,
      4019,
      4020,
      4021,
      6155,
      6156,
      6157,
      6158,
      6159,
      6160,
      6161,
      6162,
      6163,
      6164,
      6090,
      6091,
      6092,
      6093,
      6094,
      6095,
      6096,
      6097,
      6098,
      6099,
      6215,
      6216,
      6217,
      6218,
      6219,
      6220,
      6221,
      6222,
      6223,
      6224,
      6275,
      6276,
      6277,
      6278,
      6279,
      6280,
      6281,
      6282,
      6283,
      6284,
      6303,
      6304,
      5205,
      5206,
      5207,
      5208,
      5209,
      5210,
      5211,
      5212,
      5213,
      5214,
      6165,
      6166,
      6167,
      6168,
      6169,
      6170,
      6171,
      6172,
      6173,
      6174,
      6105,
      6106,
      6107,
      6108,
      6109,
      6110,
      6111,
      6112,
      6113,
      6114,
      6225,
      6226,
      6227,
      6228,
      6229,
      6230,
      6231,
      6232,
      6233,
      6234,
      6285,
      6286,
      6287,
      6288,
      6289,
      6290,
      6291,
      6292,
      6293,
      6294,
      6305,
      6306,
      6779,
      6780,
      6781,
      6782,
      6783,
      6784,
      6785,
      6786,
      6787,
      6788,
      6802,
      6803,
      6804,
      6805,
      6806,
      6807,
      6808,
      6809,
      6810,
      6811,
      6792,
      6793,
      6794,
      6795,
      6796,
      6797,
      6798,
      6799,
      6800,
      6801,
      6812,
      6813,
      6814,
      6815,
      6816,
      6817,
      6818,
      6819,
      6820,
      6821,
      6822,
      6823,
      6824,
      6825,
      6826,
      6827,
      6828,
      6829,
      6830,
      6831,
      6832,
      6833,
      7973,
      7974,
      7975,
      7976,
      7977,
      7978,
      7979,
      7980,
      7981,
      7982,
      7998,
      7999,
      8000,
      8001,
      8002,
      8003,
      8004,
      8005,
      8006,
      8007,
      7988,
      7989,
      7990,
      7991,
      7992,
      7993,
      7994,
      7995,
      7996,
      7997,
      8008,
      8009,
      8010,
      8011,
      8012,
      8013,
      8014,
      8015,
      8016,
      8017,
      8018,
      8019,
      8020,
      8021,
      8022,
      8023,
      8024,
      8025,
      8026,
      8027,
      8028,
      8029,
      7918,
      7919,
      7920,
      7921,
      7922,
      7923,
      7924,
      7925,
      7926,
      7927,
      7941,
      7942,
      7943,
      7944,
      7945,
      7946,
      7947,
      7948,
      7949,
      7950,
      7931,
      7932,
      7933,
      7934,
      7935,
      7936,
      7937,
      7938,
      7939,
      7940,
      7951,
      7952,
      7953,
      7954,
      7955,
      7956,
      7957,
      7958,
      7959,
      7960,
      7961,
      7962,
      7963,
      7964,
      7965,
      7966,
      7967,
      7968,
      7969,
      7970,
      7971,
      7972
   };
   private static final int[] NPC_IDS = new int[]{
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061,
      35010,
      35011,
      35012,
      35013,
      35014,
      35015,
      35016,
      35017,
      35018,
      35019,
      35020,
      35021,
      35022,
      35023,
      35024,
      35025,
      35026,
      35027,
      35028,
      35029,
      35030,
      35031,
      35032,
      35033,
      35034,
      35035,
      35036,
      35037,
      35038,
      35039,
      35040,
      35041,
      35042,
      35043,
      35044,
      35045,
      35046,
      35047,
      35048,
      35049,
      35050,
      35051,
      35052,
      35053,
      35054,
      35055,
      35056,
      35057,
      35058,
      35059,
      35060,
      35061
   };
   private static final int GUARDIAN_TYPES_COUNT = 52;

   protected MercTicketManager() {
      this.load();
   }

   public int getTicketCastleId(int itemId) {
      for(int i = 0; i < 9; ++i) {
         for(int i2 = 0; i2 < 50; i2 += 10) {
            if (itemId >= ITEM_IDS[i2 + i * 52] && itemId <= ITEM_IDS[i2 + 9 + i * 52]) {
               return i + 1;
            }
         }

         if (itemId >= ITEM_IDS[50] && itemId <= ITEM_IDS[51]) {
            return i + 1;
         }
      }

      return -1;
   }

   public void reload() {
      _droppedTickets.clear();
      this.load();
   }

   private final void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where isHired = 1");
         ResultSet rs = statement.executeQuery();
         int[] mercPlaced = new int[20];
         int startindex = 0;

         while(true) {
            int npcId;
            int x;
            int y;
            int z;
            Castle castle;
            while(true) {
               if (!rs.next()) {
                  rs.close();
                  statement.close();
                  _log.info(this.getClass().getSimpleName() + ": Loaded " + _droppedTickets.size() + " mercenary tickets.");
                  return;
               }

               npcId = rs.getInt("npcId");
               x = rs.getInt("x");
               y = rs.getInt("y");
               z = rs.getInt("z");
               castle = CastleManager.getInstance().getCastle(x, y, z);
               if (castle == null) {
                  break;
               }

               if (mercPlaced[castle.getId() - 1] < MERCS_MAX_PER_CASTLE[castle.getId() - 1]) {
                  startindex = 52 * (castle.getId() - 1);
                  mercPlaced[castle.getId() - 1]++;
                  break;
               }
            }

            for(int i = startindex; i < startindex + 52; ++i) {
               if (NPC_IDS[i] == npcId) {
                  if (castle != null && !castle.getSiege().getIsInProgress()) {
                     int itemId = ITEM_IDS[i];
                     ItemInstance dropticket = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
                     dropticket.setItemLocation(ItemInstance.ItemLocation.VOID);
                     dropticket.dropMe(null, x, y, z);
                     dropticket.setDropTime(0L);
                     _droppedTickets.add(dropticket);
                  }
                  break;
               }
            }
         }
      } catch (Exception var25) {
         _log.log(Level.WARNING, "Exception: loadMercenaryData(): " + var25.getMessage(), (Throwable)var25);
      }
   }

   public boolean isAtTypeLimit(int itemId) {
      int limit = -1;

      for(int i = 0; i < ITEM_IDS.length; ++i) {
         if (ITEM_IDS[i] == itemId) {
            limit = MAX_MERC_PER_TYPE[i];
            break;
         }
      }

      if (limit <= 0) {
         return true;
      } else {
         int count = 0;

         for(ItemInstance ticket : _droppedTickets) {
            if (ticket != null && ticket.getId() == itemId) {
               ++count;
            }
         }

         return count >= limit;
      }
   }

   public boolean isAtCasleLimit(int itemId) {
      int castleId = this.getTicketCastleId(itemId);
      if (castleId <= 0) {
         return true;
      } else {
         int limit = MERCS_MAX_PER_CASTLE[castleId - 1];
         if (limit <= 0) {
            return true;
         } else {
            int count = 0;

            for(ItemInstance ticket : _droppedTickets) {
               if (ticket != null && this.getTicketCastleId(ticket.getId()) == castleId) {
                  ++count;
               }
            }

            return count >= limit;
         }
      }
   }

   public int getMaxAllowedMerc(int castleId) {
      return MERCS_MAX_PER_CASTLE[castleId - 1];
   }

   public boolean isTooCloseToAnotherTicket(int x, int y, int z) {
      for(ItemInstance item : _droppedTickets) {
         double dx = (double)(x - item.getX());
         double dy = (double)(y - item.getY());
         double dz = (double)(z - item.getZ());
         if (dx * dx + dy * dy + dz * dz < 625.0) {
            return true;
         }
      }

      return false;
   }

   public int addTicket(int itemId, Player activeChar) {
      int x = activeChar.getX();
      int y = activeChar.getY();
      int z = activeChar.getZ();
      int heading = activeChar.getHeading();
      Castle castle = CastleManager.getInstance().getCastle(activeChar);
      if (castle == null) {
         return -1;
      } else {
         for(int i = 0; i < ITEM_IDS.length; ++i) {
            if (ITEM_IDS[i] == itemId) {
               this.spawnMercenary(NPC_IDS[i], x, y, z, 3000);
               castle.getSiege().getSiegeGuardManager().hireMerc(x, y, z, heading, NPC_IDS[i]);
               ItemInstance dropticket = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
               dropticket.setItemLocation(ItemInstance.ItemLocation.VOID);
               dropticket.dropMe(null, x, y, z);
               dropticket.setDropTime(0L);
               _droppedTickets.add(dropticket);
               return NPC_IDS[i];
            }
         }

         return -1;
      }
   }

   private void spawnMercenary(int npcId, int x, int y, int z, int despawnDelay) {
      NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
      if (template != null) {
         DefenderInstance npc = new DefenderInstance(IdFactory.getInstance().getNextId(), template);
         npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
         npc.setDecayed(false);
         npc.spawnMe(x, y, z + 20);
         if (despawnDelay > 0) {
            npc.scheduleDespawn((long)despawnDelay);
         }
      }
   }

   public void deleteTickets(int castleId) {
      Iterator<ItemInstance> it = _droppedTickets.iterator();

      while(it.hasNext()) {
         ItemInstance item = it.next();
         if (item != null && this.getTicketCastleId(item.getId()) == castleId) {
            item.decayMe();
            it.remove();
         }
      }
   }

   public void removeTicket(ItemInstance item) {
      int itemId = item.getId();
      int npcId = -1;

      for(int i = 0; i < ITEM_IDS.length; ++i) {
         if (ITEM_IDS[i] == itemId) {
            npcId = NPC_IDS[i];
            break;
         }
      }

      Castle castle = CastleManager.getInstance().getCastleById(this.getTicketCastleId(itemId));
      if (npcId > 0 && castle != null) {
         new SiegeGuardManager(castle).removeMerc(npcId, item.getX(), item.getY(), item.getZ());
      }

      _droppedTickets.remove(item);
   }

   public int[] getItemIds() {
      return ITEM_IDS;
   }

   public final List<ItemInstance> getDroppedTickets() {
      return _droppedTickets;
   }

   public static final MercTicketManager getInstance() {
      return MercTicketManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final MercTicketManager _instance = new MercTicketManager();
   }
}
