package me.ayunami2000.psbot.uno;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.github.markozajc.juno.cards.UnoCard;
import com.github.markozajc.juno.cards.UnoCardColor;
import com.github.markozajc.juno.cards.impl.UnoDrawCard;
import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.rules.pack.impl.house.UnoProgressiveRulePack;
import com.github.markozajc.juno.utils.UnoRuleUtils;
import me.ayunami2000.psbot.PsBot;

public class UnoChatPlayer extends UnoPlayer {

    private static String INVALID_CHOICE_STRING = PsBot.unoThread.getMessage(10);

    public UnoChatPlayer(@Nonnull String name) {
        super(name);
        INVALID_CHOICE_STRING = PsBot.unoThread.getMessage(10);
    }

    @SuppressWarnings("null")
    @Override
    public UnoCard playCard(UnoGame game, UnoPlayer next) {
        UnoCard top = game.getDiscard().getTop();
        List<UnoCard> possible = UnoRuleUtils.combinedPlacementAnalysis(top, this.getHand().getCards(), game.getRules(),
                this.getHand());

        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(11,next.getName(),next.getHand().getSize(),game.getDraw().getSize(),game.getDiscard().getSize(),game.getDiscard().getTop()));

        List<UnoDrawCard> drawCards = UnoProgressiveRulePack.getConsecutive(game.getDiscard());
        if (!drawCards.isEmpty()) {
            PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(12,drawCards.size() * drawCards.get(0).getAmount(),drawCards.size(),top + (drawCards.size() == 1 ? "" : "s")));
        } else {
            PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(13));
        }

        int i = 1;
        for (UnoCard card : this.getHand().getCards()) {
            if (possible.contains(card)) {
                PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(14,i,card));
            } else {
                PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(15,i,card));
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            i++;
        }

        while (true) {
            String cmd="";
            while((cmd=PsBot.unoThread.readCommand()).equalsIgnoreCase("")){
                try {Thread.sleep(1);} catch (InterruptedException e) {}
            }
            String nextLine = cmd;
            if (nextLine.equalsIgnoreCase("rules")) {
                PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(16,game.getRules()
                        .getRules()
                        .stream()
                        .map(r -> r.getClass().getSimpleName())
                        .collect(Collectors.joining(", "))));
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(nextLine);
            } catch (NumberFormatException e) {
                PsBot.unoThread.unoMessage(INVALID_CHOICE_STRING);
                continue;
            }

            if (choice == 0)
                return null;

            if (choice > this.getCards().size()) {
                PsBot.unoThread.unoMessage(INVALID_CHOICE_STRING);
                continue;
            }

            UnoCard card = this.getCards().get(choice - 1);

            if (!possible.contains(card)) {
                PsBot.unoThread.unoMessage(INVALID_CHOICE_STRING);
                continue;
            }

            return card;
        }
    }

    @Override
    public UnoCardColor chooseColor(UnoGame game) {
        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(17));

        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(18));
        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(19));
        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(20));
        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(21));

        while (true) {
            int choice;
            try {
                String cmd="";
                while((cmd=PsBot.unoThread.readCommand()).equalsIgnoreCase("")){
                    try {Thread.sleep(1);} catch (InterruptedException e) {}
                }
                choice = Integer.parseInt(cmd);
            } catch (NumberFormatException e) {
                PsBot.unoThread.unoMessage(INVALID_CHOICE_STRING);
                continue;
            }

            switch (choice) {
                case 0:
                    return UnoCardColor.YELLOW;
                case 1:
                    return UnoCardColor.RED;
                case 2:
                    return UnoCardColor.GREEN;
                case 3:
                    return UnoCardColor.BLUE;
                default:
                    break;
            }

            PsBot.unoThread.unoMessage(INVALID_CHOICE_STRING);
        }
    }

    @Override
    public boolean shouldPlayDrawnCard(UnoGame game, UnoCard drawnCard, UnoPlayer next) {
        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(22,drawnCard.toString()));
        String cmd="";
        while((cmd=PsBot.unoThread.readCommand()).equalsIgnoreCase("")){
            try {Thread.sleep(1);} catch (InterruptedException e) {}
        }
        return cmd.equalsIgnoreCase("y");
    }

}
