package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import models.TexturedModel;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;

public class MainGameLoop {

	public static void main(String[] args) {

		//Initialize renderer
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		MasterRenderer renderer = new MasterRenderer(loader);
		
		//******Load Terrain Textures******
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap2"));
		
		//Procedural terrain generation example
		Terrain terrain = new Terrain(1,1,loader,texturePack,blendMap);
		
		// Height map terrain generation example
		//Terrain terrain = new Terrain(0,-1,loader, texturePack, blendMap, "heightmap2");
		//*********************************
		
		
		//******Load Models and respective textures******	
		//Load Player
		TexturedModel character = new TexturedModel(OBJLoader.loadObjModel("person", loader), new ModelTexture(loader.loadTexture("playerTexture")));
		character.getTexture().setHasTransparency(false);
		Player player = new Player(character, new Vector3f(1200, 1, 1200), 0, 0, 0, 0.5f);
		
		//Load Tree
		TexturedModel tree = new TexturedModel(OBJLoader.loadObjModel("tree", loader),new ModelTexture(loader.loadTexture("tree")));
		tree.getTexture().setHasTransparency(false);
		
		//Load Fern	
		//ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fernatlas"));
		//fernTextureAtlas.setNumberOfRows(2);
		//TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTextureAtlas);
		//fern.getTexture().setHasTransparency(true);
		
		//Load Lights
		List<Light> lights = new ArrayList<Light>();
		Light sun = new Light(new Vector3f(1000, 3000, 400), new Vector3f(0.4f, 0.4f, 0.4f));
		lights.add(sun);
		
		//Load Camera
		Camera camera = new Camera(player);	
		//***********************************************

		
		//******Spawn entities******
		List<Entity> entities = new ArrayList<Entity>();	
		Random random = new Random();
		for(int i=0;i<400;i++){
			if (i % 40 == 0) {
				float x = random.nextFloat()*100 + 800;
				float z = random.nextFloat() * -100 + 800;
				float y = terrain.getHeightOfTerrain(x,z);
				entities.add(new Entity(tree, new Vector3f(x,y,z),0,0,0,3));
			}
			/*
			if (i % 1 == 0) {
				float x = random.nextFloat()*800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x,z);
				entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x,y,z), 0, random.nextFloat() * 360, 0, 0.5f));
			}
			*/
		}
		
		//**************************
		
		
		//******Ray Cast******
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
		//********************
			
		
		//******Water******
		WaterFrameBuffers buffers = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(1200, 1200, 0);
		waters.add(water);
		//*****************
		
		
		//******GUI******
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		//***************
		
				
		//******Render******
		while(!Display.isCloseRequested()){			
			player.move(terrain);
			camera.move();
			picker.update();
			
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			
			//Reflection Buffer
			buffers.bindReflectionFrameBuffer();
			float distance = 2* (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, terrain, lights, camera, player, new Vector4f(0, 1 , 0, -water.getHeight()+1));
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			//Refraction Buffer
			buffers.bindRefractionFrameBuffer();
			renderer.renderScene(entities, terrain, lights, camera, player, new Vector4f(0, -1 , 0, water.getHeight()+1));
			
			//Render to screen
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			buffers.unbindCurrentFrameBuffer();
			renderer.renderScene(entities, terrain, lights, camera, player, new Vector4f(0, -1, 0, 20));
			waterRenderer.render(waters, camera, sun);		
			guiRenderer.render(guis);
			
			DisplayManager.updateDisplay();
			
			/*
			Vector3f terrainPoint = picker.getCurrentTerrainPoint();
			if(terrainPoint!=null) {
				lampEntity.setPosition(terrainPoint);
				light.setPosition(new Vector3f(terrainPoint.x, terrainPoint.y+15, terrainPoint.z));
			}
			*/
		}

		buffers.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
