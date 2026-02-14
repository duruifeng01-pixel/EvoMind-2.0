package com.evomind.service;

import com.evomind.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {

    Card createCard(Long userId, String title, String summaryText, Long sourceId, String sourceUrl, String mindmapJson);

    Card getCardById(Long id, Long userId);

    List<Card> getCardsByUserId(Long userId);

    Page<Card> getCardsByUserId(Long userId, Pageable pageable);

    List<Card> getFavoriteCards(Long userId);

    List<Card> getCardsBySourceId(Long userId, Long sourceId);

    List<Card> searchCards(Long userId, String keyword);

    Card updateCard(Long id, Long userId, String title, String summaryText);

    void deleteCard(Long id, Long userId);

    void toggleFavorite(Long id, Long userId);

    void archiveCard(Long id, Long userId);

    void incrementViewCount(Long id);

    long countByUserId(Long userId);

    long countFavoritesByUserId(Long userId);
}
