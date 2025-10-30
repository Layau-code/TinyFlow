package com.layor.tinyflow.Controller;

import com.layor.tinyflow.entity.ShortUrl;
import com.layor.tinyflow.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/history")
public class HistoryController
{

    @Autowired
    private
    HistoryService historyService;

    /**
     * 刷新用户的历史记录（例如重新加载或同步）
     * @return
    刷新后的完整历史记录列表
     */
    @GetMapping("/refresh")
    public
    ResponseEntity<List<ShortUrl>> refreshHistory() {
        List<ShortUrl> refreshedHistory = historyService.refreshHistory();
        return
                ResponseEntity.ok(refreshedHistory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistoryById(@PathVariable Long id) {
        try
        {
            historyService.deleteHistoryById(id);
            return ResponseEntity.noContent().build(); // 204
        } catch
        (NoSuchElementException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }
}