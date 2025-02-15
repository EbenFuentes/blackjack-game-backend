package com.ebenfuentes.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private final List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    // Add a card to the hand
    public void addCard(Card card) {
        cards.add(card);
    }

    // Clear the hand (when a new game starts)
    public void clear() {
        cards.clear();
    }

    // Get the total value of the hand, handling Aces properly
    public int getTotalValue() {
        int total = 0;
        int aceCount = 0;

        for (Card card : cards) {
            total += card.getValue();
            if (card.getRank().equals("A")) {
                aceCount++;
            }
        }

        // Adjust for Aces (Ace can be 1 or 11)
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }

        return total;
    }

    // Return an unmodifiable list to prevent external modifications
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
