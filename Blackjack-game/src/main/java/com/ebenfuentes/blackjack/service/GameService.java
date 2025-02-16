package com.ebenfuentes.blackjack.service;

import java.util.*;

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

    // ✅ Player places a bet
    public void placeBet(int playerId, int amount) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            // ✅ Prevent betting if the game is already in session
            if (player.isGameStarted()) {
                throw new IllegalStateException("Cannot place a bet while the game is in session.");
            }

            // ✅ Allow betting only if game has NOT started
            player.placeBet(amount);
            playerRepository.save(player);
        } else {
            throw new RuntimeException("Player not found.");
        }
    }


    // ✅ Start game (deal cards after bet)
    public void startGame(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            // ✅ Prevent starting if game is already in session
            if (player.isGameStarted()) {
                throw new IllegalStateException("Game already started! Cannot restart without resetting.");
            }

            // ✅ Prevent starting if no bet is placed
            if (player.getBet() == 0) {
                throw new IllegalStateException("Cannot start game without placing a bet!");
            }

            // ✅ Clear previous hands
            Hand hand = player.getHand();
            Hand dealerHand = player.getDealerHand();
            hand.clear();
            dealerHand.clear();
            player.setHand(hand);
            player.setDealerHand(dealerHand);

            // ✅ Ensure deck is full before dealing
            if (deck.size() < 4) {
                deck = generateDeck();
            }

            // ✅ Deal cards in correct order (Player → Dealer Face-Up → Player → Dealer Face-Down)
            hand.addCard(deck.remove(0));
            dealerHand.addCard(deck.remove(0)); // Face-Up
            hand.addCard(deck.remove(0));
            dealerHand.addCard(deck.remove(0)); // Face-Down

            // ✅ Mark game as started
            player.setGameStarted(true);

            // ✅ Save updates
            handRepository.save(hand);
            handRepository.save(dealerHand);
            playerRepository.save(player);
        }
    }


    // ✅ Player hits
    public Map<String, Object> hit(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Hand playerHand = player.getHand();
            Hand dealerHand = player.getDealerHand();

            // ✅ Player receives a new card
            Card newCard = null;
            if (!deck.isEmpty()) {
                newCard = deck.remove(0);
                player.receiveCard(newCard);
            }

            playerRepository.save(player);

            // ✅ Prepare response with updated game state
            Map<String, Object> response = new HashMap<>();

            // ✅ Include player's hand details
            List<Map<String, String>> playerCardsList = new ArrayList<>();
            for (Card card : playerHand.getCards()) {
                Map<String, String> cardDetails = new HashMap<>();
                cardDetails.put("rank", card.getRank());
                cardDetails.put("suit", card.getSuit());
                playerCardsList.add(cardDetails);
            }
            response.put("playerCards", playerCardsList);
            response.put("handValue", playerHand.getTotalValue());

            // ✅ Include bet amount
            response.put("bet", player.getBet());

            // ✅ Include dealer's face-up card & value
            if (dealerHand != null && !dealerHand.getCards().isEmpty()) {
                Card dealerFaceUpCard = dealerHand.getCards().get(0);
                Map<String, String> dealerCardDetails = new HashMap<>();
                dealerCardDetails.put("rank", dealerFaceUpCard.getRank());
                dealerCardDetails.put("suit", dealerFaceUpCard.getSuit());
                response.put("dealerFaceUpCard", dealerCardDetails);
                response.put("dealerHandValue", dealerFaceUpCard.getValue()); // ✅ Only show face-up card value
            } else {
                response.put("dealerFaceUpCard", "Unknown");
                response.put("dealerHandValue", 0);
            }

            // ✅ If the player busts, auto-end game & return result
            if (playerHand.getTotalValue() > 21) {
                response.put("status", "Bust! Dealer wins.");
                Map<String, Object> result = evaluateGame(player);
                response.putAll(result);
                player.setGameStarted(false); // ✅ Mark game as over
                playerRepository.save(player);
            } else {
                response.put("status", "Continue playing.");
            }

            return response;
        }
        throw new RuntimeException("Player not found.");
    }

    
    // ✅ Player stands (dealer reveals their hand)
    public Map<String, Object> stand(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Hand dealerHand = player.getDealerHand();

            // ✅ Dealer draws until reaching 17+
            while (dealerHand.getTotalValue() < 17 && !deck.isEmpty()) {
                dealerHand.addCard(deck.remove(0));
            }

            handRepository.save(dealerHand);

            // ✅ Evaluate game results
            Map<String, Object> result = evaluateGame(player);

            // ✅ Mark game as over, but do NOT reset yet
            player.setGameStarted(false);
            playerRepository.save(player);

            return result;
        }
        throw new RuntimeException("Player not found.");
    }

    // ✅ Player doubles down (double bet & get 1 more card)
    public Map<String, Object> doubleDown(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            int betAmount = player.getBet();

            // ✅ Ensure player has enough balance to double down
            if (betAmount <= player.getBalance()) {
                player.placeBet(betAmount); // Double the bet

                // ✅ Player receives one last card
                Card lastCard = null;
                if (!deck.isEmpty()) {
                    lastCard = deck.remove(0);
                    player.receiveCard(lastCard);
                }

                playerRepository.save(player);

                // ✅ Dealer now plays automatically after double down
                Map<String, Object> result = stand(playerId);

                // ✅ Include last card dealt to player
                if (lastCard != null) {
                    Map<String, String> lastCardDetails = new HashMap<>();
                    lastCardDetails.put("rank", lastCard.getRank());
                    lastCardDetails.put("suit", lastCard.getSuit());
                    result.put("lastCardDealt", lastCardDetails);
                }

                return result;
            } else {
                throw new IllegalArgumentException("Insufficient funds to double down.");
            }
        }
        throw new RuntimeException("Player not found.");
    }



    // ✅ Player splits (if possible)
    public void split(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Hand hand = player.getHand();

            if (hand.getCards().size() == 2 && hand.getCards().get(0).getRank().equals(hand.getCards().get(1).getRank())) {
                Card firstCard = hand.getCards().get(0);
                Card secondCard = hand.getCards().get(1);

                Hand splitHand1 = new Hand();
                splitHand1.addCard(firstCard);
                splitHand1.addCard(deck.remove(0));

                Hand splitHand2 = new Hand();
                splitHand2.addCard(secondCard);
                splitHand2.addCard(deck.remove(0));

                handRepository.save(splitHand1);
                handRepository.save(splitHand2);
                player.setHand(splitHand1);

                playerRepository.save(player);
            } else {
                throw new IllegalArgumentException("Cannot split this hand.");
            }
        }
    }

    // ✅ Check Game Status
    public Map<String, Object> checkGameStatus(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Hand playerHand = player.getHand();
            Hand dealerHand = player.getDealerHand();

            Map<String, Object> response = new HashMap<>();
            response.put("playerBalance", player.getBalance()); // ✅ Include balance

            // ✅ If game has NOT started, return "Game not in session."
            if (!player.isGameStarted()) {
                response.put("status", "Game not in session.");
                return response;
            }

            // ✅ Include current hand values (but don't evaluate game outcome)
            response.put("playerHandValue", playerHand.getTotalValue());
            response.put("dealerHandValue", dealerHand.getTotalValue());
            response.put("status", "Game in progress."); // ✅ Show game is active

            return response;
        }
        throw new RuntimeException("Player not found.");
    }



    // ✅ Reset Game
    public void resetGame(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            player.resetHand();
            deck = generateDeck();
            player.setGameStarted(false);
            playerRepository.save(player);
        }
    }
    
    public Player createPlayer(String username, int balance) {
        Player player = new Player(username, balance);
        return playerRepository.save(player);
    }
    
    public Map<String, Object> getPlayerHandDetails(int playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);

        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Map<String, Object> response = new HashMap<>();

            // ✅ Player's Hand Details
            if (player.getHand() != null) {	
                response.put("handValue", player.getHand().getTotalValue());

                List<Map<String, String>> playerCardsList = new ArrayList<>();
                for (Card card : player.getHand().getCards()) {
                    Map<String, String> cardDetails = new HashMap<>();
                    cardDetails.put("rank", card.getRank());
                    cardDetails.put("suit", card.getSuit());
                    playerCardsList.add(cardDetails);
                }
                response.put("playerCards", playerCardsList);
                response.put("bet", player.getBet());
            }

            // ✅ Dealer's Hand Details (Different behavior before & after player stands)
            Hand dealerHand = player.getDealerHand();
            if (dealerHand != null && !dealerHand.getCards().isEmpty()) {
                List<Map<String, String>> dealerCardsList = new ArrayList<>();
                boolean isPlayerStanding = isPlayerStanding(player); // 🔥 FIXED ERROR

                if (isPlayerStanding) {
                    // 🚀 Player stood, reveal ALL dealer cards
                    for (Card card : dealerHand.getCards()) {
                        Map<String, String> cardDetails = new HashMap<>();
                        cardDetails.put("rank", card.getRank());
                        cardDetails.put("suit", card.getSuit());
                        dealerCardsList.add(cardDetails);
                    }
                    response.put("dealerCards", dealerCardsList);
                    response.put("dealerHandValue", dealerHand.getTotalValue());
                } else {
                    // 🔒 Player hasn't stood yet, show only the first (face-up) card
                    Card dealerFaceUpCard = dealerHand.getCards().get(0);
                    Map<String, String> dealerCardDetails = new HashMap<>();
                    dealerCardDetails.put("rank", dealerFaceUpCard.getRank());
                    dealerCardDetails.put("suit", dealerFaceUpCard.getSuit());
                    response.put("dealerFaceUpCard", dealerCardDetails);
                    response.put("dealerHandValue", dealerHand.getCards().get(0).getValue());
                }
            } else {
                response.put("dealerFaceUpCard", "Unknown"); // Fallback case
            }

            return response;
        }

        throw new RuntimeException("Player not found.");
    }
    

 // ✅ Determine winner & include bet amount in the response
    private Map<String, Object> evaluateGame(Player player) {
        Map<String, Object> result = new HashMap<>();
        int playerValue = player.getHand().getTotalValue();
        int dealerValue = player.getDealerHand().getTotalValue();
        int betAmount = player.getBet();
        int winnings = 0; // Stores how much the player wins or loses

        // ✅ Move playerValue & dealerValue to the top for readability
        result.put("playerValue", playerValue);
        result.put("dealerValue", dealerValue);

        // ✅ Include the final hands (Player & Dealer)
        List<Map<String, String>> playerCardsList = new ArrayList<>();
        for (Card card : player.getHand().getCards()) {
            Map<String, String> cardDetails = new HashMap<>();
            cardDetails.put("rank", card.getRank());
            cardDetails.put("suit", card.getSuit());
            playerCardsList.add(cardDetails);
        }
        result.put("playerHand", playerCardsList);

        List<Map<String, String>> dealerCardsList = new ArrayList<>();
        for (Card card : player.getDealerHand().getCards()) {
            Map<String, String> cardDetails = new HashMap<>();
            cardDetails.put("rank", card.getRank());
            cardDetails.put("suit", card.getSuit());
            dealerCardsList.add(cardDetails);
        }
        result.put("dealerHand", dealerCardsList);

        // ✅ Determine the outcome
        if (playerValue > 21) {
            result.put("winner", "Dealer");
            result.put("message", "Player busted!");
            player.loseBet();
            winnings = -betAmount;
        } else if (dealerValue > 21 || playerValue > dealerValue) {
            result.put("winner", "Player");
            result.put("message", "Player wins!");
            player.winBet();
            winnings = betAmount;
        } else if (dealerValue > playerValue) {
            result.put("winner", "Dealer");
            result.put("message", "Dealer wins.");
            player.loseBet();
            winnings = -betAmount;
        } else {
            result.put("winner", "Tie");
            result.put("message", "It's a push!");
            player.push();
            winnings = 0; // No change in balance
        }

        // ✅ Keep bet details grouped together
        result.put("betAmount", betAmount);
        result.put("winnings", winnings);
        result.put("playerNewBalance", player.getBalance());

        playerRepository.save(player); // Save new balance
        return result;
    }



    
    private boolean isPlayerStanding(Player player) {
        return player.hasStood();
    }




}
