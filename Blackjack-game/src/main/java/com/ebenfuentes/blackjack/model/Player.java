package com.ebenfuentes.blackjack.model;

import jakarta.persistence.*;

@Entity
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String username;
	private int balance;
	private int bet; // New field for bet amount

	private boolean gameStarted = false;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "hand_id")
	private Hand hand; // Player's hand
	private boolean hasStood = false;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "dealer_hand_id")
	private Hand dealerHand; // Dealer's hand

	public Player() {
		this.hand = new Hand();
		this.dealerHand = new Hand();
	}

	public Player(String username, int balance) {
		this.setUsername(username);
		this.balance = balance;
		this.hand = new Hand();
		this.dealerHand = new Hand(true);
	}

	public void placeBet(int amount) {
		if (amount > balance) {
			throw new IllegalArgumentException("Insufficient balance to place bet.");
		}
		this.bet = amount;
		this.balance -= amount;
	}

	public void winBet() {
		this.balance += bet * 2; // Winning doubles the bet
		this.bet = 0;
	}

	public void loseBet() {
		this.bet = 0;
	}

	public void push() { // Tie scenario
		this.balance += bet;
		this.bet = 0;
	}

	public int getBet() {
		return bet;
	}

	public void resetHand() {
		hand.clear();
		dealerHand.clear();
		hasStood = false;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public Hand getHand() {
		return hand;
	}

	public Hand getDealerHand() {
		return dealerHand;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public void setDealerHand(Hand dealerHand) {
		this.dealerHand = dealerHand;
	}

	public void receiveCard(Card card) {
		if (hand != null) {
			hand.addCard(card);
		}
	}

	public int calculateHandValue() {
		return hand.getTotalValue();
	}

	public boolean hasStood() {
		return hasStood;
	}

	public void setHasStood(boolean hasStood) {
		this.hasStood = hasStood;
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void setGameStarted(boolean gameStarted) {
		this.gameStarted = gameStarted;
	}

}
