import ptmx.*;

Ptmx map;
int x = 0, y = 0;
boolean showCoords = false;

void setup() {
  size(800, 600);
  ellipseMode(CENTER);
  map = new Ptmx(this, "desert.tmx");
}

void draw(){
  background(map.getBackgroundColor());
  map.draw(x, y);
  if(showCoords){
    fill(255);
    textSize(8);
    for(int nx = 0; nx < map.getMapSize().x; nx++)
    for(int ny = 0; ny < map.getMapSize().y; ny++){
      float px = map.mapToCam(nx, ny).x;
      float py = map.mapToCam(nx, ny).y;
      ellipse(px, py, 2, 2);
      text(nx + "," + ny, px, py);
    }
  }
  textSize(24);
  fill(128);
  text("Cursor keys to move, z to switch coordinates, x to change map", 10, 50);

  text("Corner coordinates: " + x + ", " + y, 10, 100);
}

void changeMap(){
  x = 0;
  y = 0;
  switch(map.getFilename()){
    case "desert.tmx":
      map = new Ptmx(this, "isometric_grass_and_water.tmx");
      break;
    case "isometric_grass_and_water.tmx":
      map = new Ptmx(this, "perspective_walls.tmx");
      break;
    case "perspective_walls.tmx":
      map = new Ptmx(this, "sewers.tmx");
      break;
    case "sewers.tmx":
      map = new Ptmx(this, "desert.tmx");
      break;
  }
}

void keyPressed(){
  if(keyCode == LEFT) x-=map.getTileSize().x;
  if(keyCode == RIGHT) x+=map.getTileSize().x;
  if(keyCode == UP) y-=map.getTileSize().y;
  if(keyCode == DOWN) y+=map.getTileSize().y;
  if(keyCode == 'Z') showCoords = !showCoords;
  if(keyCode == 'X') changeMap();
}
