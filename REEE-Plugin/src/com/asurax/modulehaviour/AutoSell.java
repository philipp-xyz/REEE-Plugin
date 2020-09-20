package com.asurax.modulehaviour;

import com.asurax.utils.VerifierChecker;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.suppliers.PetGearSupplier;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Tickable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.TemporalModule;

import java.util.ArrayList;
import java.util.List;

@Feature(name = "AutoSell for PET", description = "Selling ores with PET-Module")
public class AutoSell extends TemporalModule implements Behaviour, Tickable, Configurable<AutoSellConfig> {

    private AutoSellConfig autoSellConfig;
    private Main main;
    private StatsManager statsManager;
    private OreTradeGui oreTrade;

    private long lastSell;
    private long tradeWait = 0;
    private int sellPosition = 0;

    private int prometium   = 48;
    private int endurium    = 130;
    private int terbium     = 211;
    private int prometid    = 288;
    private int duranium    = 369;
    private int promerium   = 450;
    private int seprom      = 526;
    private int y           = 179;

    private boolean moved = false;
    private boolean goingBack = false;

    public void install(Main main) {
        if (!VerifierChecker.getAuthApi().requireDonor()) {
            return;
        }
        super.install(main);
        this.main = main;
        this.statsManager = main.statsManager;
        this.oreTrade = main.guiManager.oreTrade;
    }

    public void setConfig(AutoSellConfig autoSellConfig) {
        this.autoSellConfig = autoSellConfig;
    }

    public boolean canRefresh() {
        return false;
    }

    public String status() {
        return "Selling Ores | OreTrade visible: " + oreTrade.visible + " | Ship moving: " + main.hero.locationInfo.isMoving();
    }

    @Override
    public void tickBehaviour() {
        if (this.statsManager.deposit >= this.statsManager.depositTotal && this.statsManager.depositTotal != 0 && System.currentTimeMillis() - autoSellConfig.SELL_CD * 1000L > lastSell) {
            if (this.main.module != this) {
                main.setModule(this);
            }
        }
    }

    @Override
    public void tickModule() {
        if (!moved) {
            main.hero.drive.move(main.hero.locationInfo.now.x + 100, main.hero.locationInfo.now.y + 100);
            moved = true;
        }

        if (!main.hero.locationInfo.isMoving()) {
            sell();
        }

        if (goingBack){
            goingBack = false;
            moved = false;
            goBack();
            //System.out.println("Module: " + this.main.module);
        }
    }

    public void tick() {
    }

    private void sell() {
        this.main.hero.setMode(autoSellConfig.SELL);

        main.config.PET.ENABLED = true;
        this.main.guiManager.pet.setEnabled(false);
        this.main.guiManager.pet.setEnabled(true);
        this.main.config.PET.MODULE_ID = autoSellConfig.SELL_MODULE;

//        System.out.println("Ship config " + this.main.hero.config);
//        System.out.println("Pet module " + this.main.config.PET.MODULE_ID);

        if (tradeWait == 0) {
            tradeWait = System.currentTimeMillis();
        }
        //System.out.println(System.currentTimeMillis() - tradeWait);
        if (this.main.guiManager.oreTrade.visible && oreTrade.isAnimationDone()) {
            if (System.currentTimeMillis() - tradeWait > 5500L) {
                List<Integer> sellOres = new ArrayList<Integer>();
                //System.out.println("sell list");
                if (autoSellConfig.ORE_LIST.PROMETIUM) {
                    sellOres.add(prometium);
                }
                if (autoSellConfig.ORE_LIST.ENDURIUM) {
                    sellOres.add(endurium);
                }
                if (autoSellConfig.ORE_LIST.TERBIUM) {
                    sellOres.add(terbium);
                }
                if (autoSellConfig.ORE_LIST.PROMETID) {
                    sellOres.add(prometid);
                }
                if (autoSellConfig.ORE_LIST.DURANIUM) {
                    sellOres.add(duranium);
                }
                if (autoSellConfig.ORE_LIST.PROMERIUM) {
                    sellOres.add(promerium);
                }
                if (autoSellConfig.ORE_LIST.SEPROM) {
                    sellOres.add(seprom);
                }
                System.out.println("sellPosition" + sellPosition);
                if (sellPosition % 15 == 0) {
//                    System.out.println(sellPosition);
//                    System.out.println(sellOres.get(sellPosition/15));
                    this.oreTrade.click(sellOres.get(sellPosition/15), y);
                    if (sellPosition/15 == sellOres.size() - 1) {
                        sellingDone();
                    }
                }
                sellPosition++;
            }
        }
    }

    private void sellingDone() {
        this.main.config.PET.MODULE_ID = PetGearSupplier.Gears.PASSIVE.getId()/*autoSellConfig.SELLING_DONE_MODULE*/;
        this.oreTrade.click(730, 9);
        //System.out.println("close oretrade");
        lastSell = System.currentTimeMillis();
        tradeWait = 0;
        //System.out.println("selling done");
        goingBack = true;
        sellPosition = -1;
    }
}
