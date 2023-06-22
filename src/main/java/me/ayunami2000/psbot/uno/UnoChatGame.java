package me.ayunami2000.psbot.uno;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Nonnull;

import com.github.markozajc.juno.decks.impl.UnoStandardDeck;
import com.github.markozajc.juno.game.UnoControlledGame;
import com.github.markozajc.juno.players.impl.UnoStrategicPlayer;
import com.github.markozajc.juno.rules.pack.UnoRulePack;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules.UnoHouseRule;
import me.ayunami2000.psbot.PsBot;

public class UnoChatGame extends UnoControlledGame {

    @SuppressWarnings({
            "resource", "null"
    })
    @Nonnull
    private static UnoRulePack getRulePack() {
        List<UnoHouseRule> rules = new ArrayList<>();
        Scanner s = new Scanner(System.in);
        for (UnoHouseRule rule : UnoHouseRule.values()) {
            PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(8,rule.getName()));
            String cmd="";
            while((cmd=PsBot.unoThread.readCommand()).equalsIgnoreCase("")){
                try {Thread.sleep(1);} catch (InterruptedException e) {}
            }
            if (cmd.equalsIgnoreCase("y"))
                rules.add(rule);
        }

        return UnoOfficialRules.getPack(rules.toArray(new UnoHouseRule[rules.size()]));
    }


    public UnoChatGame() {
        super(new UnoChatPlayer("You"), new UnoStrategicPlayer("COMPUTER"),
                UnoStandardDeck.getDeck(), 7, getRulePack());
    }

    @Override
    public void onEvent(String format, Object... arguments) {
        //note: might include unnecessary newlines, todo concat into string then send?
        PsBot.unoThread.unoMessage(PsBot.unoThread.getMessage(9,String.format(format,arguments)));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
