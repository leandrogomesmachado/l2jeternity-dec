package com.mchange.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface MailSender {
   void sendMail(String var1, String[] var2, String[] var3, String[] var4, String var5, String var6, String var7) throws IOException, ProtocolException, UnsupportedEncodingException;

   void sendMail(String var1, String[] var2, String[] var3, String[] var4, String var5, String var6) throws IOException, ProtocolException;
}
