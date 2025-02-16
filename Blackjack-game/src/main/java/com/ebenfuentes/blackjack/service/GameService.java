package com.ebenfuentes.blackjack.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ebenfuentes.blackjack.model.Card;
import com.ebenfuentes.blackjack.model.Hand;
import com.ebenfuentes.blackjack.model.Player;
import com.ebenfuentes.blackjack.repository.HandRepository;
import com.ebenfuentes.blackjack.repository.PlayerRepository;

@Service
public class GameService {
    private final PlayerRepository playerRepository;
    private final HandRepository handRepository;
    private List<Card> deck;

    public GameService(PlayerRepository playerRepository, HandRepository handRepository) {
        this.playerRepository = playerRepository;
        this.handRepository = handRepository;
        this.deck = generateDeck();
    }

    // Generate a fresh deck of 52 shuffled cards
    private List<Card> generateDeck() {
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] suits = {"Spades", "Hearts", "Diamonds", "Clubs"};
        List<Card> newDeck = new ArrayList<>();

        for (String rank : ranks) {
            for (String suit : suits) {
                int value = rank.matches("\\d+") ? Integer.parseInt(rank) : (rank.equals("A") ? 11 : 10);
                newDeck.add(new Card(rank, suit, value));
            }
        }
        Collections.shuffle(newDeck);
        return newDeck;
    }

    // Create a new player
    public Player createPlayer(String username, int balance) {
        Player player = new Player(username, balance);
        return playerRepository.save(player);
    }

    public void startGame(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            // Check if hand exists, otherwise create new one
            Hand hand = player.getHand();
            if (hand == null) {
                hand = new Hand();
            } else {
                hand.clear(); // Reset hand
            }

            if (deck.size() < 2) {
                deck = generateDeck(); // Refresh deck if empty
            }

            // Assign cards to hand
            Card card1 = deck.remove(0);
            Card card2 = deck.remove(0);
            hand.addCard(card1);
            hand.addCard(card2);

            // Save Hand & Assign it to Player
            handRepository.save(hand);
            player.setHand(hand);

            // Debugging
            System.out.println("Game started for Player ID: " + player.getId());
            System.out.println("Hand ID: " + hand.getId());
            System.out.println("Hand contains cards: " + hand.getCards().size());

            playerRepository.save(player);
        }
    }

    // Player takes another card (hit)
    public void hit(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            if (!deck.isEmpty()) {
                player.receiveCard(deck.remove(0));
            }
            playerRepository.save(player);
        }
    }

    // Check Game Status: "Blackjack!", "Bust!", "Continue Playing"
    public String checkGameStatus(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            int handValue = player.calculateHandValue();
            if (handValue == 21) return "Blackjack!";
            if (handValue > 21) return "Bust!";
            return "Continue playing.";
        }
        return "Player not found.";
    }

    // Get the player's hand value
    public int getPlayerHandValue(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            if (player.getHand() != null) {
                System.out.println("Hand ID: " + player.getHand().getId());  // Debugging
                System.out.println("Hand has cards: " + player.getHand().getCards().size());

                return player.getHand().getTotalValue();
            } else {
                System.out.println("Hand is NULL for player ID: " + playerId);
            }
        }
        throw new RuntimeException("Player not found.");
    }

    // Reset game (clear hand and refresh deck)
    public void resetGame(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            player.resetHand();
            deck = generateDeck();
            playerRepository.save(player);
        }
    }
}
