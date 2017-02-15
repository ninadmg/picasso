package com.squareup.picasso;

import com.squareup.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ninad on 15/02/17.
 */

public class CacheHelper {

    private static final int VERSION = 1;
    private static final int ENTRY_METADATA = 0;
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 1;


    private final DiskLruCache cache;

    public CacheHelper(File directory, long maxSize) throws IOException {
        this.cache = DiskLruCache.open(directory, VERSION, ENTRY_COUNT, maxSize);
    }

    public Downloader.Response get(String key)
    {

        DiskLruCache.Snapshot snapshot;

        try {
            snapshot = cache.get(key);
            if (snapshot==null)
            {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        Downloader.Response response = new Downloader.Response(snapshot.getInputStream(0),true,snapshot.getLength(0));

        return response;
    }

    public void put(InputStream stream, long contentLength, String key)
    {
        try {
            DiskLruCache.Editor editor = cache.edit(key);

            OutputStream outputStream = editor.newOutputStream(0);
            copyStream(stream,outputStream);
            outputStream.close();
            editor.commit();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public void close() throws IOException {
        cache.close();
    }
}
