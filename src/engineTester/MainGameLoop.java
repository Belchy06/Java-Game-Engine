package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.TexturedModel;

import org.lwjgl.opengl.Display;
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

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		//******Load Terrain Textures******
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		//*********************************
		
		
		//******Load Models and respective textures******	
		TexturedModel stanfordBunny = new TexturedModel(OBJLoader.loadObjModel("stanfordBunny", loader), new ModelTexture(loader.loadTexture("white")));
		stanfordBunny.getTexture().setHasTransparency(false);
		
		TexturedModel tree = new TexturedModel(OBJLoader.loadObjModel("tree", loader),new ModelTexture(loader.loadTexture("tree")));
		tree.getTexture().setHasTransparency(false);
		
		/*
		 * TexturedModel grass = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);
		*/
		
		TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), new ModelTexture(loader.loadTexture("fern")));
		fern.getTexture().setHasTransparency(true);
		
		//***********************************************
		
		
		//******Initialize Light, Camera, Terrain, Renderer, ETC******
		Light light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));
		
		Player player = new Player(stanfordBunny, new Vector3f(0, 0, 0), 0, 0, 0, 0.5f);
		
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
			
			if (i % 10 == 0) {
				float x = random.nextFloat()*800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x,z);
				entities.add(new Entity(fern, new Vector3f(x,y,z),0,random.nextFloat() * 360 ,0,0.5f));
			}
		}
		
		//**************************
		
		
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
			DisplayManager.updateDisplay();
		}
		
		//******************

		
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
