package com.ebenfuentes.blackjack.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ebenfuentes.blackjack.model.Card;
import com.ebenfuentes.blackjack.model.Player;
import com.ebenfuentes.blackjack.repository.PlayerRepository;

@Service
public class GameService {
    private final PlayerRepository playerRepository;
    private List<Card> deck;

    public GameService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
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

    // Start a new game (reset hand & deal two cards)
    public void startGame(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            player.resetHand();

            if (deck.size() < 2) {
                deck = generateDeck(); // Refresh deck if empty
            }

            player.receiveCard(deck.remove(0));
            player.receiveCard(deck.remove(0));

            playerRepository.save(player);
        }
    }

    // Player takes another card
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

    // Check game status (Blackjack, Bust, Continue)
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

    // Reset game (clear player hand, refresh deck)
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
