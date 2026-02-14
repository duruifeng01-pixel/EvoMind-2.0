package com.evomind.service.impl;

import com.evomind.entity.Card;
import com.evomind.exception.BusinessException;
import com.evomind.repository.CardRepository;
import com.evomind.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public Card createCard(Long userId, String title, String summaryText, Long sourceId, 
                          String sourceUrl, String mindmapJson) {
        Card card = new Card();
        card.setUserId(userId);
        card.setTitle(title);
        card.setSummaryText(summaryText);
        card.setSourceId(sourceId);
        card.setSourceUrl(sourceUrl);
        card.setMindmapJson(mindmapJson);
        card.setIsFavorite(false);
        card.setIsArchived(false);
        card.setViewCount(0);
        return cardRepository.save(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Card getCardById(Long id, Long userId) {
        return cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("卡片不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByUserId(Long userId) {
        return cardRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Card> getCardsByUserId(Long userId, Pageable pageable) {
        return cardRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getFavoriteCards(Long userId) {
        return cardRepository.findByUserIdAndIsFavoriteTrueOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsBySourceId(Long userId, Long sourceId) {
        return cardRepository.findByUserIdAndSourceIdOrderByCreatedAtDesc(userId, sourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> searchCards(Long userId, String keyword) {
        return cardRepository.searchByUserIdAndKeyword(userId, keyword);
    }

    @Override
    @Transactional
    public Card updateCard(Long id, Long userId, String title, String summaryText) {
        Card card = getCardById(id, userId);
        if (title != null) {
            card.setTitle(title);
        }
        if (summaryText != null) {
            card.setSummaryText(summaryText);
        }
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long id, Long userId) {
        Card card = getCardById(id, userId);
        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long id, Long userId) {
        Card card = getCardById(id, userId);
        card.setIsFavorite(!Boolean.TRUE.equals(card.getIsFavorite()));
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void archiveCard(Long id, Long userId) {
        Card card = getCardById(id, userId);
        card.setIsArchived(true);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        cardRepository.incrementViewCount(id, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return cardRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFavoritesByUserId(Long userId) {
        return cardRepository.countByUserIdAndIsFavoriteTrue(userId);
    }
}
