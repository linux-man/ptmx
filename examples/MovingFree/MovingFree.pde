import ptmx.*;

Ptmx map;
PImage smiley;
int x , y;
boolean left, right, up, down;

void setup() {
  size(800, 600);
  smiley = loadImage("smiley.png");
  map = new Ptmx(this, "desert.tmx");
  map.setDrawMode(CENTER);
  map.setPositionMode("CANVAS");//Default Position Mode
  x = int(map.mapToCanvas(map.getMapSize()).x / 2);
  y = int(map.mapToCanvas(map.getMapSize()).y / 2);
  imageMode(CENTER);
}

void draw(){
  background(map.getBackgroundColor());
  map.draw(x, y);
  image(smiley, width / 2, height / 2);
  textSize(24);
  fill(128);
  text("Move (almost) free. Catch some plants...", 10, 50);
  int prevX = x;
  int prevY = y;
  if(left) x -= 3;
  if(right) x += 3;
  if(up) y -= 3;
  if(down) y += 3;
  PVector overTile = map.canvasToMap(x, y);
  switch(map.getTileIndex(0, round(overTile.x), round(overTile.y))){
    case 6: case 7: case 13: case 14: case 15: case 21: case 22: case 23: case 29:
      break;
    case 30: case 37: case 38: case 39: case 46: case 47:
      map.setTileIndex(0, round(overTile.x), round(overTile.y), 29);
      break;
    default:
      x = prevX;
      y = prevY;
  }
}

void keyPressed(){
  if(keyCode == LEFT) left = true;
  if(keyCode == RIGHT) right = true;
  if(keyCode == UP) up = true;
  if(keyCode == DOWN) down = true;
}

void keyReleased(){
  if(keyCode == LEFT) left = false;
  if(keyCode == RIGHT) right = false;
  if(keyCode == UP) up = false;
  if(keyCode == DOWN) down = false;
}
