package net.WAC.wurmunlimited.mods.windmill;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import java.util.Arrays;
import com.wurmonline.server.players.Player;
import java.util.List;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.creatures.Creature;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import com.wurmonline.server.behaviours.ActionEntry;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTypes;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

public class deckboardaction implements WurmServerMod, ItemTypes, MiscConstants, ModAction, BehaviourProvider, ActionPerformer
{
    public static short actionId;
    static ActionEntry actionEntry;

    public deckboardaction() {
        deckboardaction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(deckboardaction.actionEntry = ActionEntry.createEntry(deckboardaction.actionId, "Create deck boards", "Creating deck boards", new int[0]));
    }

    public BehaviourProvider getBehaviourProvider() {
        return (BehaviourProvider)this;
    }

    public ActionPerformer getActionPerformer() {
        return (ActionPerformer)this;
    }

    public short getActionId() {
        return deckboardaction.actionId;
    }

    public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item target) {
        return this.getBehavioursFor(performer, target);
    }

    public List<ActionEntry> getBehavioursFor(final Creature performer, final Item target) {
        if (performer instanceof Player && target.getTemplateId() == windmill.sawmilltemplateid) {
            return Arrays.asList(deckboardaction.actionEntry);
        }
        return null;
    }

    public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
        return this.action(act, performer, target, action, counter);
    }

    public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
        final String actionstring = act.getActionEntry().getActionString().toLowerCase();
        final int actiontime = 1800;
        int tickTimes = windmill.sawmillinitialtime;
        int absolutewindpower = (int)(10.0f * Math.abs(Server.getWeather().getWindPower()));
        if (!windmill.usewind) {
            absolutewindpower = 0;
        }
        tickTimes -= absolutewindpower;
        if (target.getTemplateId() == windmill.sawmilltemplateid) {
            if (counter == 1.0f) {
                performer.sendActionControl(actionstring, true, actiontime);
                performer.getCommunicator().sendNormalServerMessage("You start to create deck boards.");
            }
            if (act.currentSecond() % tickTimes == 0) {
                if (performer.getStatus().getStamina() < 5000) {
                    performer.getCommunicator().sendNormalServerMessage("You must rest.");
                    return true;
                }
                performer.getStatus().modifyStamina(-2000.0f);
                return windmill.sawmillItemCreate(performer, target, 566, 9, 4000, windmill.maxnums, 15000, "sound.work.carpentry.saw");
            }
        }
        return false;
    }
}