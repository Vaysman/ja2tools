/* 
 * The MIT License
 *
 * Copyright 2017 the_bob.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package thebob.assetmanager.managers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ja2.xml.ammo.calibers.AMMO;
import ja2.xml.ammo.types.AMMOTYPELIST;
import ja2.xml.items.ITEMLIST;
import ja2.xml.items.ITEMTYPE;
import ja2.xml.items.armor.ARMOURLIST;
import ja2.xml.items.drugs.DRUGSLIST.DRUG;
import ja2.xml.items.explosives.EXPLOSIVELIST;
import ja2.xml.items.food.FOODSLIST.FOOD;
import ja2.xml.items.lbe.LOADBEARINGEQUIPMENTLIST;
import ja2.xml.items.magazines.MAGAZINELIST;
import ja2.xml.items.weapons.WEAPONLIST;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import thebob.assetloader.sti.StiLoader;
import thebob.assetloader.xml.XmlLoader;
import thebob.assetmanager.AssetManager;
import thebob.assetmanager.managers.items.Item;
import thebob.assetmanager.managers.items.categories.ItemCategoryModel;
import thebob.assetmanager.managers.items.categories.ItemClassEnum;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Ammo;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Armour;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Blunt_Weapon;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Bomb;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Face_Item;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Grenade;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Gun;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.ItemClassMap;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Key;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Kit;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Knife;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Launcher;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Load_Bearing_Equipment;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Medkit;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Misc;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Money;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Random_Item;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Tentacle;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Throwing_Knife;
import static thebob.assetmanager.managers.items.categories.ItemClassEnum.Thrown_Weapon;

/**
 *
 * @author the_bob
 */
public class ItemManager extends VFSContextBoundManager {

    // abstraction between xmlloader item impl
    // build internal item representation
    Map<Integer, Item> items = new HashMap<Integer, Item>();
    Map<Integer, StiLoader> imageCache = new HashMap<Integer, StiLoader>();
    ItemCategoryModel categories;
    public ItemManager(AssetManager context) {
	super(context);
    }

    public Map<Integer, Item> getItems() {
	return items;
    }

    public ItemCategoryModel getCategories() {
	return categories;
    }


    @Override
    public boolean init() {
	XmlLoader xml = context.getXml();
	if (xml == null) {
	    return false;
	}

	// loads STI files holding weapon images
	loadItemImages();

	ITEMLIST itemList = xml.getItems();
	if (itemList == null) {
	    return false;
	}
	for (ITEMTYPE itemDef : itemList.getITEM()) {
	    Item item = new Item(itemDef.getUiIndex(), itemDef.getSzItemName(), itemDef.getUsItemClass(), itemDef.getUbClassIndex(), itemDef.getUbGraphicType(), itemDef.getUbGraphicNum() , itemDef.getUbCoolness() );

	    item.setFoodType(itemDef.getFoodType());
	    item.setDrugType(itemDef.getDrugType());
	    item.setClothesType(itemDef.getClothestype());

	    items.put(item.getId(), item);
	}
	if (items.size() < 1) {
	    return false;
	}

	// loads additional item data based on their item class and item class id
	processItems();

	// builds categories and fills them with items
	categories = new ItemCategoryModel(xml, items);
	categories.buildCategoryTree();
	
	// System.out.println("thebob.assetmanager.managers.ItemManager.init() " + categories.getRootNode());

	return true;
    }

    protected void loadItemImages() {
	String stiName;
	for (int i = 0; i < 10; i++) {
	    if (i == 0) {
		stiName = "MDGUNS.sti";
	    } else {
		stiName = "MDP" + i + "ITEMS.sti";
	    }

	    String assetName = "\\INTERFACE\\" + stiName;
	    StiLoader asset = new StiLoader();
	    
	    // System.out.print("thebob.assetmanager.managers.ItemManager.loadItemImages(): loading " + assetName);
	    
	    ByteBuffer assetData = context.getVfs().getFile(assetName);
	    if (assetData != null) {
		asset.loadAsset(assetData);
		// System.out.println("\t->  payload=" + asset.getImageCount());
		imageCache.put(i, asset);
	    } else {
		// System.out.println("\t-> not found");
	    }
	}
    }

    private void processItems() {
	XmlLoader xml = context.getXml();

	Map<Integer, WEAPONLIST.WEAPON> weapons = new HashMap<Integer, WEAPONLIST.WEAPON>();
	for (WEAPONLIST.WEAPON weapon : xml.getWeapons().getWEAPON()) {
	    weapons.put(weapon.getUiIndex(), weapon);
	}

	Map<Integer, ARMOURLIST.ARMOUR> armors = new HashMap<Integer, ARMOURLIST.ARMOUR>();
	for (ARMOURLIST.ARMOUR weapon : xml.getArmours().getARMOUR()) {
	    armors.put(weapon.getUiIndex(), weapon);
	}

	Map<Integer, EXPLOSIVELIST.EXPLOSIVE> explosives = new HashMap<Integer, EXPLOSIVELIST.EXPLOSIVE>();
	for (EXPLOSIVELIST.EXPLOSIVE weapon : xml.getExplosives().getEXPLOSIVE()) {
	    explosives.put((int) weapon.getUiIndex(), weapon);
	}

	Map<Integer, MAGAZINELIST.MAGAZINE> magazines = new HashMap<Integer, MAGAZINELIST.MAGAZINE>();
	for (MAGAZINELIST.MAGAZINE weapon : xml.getMagazines().getMAGAZINE()) {
	    magazines.put(weapon.getUiIndex(), weapon);
	}

	Map<Integer, LOADBEARINGEQUIPMENTLIST.LOADBEARINGEQUIPMENT> lbes = new HashMap<Integer, LOADBEARINGEQUIPMENTLIST.LOADBEARINGEQUIPMENT>();
	for (LOADBEARINGEQUIPMENTLIST.LOADBEARINGEQUIPMENT weapon : xml.getLBE().getLOADBEARINGEQUIPMENT()) {
	    lbes.put(weapon.getLbeIndex(), weapon);
	}

	Map<Integer, AMMO> calibers = new HashMap<Integer, AMMO>();
	for (AMMO weapon : xml.getCalibers().getAMMO()) {
	    calibers.put((int) weapon.getUiIndex(), weapon);
	}

	Map<Integer, AMMOTYPELIST.AMMOTYPE> ammoTypes = new HashMap<Integer, AMMOTYPELIST.AMMOTYPE>();
	for (AMMOTYPELIST.AMMOTYPE weapon : xml.getAmmoTypes().getAMMOTYPE()) {
	    ammoTypes.put((int) weapon.getUiIndex(), weapon);
	}

	Map<Integer, DRUG> drugs = new HashMap<Integer, DRUG>();
	for (DRUG weapon : xml.getDrugs().getDRUG()) {
	    drugs.put((int) weapon.getUiIndex(), weapon);
	}
	
	ImmutableMap<Integer, FOOD> foods = Maps.uniqueIndex(xml.getFood().getFOOD(), drug -> {
	    return (int) drug.getUiIndex();
	});

	for (int itemId : items.keySet()) {
	    Item item = items.get(itemId);

	    item.setImageSource(imageCache.get(item.getImageType()));

	    ItemClassEnum itemClass = ItemClassMap.get(item.getItemType());

	    if (itemClass != null) {
		switch (itemClass) {
		    case Ammo:
			MAGAZINELIST.MAGAZINE magType = magazines.get(item.getItemTypeId());
			AMMO caliber = calibers.get((int) magType.getUbCalibre());
			AMMOTYPELIST.AMMOTYPE ammoType = ammoTypes.get((int) magType.getUbAmmoType());

			item.setMagazineData(magType);
			item.setCaliberData(caliber);
			item.setAmmoTypeData(ammoType);
			break;
		    case Gun:
		    case Knife:
		    case Throwing_Knife:
		    case Launcher:
		    case Tentacle:
		    case Thrown_Weapon:
		    case Blunt_Weapon:
			WEAPONLIST.WEAPON weaponData = weapons.get(item.getItemTypeId());
			if (weaponData != null) {
			    item.setWeaponData(weaponData);

			    if (weaponData.getUbCalibre() != null && weaponData.getUbCalibre() > 0) {
				AMMO gunCaliber = calibers.get((int) weaponData.getUbCalibre());
				item.setCaliberData(gunCaliber);
			    }
			}
			break;

		    case Grenade:
		    case Bomb:
			EXPLOSIVELIST.EXPLOSIVE explosiveData = explosives.get(item.getItemTypeId());
			item.setExplosiveData(explosiveData);
			break;
		    case Armour:
			ARMOURLIST.ARMOUR armorData = armors.get(item.getItemTypeId());
			item.setArmorData(armorData);
			break;
		    case Medkit:
		    case Kit:
		    case Face_Item:
		    case Key:
			// ???
			break;
		    case Load_Bearing_Equipment:
			LOADBEARINGEQUIPMENTLIST.LOADBEARINGEQUIPMENT lbeData = lbes.get(item.getItemTypeId());
			item.setLbeData(lbeData);
			break;
		    case Misc:
		    case Money:
			// ???
			break;
		    case Random_Item:
			// TODO: load random item data (if needed, not sure, find out if it is)
			break;
		    default:
			throw new AssertionError(itemClass.name());
		}

		if (item.getDrugType() != 0) {
		    item.setDrugData(drugs.get(item.getDrugType()));
		}

		if (item.getFoodType() != 0) {
		    item.setFoodData(foods.get(item.getFoodType()));
		}

	    }
	}
    }

    public Integer getItemCount() {
	return items.size();
    }

    public Item getItem(Integer id) {
	return items.get(id);
    }

    @Override
    public String toString() {
	return "ItemManager{ items=" + items.size() + "}";
    }

}