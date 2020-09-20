package com.asurax.modulehaviour;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.PetGearSupplier;
import com.github.manolo8.darkbot.gui.tree.components.JListField;

public class AutoSellConfig
{

    @Option("Ore list")
    public SellOres ORE_LIST;

    /*@Option(value = "Sell Module", description = "Select SELL PET-Module")
    @Editor(JListField.class) @Options(PetGearSupplier.class)*/
    public int SELL_MODULE;

    @Option(value = "Selling done PET-Module", description = "Select module to return to after pet done selling")
    @Editor(JListField.class) @Options(PetGearSupplier.class)
    public int SELLING_DONE_MODULE;

    @Option(value = "Selling config", description = "Select selling configuration and formation")
    public Config.ShipConfig SELL;

    @Option("PET sell cooldown (seconds)")
    @Num(min = 30, max = 360, step = 1)
    public int SELL_CD;

    public AutoSellConfig() {
        this.ORE_LIST = new SellOres();
        this.SELL_MODULE = PetGearSupplier.Gears.TRADER.getId();
        this.SELLING_DONE_MODULE = PetGearSupplier.Gears.PASSIVE.getId();
        this.SELL =  new Config.ShipConfig(1, '8');
        this.SELL_CD = 40;
    }

    public static class SellOres
    {
        @Option("Prometium")
        public boolean PROMETIUM;
        @Option("Endurium")
        public boolean ENDURIUM;
        @Option("Terbium")
        public boolean TERBIUM;
        @Option("Prometid")
        public boolean PROMETID;
        @Option("Duranium")
        public boolean DURANIUM;
        @Option("Promerium")
        public boolean PROMERIUM;
        @Option("Seprom")
        public boolean SEPROM;
        //@Option("Palladium")
        //public boolean PALLADIUM;
        //@Option("Osmium")
        //public boolean OSMIUM;

        public SellOres() {
            this.PROMETIUM = false;
            this.ENDURIUM = false;
            this.TERBIUM = false;
            this.PROMETID = false;
            this.DURANIUM = false;
            this.PROMERIUM = false;
            this.SEPROM = false;
            //this.PALLADIUM = false;
            //this.OSMIUM = false;
        }
    }

}
