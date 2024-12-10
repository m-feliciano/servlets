package com.dev.servlet.controllers;

import com.dev.servlet.dao.InventoryDAO;
import com.dev.servlet.pojo.Inventory;
import lombok.NoArgsConstructor;

import javax.inject.Inject;


@NoArgsConstructor
public final class InventoryController extends BaseController<Inventory, Long> {

    @Inject
    public InventoryController(InventoryDAO inventoryDAO) {
        super(inventoryDAO);
    }

    @Override
    public InventoryDAO getBaseDAO() {
        return (InventoryDAO) super.getBaseDAO();
    }

    public boolean has(Inventory inventory) {
        return getBaseDAO().has(inventory);
    }
}
