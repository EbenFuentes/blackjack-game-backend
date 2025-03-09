package com.ebenfuentes.blackjack.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Hand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "hand", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Card> cards = new ArrayList<>();

    private boolean isDealerHand; // Add a flag to track if this hand belongs to the dealer

    public Hand() {
        this.isDealerHand = false; // Default is player hand
    }

    public Hand(boolean isDealer) {
        this.isDealerHand = isDealer;
    }

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isDealerHand() {
        return isDealerHand;
    }

    public void addCard(Card card) {
        card.setHand(this);
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

    public int getTotalValue() {
        int total = 0;
        int aceCount = 0;

        for (Card card : cards) {
            if (card.getRank().equals("Ace")) {
                aceCount++;  // Count Aces separately
                total += 1;  // Start by counting Aces as 1
            } else {
                total += card.getValue();
            }
        }

        // Convert Aces from 1 to 11 where possible (without busting)
        while (aceCount > 0 && total + 10 <= 21) {
            total += 10;
            aceCount--;
        }

        return total;
    }

    public List<Card> getCards() {
        return cards;
    }
}
