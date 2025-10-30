package com.layor.tinyflow.service;

import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HistoryService {
@Autowired
private ShortUrlRepository shortUrlRepository;
    public List<ShortUrl> refreshHistory() {
        return shortUrlRepository.findAllByOrderByCreatedAtDesc();
    }
    @Transactional
    public void deleteHistoryById(Long id)
    {
        if
        (!shortUrlRepository.existsById(id)) {
            throw new NoSuchElementException("History record with id " + id + " not found."
            );
        }
        shortUrlRepository.deleteById(id);
    }
}
