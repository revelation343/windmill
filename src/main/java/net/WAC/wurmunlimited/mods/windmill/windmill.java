package net.WAC.wurmunlimited.mods.windmill;

import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.creatures.Creature;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import java.util.Properties;
import org.gotti.wurmunlimited.modloader.interfaces.ItemTemplatesCreatedListener;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

public class windmill implements WurmServerMod, Configurable, ServerStartedListener, ItemTemplatesCreatedListener
{
    static int windmilltemplateid;
    static int sawmilltemplateid;
    static int masonsmilltemplateid;
    static int maxnums;
    static int windmillmaxnums;
    static int sawmillinitialtime;
    static int windmillinitialtime;
    static int masonsmillinitialtime;
    static boolean usewind;

    static {
        windmill.windmilltemplateid = 5557;
        windmill.sawmilltemplateid = 5558;
        windmill.masonsmilltemplateid = 5559;
        windmill.maxnums = 20;
        windmill.windmillmaxnums = 20;
        windmill.sawmillinitialtime = 10;
        windmill.windmillinitialtime = 10;
        windmill.masonsmillinitialtime = 10;
        windmill.usewind = true;
    }

    public String getVersion() {
        return "v1.4";
    }

    public void configure(final Properties properties) {
        windmill.usewind = Boolean.parseBoolean(properties.getProperty("usewind", Boolean.toString(windmill.usewind)));
        windmill.windmilltemplateid = Integer.parseInt(properties.getProperty("windmilltemplateid", Integer.toString(windmill.windmilltemplateid)));
        windmill.sawmilltemplateid = Integer.parseInt(properties.getProperty("sawmilltemplateid", Integer.toString(windmill.sawmilltemplateid)));
        windmill.masonsmilltemplateid = Integer.parseInt(properties.getProperty("masonsmilltemplateid", Integer.toString(windmill.masonsmilltemplateid)));
        windmill.maxnums = Integer.parseInt(properties.getProperty("maxnums", Integer.toString(windmill.maxnums)));
        windmill.windmillmaxnums = Integer.parseInt(properties.getProperty("windmillmaxnums", Integer.toString(windmill.windmillmaxnums)));
        windmill.sawmillinitialtime = Integer.parseInt(properties.getProperty("sawmillinitialtime", Integer.toString(windmill.sawmillinitialtime)));
        windmill.windmillinitialtime = Integer.parseInt(properties.getProperty("windmillinitialtime", Integer.toString(windmill.windmillinitialtime)));
        windmill.masonsmillinitialtime = Integer.parseInt(properties.getProperty("masonsmillinitialtime", Integer.toString(windmill.masonsmillinitialtime)));
    }

    public void onServerStarted() {
        ModActions.registerAction((ModAction)new deckboardaction());
        ModActions.registerAction((ModAction)new hullplankaction());
        ModActions.registerAction((ModAction)new plankaction());
        ModActions.registerAction((ModAction)new shafteaction());
        ModActions.registerAction((ModAction)new woodbeamaction());
        ModActions.registerAction((ModAction)new flouraction());
        ModActions.registerAction((ModAction)new brickaction());
        ModActions.registerAction((ModAction)new slabaction());
    }

    public void onItemTemplatesCreated() {
        new windmillitems();
    }

    static boolean sawmillItemCreate(final Creature performer, final Item item, final int templateProduce, final int templateConsume, final int weightconsume, int maxNums, final int maxItems, final String SoundName) {
        final Item[] currentItems = item.getAllItems(true);
        int produceTally = 0;
        int consumeTally = 0;
        Item[] array;
        for (int length = (array = currentItems).length, k = 0; k < length; ++k) {
            final Item i = array[k];
            if (templateProduce == i.getTemplateId()) {
                ++produceTally;
            }
            else if (templateConsume == i.getTemplateId()) {
                consumeTally += Math.abs(i.getFullWeight() / weightconsume);
            }
        }
        maxNums = Math.min(maxNums, consumeTally);
        if (produceTally >= maxItems) {
            return true;
        }
        int countcreated = 0;
        String createdname = "";
        if (templateConsume != 0) {
            consumeTally = Math.min(maxNums, consumeTally);
            boolean playsound = false;
            Item[] array2;
            for (int length2 = (array2 = currentItems).length, l = 0; l < length2; ++l) {
                final Item j = array2[l];
                if (consumeTally > 0 && j.getTemplateId() == templateConsume && j.getWeightGrams() >= weightconsume) {
                    try {
                        byte material = 0;
                        material = j.getMaterial();
                        Item toInsert;
                        if (material != 0) {
                            toInsert = ItemFactory.createItem(templateProduce, j.getQualityLevel(), material, j.getRarity(), (String)null);
                        }
                        else {
                            toInsert = ItemFactory.createItem(templateProduce, j.getQualityLevel(), j.getRarity(), (String)null);
                        }
                        if (j.getRarity() != 0) {
                            j.setRarity((byte)0);
                        }
                        playsound = true;
                        item.insertItem(toInsert, true);
                        ++countcreated;
                        createdname = toInsert.getName();
                        j.setWeight(j.getWeightGrams() - weightconsume, false);
                        if (100 >= j.getWeightGrams()) {
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
            if (countcreated > 1) {
                createdname = String.valueOf(createdname) + "s";
            }
            if (playsound) {
                performer.getCommunicator().sendSafeServerMessage("Created " + countcreated + " " + createdname);
                SoundPlayer.playSound(SoundName, item, 0.0f);
                return false;
            }
        }
        return true;
    }

    static boolean windmillItemCreate(final Creature performer, final Item item, final int templateProduce, final int templateConsume, final int templateConsume2, final int weightconsume, final int weightconsume2, int maxNums, final int maxItems, final String SoundName) {
        final Item[] currentItems = item.getAllItems(true);
        int produceTally = 0;
        int consumeTally = 0;
        int consumeTally2 = 0;
        int totaltally = 0;
        Item[] array;
        for (int length = (array = currentItems).length, k = 0; k < length; ++k) {
            final Item i = array[k];
            if (templateProduce == i.getTemplateId()) {
                ++produceTally;
            }
            else if (templateConsume == i.getTemplateId()) {
                consumeTally += Math.abs(i.getFullWeight() / weightconsume);
            }
            else if (templateConsume2 == i.getTemplateId()) {
                consumeTally2 += Math.abs(i.getFullWeight() / weightconsume2);
            }
        }
        totaltally = Math.min(consumeTally, consumeTally2);
        maxNums = Math.min(maxNums, totaltally);
        if (produceTally >= maxItems) {
            return true;
        }
        int countcreated = 0;
        String createdname = "";
        if (templateConsume != 0) {
            consumeTally = Math.min(maxNums, consumeTally);
            boolean playsound = false;
            Item[] array2;
            for (int length2 = (array2 = currentItems).length, l = 0; l < length2; ++l) {
                final Item j = array2[l];
                if (consumeTally > 0 && j.getTemplateId() == templateConsume && j.getWeightGrams() >= weightconsume) {
                    try {
                        byte material = 0;
                        material = j.getMaterial();
                        Item toInsert;
                        if (material != 0) {
                            toInsert = ItemFactory.createItem(templateProduce, j.getQualityLevel(), material, j.getRarity(), (String)null);
                        }
                        else {
                            toInsert = ItemFactory.createItem(templateProduce, j.getQualityLevel(), j.getRarity(), (String)null);
                        }
                        if (j.getRarity() != 0) {
                            j.setRarity((byte)0);
                        }
                        playsound = true;
                        item.insertItem(toInsert, true);
                        ++countcreated;
                        createdname = toInsert.getName();
                        j.setWeight(j.getWeightGrams() - weightconsume, false);
                        if (100 >= j.getWeightGrams()) {
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
            if (countcreated > 1) {
                createdname = String.valueOf(createdname) + "s";
            }
            if (playsound) {
                performer.getCommunicator().sendSafeServerMessage("Created " + countcreated + " " + createdname);
                SoundPlayer.playSound(SoundName, item, 0.0f);
                return false;
            }
        }
        return true;
    }

    static boolean masonsmillItemCreate(final Creature performer, final Item item, final int createitem, final int weightconsume, final int maxNums, final int maxItems, final String SoundName) {
        final Item[] currentItems = item.getAllItems(true);
        int consumeTally = maxNums;
        int countcreated = 0;
        String createdname = "";
        boolean playsound = false;
        Item[] array;
        for (int length = (array = currentItems).length, j = 0; j < length; ++j) {
            final Item i = array[j];
            int templateProduce = 0;
            if (consumeTally > 0 && i.getWeightGrams() >= weightconsume) {
                if (createitem == 0) {
                    createdname = "brick";
                    if (i.getTemplateId() == 770) {
                        templateProduce = 1123;
                    }
                    if (i.getTemplateId() == 785) {
                        templateProduce = 786;
                    }
                    if (i.getTemplateId() == 1116) {
                        templateProduce = 1121;
                    }
                    if (i.getTemplateId() == 146) {
                        templateProduce = 132;
                    }
                }
                else if (createitem == 1) {
                    createdname = "slab";
                    if (i.getTemplateId() == 770) {
                        templateProduce = 771;
                    }
                    if (i.getTemplateId() == 785) {
                        templateProduce = 787;
                    }
                    if (i.getTemplateId() == 1116) {
                        templateProduce = 1124;
                    }
                    if (i.getTemplateId() == 146) {
                        templateProduce = 406;
                    }
                }
                if (templateProduce != 0) {
                    try {
                        byte material = 0;
                        material = i.getMaterial();
                        Item toInsert;
                        if (material != 0) {
                            toInsert = ItemFactory.createItem(templateProduce, i.getQualityLevel(), material, i.getRarity(), (String)null);
                        }
                        else {
                            toInsert = ItemFactory.createItem(templateProduce, i.getQualityLevel(), i.getRarity(), (String)null);
                        }
                        if (i.getRarity() != 0) {
                            i.setRarity((byte)0);
                        }
                        playsound = true;
                        item.insertItem(toInsert, true);
                        ++countcreated;
                        i.setWeight(i.getWeightGrams() - weightconsume, false);
                        if (100 >= i.getWeightGrams()) {
                            Items.destroyItem(i.getWurmId());
                        }
                        --consumeTally;
                    }
                    catch (FailedException | NoSuchTemplateException ex2) {
                        final WurmServerException ex = null;
                        ex.printStackTrace();
                    }
                }
            }
        }
        if (countcreated > 1) {
            createdname = String.valueOf(createdname) + "s";
        }
        if (playsound) {
            performer.getCommunicator().sendSafeServerMessage("Created " + countcreated + " " + createdname);
            SoundPlayer.playSound(SoundName, item, 0.0f);
            return false;
        }
        return true;
    }
}