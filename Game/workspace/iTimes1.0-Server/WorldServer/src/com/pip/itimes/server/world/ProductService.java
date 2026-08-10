package com.pip.itimes.server.world;

import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.Recipe;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.TemplateGrid;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ProductService {
    public static Changed product(WorldPlayer player, Recipe recipe) throws
            ProductException {
        Changed ret = new Changed();
        TemplateGrid[] resources = recipe.getResources();
        TemplateGrid[] products = recipe.getProducts();
        if (!player.containsRecipe(recipe))
            throw new ProductException("您没有此配方");
        if (!player.contains(resources))
            throw new ProductException("资源不足");
        if (player.isOver(products))
            throw new ProductException("您背包空余的位置不够");
        player.completeRemoveItem(resources,ret);
        player.addItems(products,ret, player.getClientDataVersion());
//        for (int i = 0; i < resources.length; i++) {
//            ret.addItem(resources[i].item.getItemId(), -resources[i].count);
//        }
//        player.reset();
        return ret;
    }
}
