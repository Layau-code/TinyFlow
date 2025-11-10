package com.layor.tinyflow.Strategy;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HashidsStrategy implements ShortCodeStrategy{
   @Autowired
   private Hashids hashids;

    @Override
    public String encode(long id) {
        return hashids.encode(id);
    }
}
