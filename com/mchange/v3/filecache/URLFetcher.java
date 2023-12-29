package com.mchange.v3.filecache;

import com.mchange.v2.log.MLogger;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface URLFetcher {
   InputStream openStream(URL var1, MLogger var2) throws IOException;
}
