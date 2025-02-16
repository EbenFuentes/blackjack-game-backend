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

    public Hand() {}
    

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

    public List<Card> getCards() {
        return cards;
    }
}
