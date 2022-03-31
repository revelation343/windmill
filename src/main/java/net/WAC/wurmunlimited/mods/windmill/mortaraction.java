package net.WAC.wurmunlimited.mods.windmill;

import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.ItemFactory;
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

public class mortaraction implements WurmServerMod, ItemTypes, MiscConstants, ModAction, BehaviourProvider, ActionPerformer
{
    public static short actionId;
    static ActionEntry actionEntry;

    public mortaraction() {
        mortaraction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(mortaraction.actionEntry = ActionEntry.createEntry(mortaraction.actionId, "Create mortar", "Creating mortar", new int[0]));
    }

    public BehaviourProvider getBehaviourProvider() {
        return (BehaviourProvider)this;
    }

    public ActionPerformer getActionPerformer() {
        return (ActionPerformer)this;
    }

    public short getActionId() {
        return mortaraction.actionId;
    }

    public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item target) {
        return this.getBehavioursFor(performer, target);
    }

    public List<ActionEntry> getBehavioursFor(final Creature performer, final Item target) {
        if (performer instanceof Player && target.getTemplateId() == windmill.windmilltemplateid) {
            return Arrays.asList(mortaraction.actionEntry);
        }
        return null;
    }

    public boolean action(final Action act, final Creature performer, final Item source, final Item target, final short action, final float counter) {
        return this.action(act, performer, target, action, counter);
    }

    public boolean action(final Action act, final Creature performer, final Item target, final short action, final float counter) {
        final String actionstring = act.getActionEntry().getActionString().toLowerCase();
        final int actiontime = 1800;
        int tickTimes = windmill.windmillinitialtime;
        int absolutewindpower = (int)(10.0f * Math.abs(Server.getWeather().getWindPower()));
        if (!windmill.usewind) {
            absolutewindpower = 0;
        }
        tickTimes -= absolutewindpower;
        if (target.getTemplateId() == windmill.windmilltemplateid) {
            if (counter == 1.0f) {
                performer.sendActionControl(actionstring, true, actiontime);
                performer.getCommunicator().sendNormalServerMessage("You start to create mortar.");
            }
            if (act.currentSecond() % tickTimes == 0) {
                if (performer.getStatus().getStamina() < 5000) {
                    performer.getCommunicator().sendNormalServerMessage("You must rest.");
                    return true;
                }
                performer.getStatus().modifyStamina(-2000.0f);
                final Item[] currentItems = target.getAllItems(true);
                int consumeTally = 0;
                int produceTally = 0;
                Item[] array;
                for (int length = (array = currentItems).length, k = 0; k < length; ++k) {
                    final Item i = array[k];
                    if (28 <= i.getTemplateId() && i.getTemplateId() <= 31) {
                        if (201 == i.getTemplateId()) {
                            ++produceTally;
                        }
                        else if (28 <= i.getTemplateId() && i.getTemplateId() <= 31) {
                            ++consumeTally;
                        }
                    }
                }
                if (produceTally >= 20) {
                    return true;
                }
                int countcreated = 0;
                String createdname = "";
                consumeTally = Math.min(windmill.windmillmaxnums, consumeTally);
                if (consumeTally != 0) {
                    boolean playsound = false;
                    Item[] array2;
                    for (int length2 = (array2 = currentItems).length, l = 0; l < length2; ++l) {
                        final Item j = array2[l];
                        if (consumeTally > 0 && 28 <= j.getTemplateId() && j.getTemplateId() <= 31 && j.getWeightGrams() >= 300) {
                            try {
                                byte material = 0;
                                material = j.getMaterial();
                                Item toInsert;
                                if (material != 0) {
                                    toInsert = ItemFactory.createItem(201, j.getQualityLevel(), material, j.getRarity(), (String)null);
                                }
                                else {
                                    toInsert = ItemFactory.createItem(201, j.getQualityLevel(), j.getRarity(), (String)null);
                                }
                                if (j.getRarity() != 0) {
                                    j.setRarity((byte)0);
                                }
                                playsound = true;
                                target.insertItem(toInsert, true);
                                ++countcreated;
                                createdname = toInsert.getName();
                                j.setWeight(j.getWeightGrams() - 300, false);
                                if (100 > j.getWeightGrams()) {
                                    Items.destroyItem(j.getWurmId());
                                }
                                --consumeTally;
                            }
                            catch (FailedException | NoSuchTemplateException ex2) {
                                final WurmServerException ex = null;
                                ex.printStackTrace();
                            }
                        }
                    }
                    if (playsound) {
                        performer.getCommunicator().sendSafeServerMessage("Created " + countcreated + " " + createdname);
                        SoundPlayer.playSound("sound.grindstone", target, 0.0f);
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}