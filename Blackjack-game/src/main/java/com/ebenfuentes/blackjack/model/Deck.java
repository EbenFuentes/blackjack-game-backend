package com.ebenfuentes.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

	private List<Card> deckOfCards;

	public Deck() {
		initializeDeck();
	}

	public void initializeDeck() {

		String[] ranks = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
		String[] suits = { "Spades", "Hearts", "Diamonds", "Clubs" };
		deckOfCards = new ArrayList<>(52);

		for (String rank : ranks) {
			for (String suit : suits) {
				int value = rank.matches("\\d+") ? Integer.parseInt(rank) : rank.equals("A") ? 11 : 10;
				deckOfCards.add(new Card(rank, suit, value));
			}
		}
		Collections.shuffle(deckOfCards);

	}

	public void shuffleDeck() {
		Collections.shuffle(deckOfCards);
	}

	public Card dealCard() {
		if (!deckOfCards.isEmpty())
			return deckOfCards.remove(deckOfCards.size() - 1);
		return null;
	}

	public boolean isEmpty() {
		return deckOfCards.isEmpty();
	}

	public void resetDeck() {
		initializeDeck();
	}
	
	public int reminaingCards() {
		return deckOfCards.size();
	}

}
