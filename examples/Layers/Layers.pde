// If you need to use layer opacity, you must draw each layer on a different canvas.
// and join them later.
import ptmx.*;

Ptmx map;
PGraphics layer0, layer1, layer2;
int x , y;

void setup() {
  size(800, 600);
  map = new Ptmx(this, "perspective_walls.tmx");
  map.setDrawMode(CENTER);
  map.setPositionMode("MAP");
  layer0 = createGraphics(width, height);
  layer1 = createGraphics(width, height);
  layer2 = createGraphics(width, height);
  x = int(map.getMapSize().x / 2);
  y = int(map.getMapSize().y / 2);
}

void draw(){
  background(map.getBackgroundColor());
  map.setBackgroundMode("COLOR");
  map.draw(layer0, 0, x, y);
  map.setBackgroundMode("CLEAR");
  map.draw(layer1, 1, x, y);
  map.draw(layer2, 2, x, y);
  image(layer0, 0, 0);
  image(layer1, 0, 0);
  image(layer2, 0, 0);
  textSize(24);
  fill(255);
  text("UP - DOWN Keys", 10, 50);
}

void keyPressed(){
  if(keyCode == UP){
    map.setOpacity(1, min(1, map.getOpacity(1) + 0.2));
    if(map.getOpacity(1) == 1) map.setOpacity(2, min(1, map.getOpacity(2) + 0.2));
  }
  if(keyCode == DOWN){
    map.setOpacity(2, max(0, map.getOpacity(2) - 0.2));
    if(map.getOpacity(2) == 0) map.setOpacity(1, max(0, map.getOpacity(1) - 0.2));   
  }
}