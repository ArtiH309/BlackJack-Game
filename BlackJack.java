import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.util.ArrayList;

public class BlackJack{
    //card class
    private class Card{
        String value;
        String suit;

        Card(String value, String suit){ //card constructor
            this.value = value;
            this.suit = suit;
        }

        public String toString(){ //returns card as string
            return value + "-" + suit;
        }
        public int getValue(){ //gets integer value of a card
            if("AJQK".contains(value)){
                if(value == "A"){
                    return 11;
                }
                else{
                    return 10;
                }
            }
            return Integer.parseInt(value);
        }
        public boolean isAce(){ //check to see if a card is an ace
            return value == "A";
        }
        public String getImagePath(){ //gets image of the card
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck; //an arraylist of Card objects to represent the deck
    Random random = new Random();

    //Dealer
    Card faceDownCard; //the face down card of the dealer
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;
    int dealerShowing;

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    int cardWidth = 150; //1/1.4 ratio  
    int cardHeight = 210;

    int playerMoney = 2500;
    int currentBet = 0;
    int amtAfterBet = playerMoney;

    //window
    JFrame frame = new JFrame("BlackJack"); //frame for the game
    JPanel gamePanel = new JPanel(){ //the panel that has the game
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            try{ //drawing cards
                Image faceDownCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage(); //the img for the face down card
                if(!stayButton.isEnabled()){ //when user stays the face down card is revealed
                    faceDownCardImg = new ImageIcon(getClass().getResource(faceDownCard.getImagePath())).getImage();
                }
                g.drawImage(faceDownCardImg, 20, 20, cardWidth, cardHeight, null); //dealer face down
                //dealer face up card
                for(int i=0; i<dealerHand.size(); i++){
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null); //sets each subsequent card next to other
                }
                for(int i=0; i<playerHand.size(); i++){//player cards
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 20+(cardWidth+5)*i, 420, cardWidth, cardHeight, null);
                }
                //visuals
                g.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g.setColor(Color.white);
                g.drawString("Player Money: $" + playerMoney, 20, 390);
                g.drawString("Current Bet: $" + currentBet, 20, 410);
                g.drawString("Player Hand: " + playerSum, 20, 650);
                g.drawString("Dealer Showing: " + dealerShowing, 20, 250 );

                if(!stayButton.isEnabled()){ //if player stops drawing, checks result
                    checkGameResult(g);
                }

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    public void checkGameResult(Graphics g){
        dealerSum = reduceDealerAce();
        playerSum = reducePlayerAce();
        dealerShowing = dealerSum;
        //different win/lose/draw conditions
        String message = "";
        if(playerSum>21){
            message = "You Bust!";
            playerMoney -= currentBet;
        }
        else if(dealerSum>21 || playerSum>dealerSum){
            message = "You Win!";
            playerMoney += currentBet;
        }
        else if(playerSum == dealerSum){
            message = "Push";
        }
        else if(playerSum<dealerSum){
            message = "Dealer Wins!";
            playerMoney -= currentBet;
        }
        g.setFont(new Font("Segoe UI", Font.BOLD, 40));
        g.setColor(Color.white);
        g.drawString(message, 300, 320);

        continueButton.setEnabled(true);
    }

    JPanel buttonPanel = new JPanel(); //main game panel button objects
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton betButton = new JButton("Bet");
    JPanel betPanel = new JPanel(); //Betting panel button objects
    JButton confirmBetButton = new JButton("Confirm Bet");
    JButton continueButton = new JButton("Continue");
    JButton resetButton = new JButton("Reset");
    JButton resetBetButton = new JButton("Reset Bet");
    JButton backButton = new JButton("Back");
    JLabel playerAmount = new JLabel("Player Amount: " + Integer.toString(amtAfterBet)); //specifically during betting and the game to display money before result
    JLabel betAmount = new JLabel("Current Bet: " + Integer.toString(currentBet));

    //tokens for betting
    JButton token10 = new JButton();
    JButton token50 = new JButton();
    JButton token100 = new JButton();
    JButton token250 = new JButton();
    JButton token500 = new JButton();

    BlackJack(){
        startGame();
        frame.setVisible(true);
        frame.setSize(750, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //creating panel for game
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);
        //adding buttons to frame
        hitButton.setFocusable(false);
        hitButton.setPreferredSize(new Dimension(120, 40));
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        stayButton.setPreferredSize(new Dimension(120, 40));
        buttonPanel.add(stayButton);
        betButton.setFocusable(false);
        betButton.setPreferredSize(new Dimension(120, 40));
        buttonPanel.add(betButton);
        resetButton.setFocusable(false);
        resetButton.setPreferredSize(new Dimension(120, 40));
        buttonPanel.add(resetButton);
        continueButton.setFocusable(false);
        continueButton.setPreferredSize(new Dimension(120, 40));
        buttonPanel.add(continueButton);
        continueButton.setEnabled(false);
        //adding buttons to south of window
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {//button for hitting
            public void actionPerformed(ActionEvent e){
                hitAction();
            }
        });

        stayButton.addActionListener(new ActionListener() {//button for staying
            public void actionPerformed(ActionEvent e){
                stayAction();
            }
        });

        betButton.addActionListener(new ActionListener() { //betting button
            public void actionPerformed(ActionEvent e){
                betOptions();
            }
        });

        confirmBetButton.addActionListener(new ActionListener() { //button to confirm your bet
            public void actionPerformed(ActionEvent e){
                confirmBet();
            }
        });

        resetBetButton.addActionListener(new ActionListener() { //button to reset your bet 
            public void actionPerformed(ActionEvent e){
                resetBet();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                exitBetting();
            }
        });

        continueButton.addActionListener(new ActionListener() { //button to continue the game
            public void actionPerformed(ActionEvent e){
                continueGame();
            }
        });

        resetButton.addActionListener(new ActionListener() { //button to reset the whole game and start from scratch
            public void actionPerformed(ActionEvent e){ 
                resetGame();
            }
        });
        setupBetButtons();//adding the bet buttons only once, when the game is opened
    }
    public void hitAction(){ //function that performs the hit action
        Card card = deck.remove(deck.size()-1); //removes a card from deck and adds it to player's hand
        playerSum += card.getValue();
        playerAceCount += card.isAce() ? 1 : 0;
        playerHand.add(card);
        if(reducePlayerAce()>21){
            hitButton.setEnabled(false);
            dealerShowing = dealerSum;
            gamePanel.repaint();
        }
        if(playerSum>21){
            hitButton.setEnabled(false);
            stayButton.setEnabled(false);
            betButton.setEnabled(false);
            continueButton.setEnabled(true);
        }
        gamePanel.repaint();
    }
    public void stayAction(){ //function that performs the stay action
        hitButton.setEnabled(false); //makes all prep buttons unavailable as game decides a winner
        stayButton.setEnabled(false);
        betButton.setEnabled(false);
        while(dealerSum<17){//after staying, the dealer always hits under 17
            Card card = deck.remove(deck.size()-1);
            dealerSum += card.getValue();
            dealerAceCount += card.isAce() ? 1 : 0;
            dealerHand.add(card);  
        }
        dealerShowing = dealerSum;
        continueButton.setEnabled(true);
        gamePanel.repaint();
    }
    public void resetGame(){ //reset game function
        playerMoney = 2500; //all values back to default and game starts again
        currentBet = 0;
        amtAfterBet = 2500;
        playerAmount.setText("Player Amount: " + amtAfterBet);
        betAmount.setText("Current Bet: " + currentBet);
        gamePanel.repaint();
        startGame();
        System.out.println("RESET GAME: amtafterbet, currentbet, playermoney: " + " "+amtAfterBet + " "+currentBet + " "+playerMoney);
    }
    public void continueGame(){ //function to continue the game
        currentBet = 0; //values get updated and game starts again
        amtAfterBet = playerMoney;
        playerAmount.setText("Player Amount: " + amtAfterBet);
        betAmount.setText("Current Bet: " + currentBet);
        frame.revalidate();
        frame.repaint();
        gamePanel.revalidate();
        gamePanel.repaint();
        continueButton.setEnabled(false);
        startGame();
        System.out.println("GAME CONTINUED: amtafterbet, currentbet, playermoney: " + amtAfterBet + " "+currentBet + " "+playerMoney);
    }

    public void betOptions(){ //betting options
        gamePanel.setVisible(false);
        buttonPanel.setVisible(false);
        setupBetPanel();
        frame.add(betPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
    public void confirmBet(){//confirming the bet
        if(currentBet>0){
            hitButton.setEnabled(true);
            stayButton.setEnabled(true);
            frame.remove(betPanel);
            gamePanel.setVisible(true);
            buttonPanel.setVisible(true);
            frame.revalidate();
            frame.repaint();
            System.out.println("CONFIRM BET: amtafterbet, currentbet, playermoney: " + amtAfterBet + " "+currentBet + " "+playerMoney);
        }
    }

    public void exitBetting(){ //pressing the back button to exit betting
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        frame.remove(betPanel);
        gamePanel.setVisible(true);
        buttonPanel.setVisible(true);
        frame.revalidate();
        frame.repaint();
    }

    public void setupBetButtons(){//setting up the actions performed by the buttons
        token10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                placeBet(10);
            }
        });
        token50.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                placeBet(50);
            }
        });
        token100.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                placeBet(100);
            }
        });
        token250.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                placeBet(250);
            }
        });
        token500.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                placeBet(500);
            }
        });
    }
    public void setupBetPanel(){ //setting up the betpanel's visuals and buttons
        betPanel.setLayout(new GridBagLayout());
        betPanel.setBackground(new Color(53, 101, 77));
        betPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 3, 10, 3); //adding space between the tokens
        gbc.gridx = 0;
        gbc.gridy = 0;
        //token buttons
        token10.setIcon(new ImageIcon(getClass().getResource("./tokens/10Token.png")));
        token10.setContentAreaFilled(false);
        token10.setBorderPainted(false);
        betPanel.add(token10, gbc);

        gbc.gridx = 1;
        token50.setIcon(new ImageIcon(getClass().getResource("./tokens/50Token.png")));
        token50.setContentAreaFilled(false);
        token50.setBorderPainted(false);
        betPanel.add(token50, gbc);

        gbc.gridx = 2;
        token100.setIcon(new ImageIcon(getClass().getResource("./tokens/100Token.png")));
        token100.setContentAreaFilled(false);
        token100.setBorderPainted(false);
        betPanel.add(token100, gbc);

        gbc.gridx = 3;
        token250.setIcon(new ImageIcon(getClass().getResource("./tokens/250Token.png")));
        token250.setContentAreaFilled(false);
        token250.setBorderPainted(false);
        betPanel.add(token250, gbc);

        gbc.gridx = 4;
        token500.setIcon(new ImageIcon(getClass().getResource("./tokens/500Token.png")));
        token500.setContentAreaFilled(false);
        token500.setBorderPainted(false);
        betPanel.add(token500, gbc);

        Font font = new Font("Segoe UI", Font.BOLD, 30);
        playerAmount.setFont(font);
        playerAmount.setForeground(Color.WHITE);
        betAmount.setFont(font);
        betAmount.setForeground(Color.WHITE);

        //labels and buttons under tokens
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        betPanel.add(betAmount, gbc);

        gbc.gridy = 2;
        betPanel.add(playerAmount, gbc);

        gbc.gridy = 3;
        JPanel actionButtons = new JPanel(new FlowLayout());
        actionButtons.setBackground(new Color(53, 101, 77));
        confirmBetButton.setPreferredSize(new Dimension(120, 40));
        actionButtons.add(confirmBetButton);
        resetBetButton.setPreferredSize(new Dimension(120, 40));
        actionButtons.add(resetBetButton);
        backButton.setPreferredSize(new Dimension(120, 40));
        actionButtons.add(backButton);
        betPanel.add(actionButtons, gbc);
    }

    public void placeBet(int amount){//placing a bet
        if(playerMoney>=currentBet + amount){
            currentBet += amount;
            amtAfterBet = playerMoney-currentBet;
            playerAmount.setText("Player Amount: " + amtAfterBet);
            betAmount.setText("Current Bet: " + currentBet);
            System.out.println("PLACE BET: amtafterbet, currentbet, playermoney: " + amtAfterBet +" "+ currentBet +" "+ playerMoney);
        }
        else{
            JOptionPane.showMessageDialog(frame, "Invalid Bet, not enough money");
        }
    }
    public void resetBet(){ //resetting a bet
        currentBet = 0;
        amtAfterBet = playerMoney;
        playerAmount.setText("Player Amount: " + amtAfterBet);
        betAmount.setText("Current Bet: " + currentBet);
        System.out.println("RESET BET: amtafterbet, currentbet, playermoney: " + amtAfterBet + " "+ currentBet + " "+ playerMoney);
    }

    public void startGame(){ //function to start the game
        buildDeck();
        shuffleDeck();
        frame.revalidate();
        frame.repaint();
        gamePanel.revalidate();
        gamePanel.repaint();

        //dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        faceDownCard = deck.remove(deck.size()-1);
        dealerSum += faceDownCard.getValue();
        dealerAceCount += faceDownCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerShowing = card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);
        
        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for(int i=0; i<2; i++){ //loop to add cards to player hand
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        currentBet = 0;
        amtAfterBet = playerMoney;
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        betButton.setEnabled(true);

    }

    public void buildDeck(){ //building deck function
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"C", "D", "H", "S"}; //all possible card values and suits

        for(int i=0; i < suits.length; i++){ //loop to iterate through suits
            for(int j=0; j < values.length; j++){ //loop to iterate through values
                Card card = new Card(values[j], suits[i]); //creates a card with each suit, and value
                deck.add(card); //adds card to the deck
            }
        }
    }

    public void shuffleDeck(){ //shuffling the deck
        for(int i=0; i<deck.size(); i++){
            int j = random.nextInt(deck.size());
            Card currentCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currentCard);
        }
    }
    public int reducePlayerAce(){
        while(playerSum>21 && playerAceCount>0){ //if player busts with an ace in hand
            playerSum -= 10; //makes the playerSum 10 less
            playerAceCount -= 1; //makes an ace from 11 to 1, one less ace
        }
        return playerSum;
    }
    public int reduceDealerAce(){
        while(dealerSum>21 && dealerAceCount>0){ //if dealer busts with an ace in hand
            dealerSum -= 10; //makes the dealerSum 10 less
            dealerAceCount -= 1; //makes an ace from 11 to 1, one less ace
        }
        return dealerSum;
    }
}
