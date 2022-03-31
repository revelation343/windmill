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

public class flouraction implements WurmServerMod, ItemTypes, MiscConstants, ModAction, BehaviourProvider, ActionPerformer
{
    public static short actionId;
    static ActionEntry actionEntry;

    public flouraction() {
        flouraction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(flouraction.actionEntry = ActionEntry.createEntry(flouraction.actionId, "Create flour", "Creating flour", new int[0]));
    }

    public BehaviourProvider getBehaviourProvider() {
        return (BehaviourProvider)this;
    }

    public ActionPerformer getActionPerformer() {
        return (ActionPerformer)this;
    }

    public short getActionId() {
        return flouraction.actionId;
    }

    public List<ActionEntry> getBehavioursFor(final Creature performer, final Item source, final Item target) {
        return this.getBehavioursFor(performer, target);
    }

    public List<ActionEntry> getBehavioursFor(final Creature performer, final Item target) {
        if (performer instanceof Player && target.getTemplateId() == windmill.windmilltemplateid) {
            return Arrays.asList(flouraction.actionEntry);
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
                performer.getCommunicator().sendNormalServerMessage("You start to create flour.");
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
                    if (28 <= i.getTemplateId() && i.getTemplateId() <= 32) {
                        if (201 == i.getTemplateId() || 1220 == i.getTemplateId()) {
                            ++produceTally;
                        }
                        else if (28 <= i.getTemplateId() && i.getTemplateId() <= 32) {
                            ++consumeTally;
                        }
                    }
                }
                if (produceTally >= 1000) {
                    return true;
                }
                int countcreated = 0;
                int cornflourcount = 0;
                String createdname = "";
                String cornflourcreatedname = "";
                consumeTally = Math.min(windmill.windmillmaxnums, consumeTally);
                if (consumeTally != 0) {
                    boolean playsound = false;
                    Item[] array2;
                    for (int length2 = (array2 = currentItems).length, l = 0; l < length2; ++l) {
                        final Item j = array2[l];
                        if (consumeTally > 0) {
                            if (28 <= j.getTemplateId() && j.getTemplateId() <= 31 && j.getWeightGrams() >= 300) {
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
                                catch (FailedException | NoSuchTemplateException ex3) {
                                    final WurmServerException ex = null;
                                    ex.printStackTrace();
                                }
                            }
                            if (j.getTemplateId() == 32 && j.getWeightGrams() >= 100) {
                                try {
                                    byte material = 0;
                                    material = j.getMaterial();
                                    Item toInsert;
                                    if (material != 0) {
                                        toInsert = ItemFactory.createItem(1220, j.getQualityLevel(), material, j.getRarity(), (String)null);
                                    }
                                    else {
                                        toInsert = ItemFactory.createItem(1220, j.getQualityLevel(), j.getRarity(), (String)null);
                                    }
                                    if (j.getRarity() != 0) {
                                        j.setRarity((byte)0);
                                    }
                                    playsound = true;
                                    target.insertItem(toInsert, true);
                                    ++cornflourcount;
                                    cornflourcreatedname = toInsert.getName();
                                    j.setWeight(j.getWeightGrams() - 100, false);
                                    if (100 > j.getWeightGrams()) {
                                        Items.destroyItem(j.getWurmId());
                                    }
                                    --consumeTally;
                                }
                                catch (FailedException | NoSuchTemplateException ex4) {
                                    final WurmServerException ex2 = null;
                                    ex2.printStackTrace();
                                }
                            }
                        }
                    }
                    if (playsound) {
                        if (countcreated != 0) {
                            performer.getCommunicator().sendSafeServerMessage("Created " + countcreated + " " + createdname);
                        }
                        if (cornflourcount != 0) {
                            performer.getCommunicator().sendSafeServerMessage("Created " + cornflourcount + " " + cornflourcreatedname);
                        }
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