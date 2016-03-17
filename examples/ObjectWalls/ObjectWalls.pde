// When drawing all layers, only the visible ones are drawn.
// When drawing a specific layer, the visible property is not considered.
// That way you can use a invisible layer for collisins.
import ptmx.*;

Ptmx map;
PGraphics walls;
PImage smiley;
int x , y;
boolean left, right, up, down, seeCol;

void setup() {
  size(800, 600);
  smiley = loadImage("smiley.png");
  map = new Ptmx(this, "desert.tmx");
  map.setDrawMode(CENTER);
  map.setPositionMode("CANVAS");//Default Position Mode
  walls = createGraphics(32, 32);
  x = int(map.mapToCanvas(map.getMapSize()).x / 2);
  y = int(map.mapToCanvas(map.getMapSize()).y / 2);
  imageMode(CENTER);
}

void draw(){
  background(map.getBackgroundColor());
  map.draw(x, y);
  image(smiley, width / 2, height / 2);
  if(seeCol) image(walls, width/2, height/2);
  textSize(24);
  fill(128);
  text("Move more freely. Press X to see the 'Wall' image", 10, 50);
  text("Press Z to see the walls", 10, 100);
  int prevX = x;
  int prevY = y;
  if(left) x -= 3;
  if(right) x += 3;
  if(up) y -= 3;
  if(down) y += 3;
  map.draw(walls, 1, x, y);
  if(walls.get(15, 15) == color(0)){
    x = prevX;
    y = prevY;
  }
}

void keyPressed(){
  if(keyCode == LEFT) left = true;
  if(keyCode == RIGHT) right = true;
  if(keyCode == UP) up = true;
  if(keyCode == DOWN) down = true;
  if(keyCode == 'Z') map.setVisible(1, !map.getVisible(1));
  if(keyCode == 'X') seeCol = !seeCol;
}

void keyReleased(){
  if(keyCode == LEFT) left = false;
  if(keyCode == RIGHT) right = false;
  if(keyCode == UP) up = false;
  if(keyCode == DOWN) down = false;
}
