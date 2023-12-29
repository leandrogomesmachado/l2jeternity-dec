package jonelo.jacksum.cli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import jonelo.sugar.util.ExitException;

public class JacksumHelp {
   public static void printVersion() {
      System.out.println("Jacksum 1.7.0");
   }

   public static void printGPL() {
      System.out.println("\n Jacksum v1.7.0, Copyright (C) 2002-2006, Dipl.-Inf. (FH) Johann N. Loefflmann\n");
      System.out.println(" Jacksum comes with ABSOLUTELY NO WARRANTY; for details see 'license.txt'.");
      System.out.println(" This is free software, and you are welcome to redistribute it under certain");
      System.out.println(" conditions; see 'license.txt' for details.");
      System.out.println(" This software is OSI Certified Open Source Software.");
      System.out.println(" OSI Certified is a certification mark of the Open Source Initiative.\n");
      System.out.println(" Go to http://www.jonelo.de/java/jacksum/index.html to get the latest version.\n");
   }

   public static void printHelpShort() throws ExitException {
      printGPL();
      System.out.println(" For more information please type:");
      System.out.println(" java -jar jacksum.jar -h en");
      System.out.println("\n Fuer weitere Informationen bitte eingeben:");
      System.out.println(" java -jar jacksum.jar -h de\n");
      throw new ExitException(null, 0);
   }

   public static void printHelpLong(String var0, String var1) throws FileNotFoundException, IOException {
      InputStream var2 = null;
      InputStreamReader var3 = null;
      BufferedReader var4 = null;

      try {
         var2 = (class$jonelo$jacksum$cli$Jacksum == null
               ? (class$jonelo$jacksum$cli$Jacksum = class$("jonelo.jacksum.cli.Jacksum"))
               : class$jonelo$jacksum$cli$Jacksum)
            .getResourceAsStream(var0);
         if (var2 == null) {
            throw new FileNotFoundException(var0);
         }

         var3 = new InputStreamReader(var2);
         var4 = new BufferedReader(var3);
         String var11;
         if (var1 == null) {
            while((var11 = var4.readLine()) != null) {
               System.out.println(var11);
            }
         } else {
            StringBuffer var6 = new StringBuffer();
            boolean var7 = false;

            while((var11 = var4.readLine()) != null) {
               if (var11.length() == 0) {
                  if (var7 && var6.length() > 0) {
                     System.out.println(var6.toString());
                  }

                  var7 = false;
                  var6 = new StringBuffer();
               } else {
                  var6.append(var11);
                  var6.append('\n');
                  if (!var7 && (var11.length() > 18 && var11.substring(0, 18).trim().toLowerCase().startsWith(var1) || var11.toLowerCase().startsWith(var1))) {
                     var7 = true;
                  }
               }
            }
         }
      } finally {
         if (var4 != null) {
            var4.close();
         }

         if (var3 != null) {
            var3.close();
         }

         if (var2 != null) {
            var2.close();
         }
      }
   }

   public static void help(String var0, String var1) throws ExitException {
      String var2 = "/help/jacksum/help_" + var0 + ".txt";
      byte var3 = 0;

      try {
         printHelpLong(var2, var1);
      } catch (FileNotFoundException var5) {
         System.err.println("Helpfile " + var2 + " not found.");
         var3 = 2;
      } catch (IOException var6) {
         System.err.println("Problem while reading helpfile " + var2);
         var3 = 2;
      }

      throw new ExitException(null, var3);
   }
}
