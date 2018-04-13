package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		//******Load Terrain Textures******
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		//*********************************
		
		
		//******Load Models and respective textures******	
		TexturedModel character = new TexturedModel(OBJLoader.loadObjModel("person", loader), new ModelTexture(loader.loadTexture("playerTexture")));
		character.getTexture().setHasTransparency(false);
		
		TexturedModel tree = new TexturedModel(OBJLoader.loadObjModel("tree", loader),new ModelTexture(loader.loadTexture("tree")));
		tree.getTexture().setHasTransparency(false);
		
		/*
		 * TexturedModel grass = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);
		*/
		
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fernatlas"));
		fernTextureAtlas.setNumberOfRows(2);
		
		TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTextureAtlas);
		fern.getTexture().setHasTransparency(true);
		
		//***********************************************
		
		
		//******Initialize Light, Camera, Terrain, Renderer, ETC******
		Light light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));
		
		Player player = new Player(character, new Vector3f(0, 0, -200), 0, 0, 0, 0.5f);
		
		Terrain terrain = new Terrain(-0.5f,-1,loader, texturePack, blendMap, "heightmap");
		
		Camera camera = new Camera(player);	
		MasterRenderer renderer = new MasterRenderer();
		
		//************************************************************

		
		//******Spawn entities******
		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
		for(int i=0;i<400;i++){
			if (i % 20 == 0) {
				float x = random.nextFloat()*800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x,z);
				entities.add(new Entity(tree, new Vector3f(x,y,z),0,random.nextFloat() * 360 ,0,6));
			}
			
			if (i % 1 == 0) {
				float x = random.nextFloat()*800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x,z);
				entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x,y,z), 0, random.nextFloat() * 360, 0, 0.5f));
			}
		}
		
		//**************************
		
		
		//******GUI******
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("socuwan"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f,0.25f));
		guis.add(gui);
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		//***************
		
		
		//******Render******
		while(!Display.isCloseRequested()){			
			player.move(terrain);
			camera.move();
			renderer.processEntity(player);
			renderer.processTerrain(terrain);
			for(Entity entity:entities){
				renderer.processEntity(entity);
			}
			renderer.render(light, camera);
			guiRenderer.render(guis);
			DisplayManager.updateDisplay();
		}
		
		//******************

		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
