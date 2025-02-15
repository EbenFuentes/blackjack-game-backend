package com.ebenfuentes.model;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Player {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Integer id;
	
	private String username;
	private int balance;
	
	@Transient
	private Hand hand;
	
	public Player() {
		this.hand = new Hand();
	}
	
	public Player(String username, int balance) {
		this.username = username;
		this.balance = balance;
		this.hand = new Hand();
	}
	
	public void receiveCard(Card card) {
        hand.addCard(card);
    }
	
	public int calculateHandValue() {
        return hand.getTotalValue();
    }
	
	public void resetHand() {
        hand.clear();
    }
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	

}
