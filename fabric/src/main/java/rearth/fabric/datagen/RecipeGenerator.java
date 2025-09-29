package rearth.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import rearth.Drones;
import rearth.init.BlockContent;
import rearth.init.ItemContent;
import rearth.init.TagContent;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends FabricRecipeProvider {
    
    public RecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }
    
    @Override
    public void generate(RecipeExporter exp) {
        
        // controller
        offerFrameRecipe(exp, BlockContent.ASSEMBLER_FRAME.get().asItem(), Ingredient.ofItems(Items.REPEATER), Ingredient.ofItems(Items.REDSTONE), Ingredient.fromTag(ItemTags.LOGS), Ingredient.ofItems(Items.IRON_INGOT), Ingredient.ofItems(Items.SMOOTH_STONE), 1, "_controller");
        
        // frame
        offerFrameRecipe(exp, BlockContent.ASSEMBLER_FRAME.get().asItem(), Ingredient.ofItems(Items.SMOOTH_STONE), Ingredient.ofItems(Items.SMOOTH_STONE), Ingredient.fromTag(ItemTags.LOGS), Ingredient.ofItems(Items.IRON_INGOT), Ingredient.ofItems(Items.SMOOTH_STONE), 6, "_frame");
        
        // basic rotor
        offerRotorRecipe(exp, BlockContent.WOOD_ROTOR.get().asItem(), Ingredient.fromTag(ItemTags.PLANKS), Ingredient.ofItems(Items.STICK), Ingredient.ofItems(Items.COPPER_INGOT), 1, "_woodrotor");
        // iron
        offerRotorRecipe(exp, BlockContent.IRON_ROTOR.get().asItem(), Ingredient.ofItems(Items.IRON_INGOT), Ingredient.ofItems(Items.STICK), Ingredient.ofItems(Items.COPPER_INGOT), 1, "_ironrotor");
        // ion thruster
        offerRotorRecipe(exp, BlockContent.ION_THRUSTER.get().asItem(), Ingredient.ofItems(Items.IRON_INGOT), Ingredient.ofItems(Items.STICK), Ingredient.ofItems(Items.DIAMOND), 1, "_ionrotor");
    
        // drill
        offerDrillRecipe(exp, BlockContent.DRILL.get().asItem(), Ingredient.ofItems(Items.IRON_INGOT), 1, "_drill");
    }
    
    public void offerFrameRecipe(RecipeExporter exporter,
                                 Item output,
                                 Ingredient bottom,
                                 Ingredient botSides,
                                 Ingredient middleSides,
                                 Ingredient core,
                                 Ingredient top,
                                 int count,
                                 String suffix) {
        var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, output, count)
                        .input('s', botSides)
                        .input('c', core)
                        .input('f', top)
                        .input('b', bottom)
                        .input('m', middleSides)
                        .pattern("fff")
                        .pattern("mcm")
                        .pattern("sbs");
        builder.criterion(hasItem(output), conditionsFromItem(output)).offerTo(exporter, Drones.id("crafting/" + suffix));
    }
    
    public void offerRotorRecipe(RecipeExporter exporter,
                                 Item output,
                                 Ingredient outer,
                                 Ingredient inner,
                                 Ingredient core,
                                 int count,
                                 String suffix) {
        var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, output, count)
                        .input('o', outer)
                        .input('i', inner)
                        .input('c', core)
                        .pattern("oio")
                        .pattern("ici")
                        .pattern("ooo");
        builder.criterion(hasItem(output), conditionsFromItem(output)).offerTo(exporter, Drones.id("crafting/" + suffix));
    }
    
    public void offerDrillRecipe(RecipeExporter exporter,
                                 Item output,
                                 Ingredient main,
                                 int count,
                                 String suffix) {
        var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, output, count)
                        .input('m', main)
                        .pattern("m  ")
                        .pattern("mmm")
                        .pattern("m  ");
        builder.criterion(hasItem(output), conditionsFromItem(output)).offerTo(exporter, Drones.id("crafting/" + suffix));
    }
}
