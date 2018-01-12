package ptmx;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.XML;
import processing.data.StringDict;

public class Ptmx {
  private PApplet parent;
  private float version;
  private String filename, orientation, renderorder, staggeraxis;//renderorder is not used. Always "right-down"
  private int camwidth, camheight, camleft, camtop, mapwidth, mapheight, tilewidth, tileheight, staggerindex, hexsidelength, backgroundcolor;
  private int drawmargin = 2;
  private int drawmode = 0;//0=CORNER, 3=CENTER
  private String positionmode = "CANVAS";//"CANVAS" or "MAP"
  private String backgroundmode = "COLOR";//"COLOR", "CLEAR" or "NONE"
  private Tile[] tile = new Tile[0];
  private Layer[] layer = new Layer[0];
  private Xmap map;

  abstract class Xmap{
    abstract PVector mapToCanvas(float nx, float ny);
    abstract PVector canvasToMap(float x, float y);
    abstract void drawTileLayer(PGraphics pg, int m);
  }

  private class OrthogonalMap extends Xmap{
    public PVector mapToCanvas(float nx, float ny){
      float x = (nx + (float)0.5) * Ptmx.this.tilewidth;
      float y = (ny + (float)0.5) * Ptmx.this.tileheight;
      return new PVector(x , y);
    }
    public PVector canvasToMap(float x, float y){
      float nx = x / Ptmx.this.tilewidth - (float)0.5;
      float ny = y / Ptmx.this.tileheight - (float)0.5;
      return new PVector(nx, ny);
    }
    public void drawTileLayer(PGraphics pg, int m){
      int n;
      float x, y;
      PVector p;
      Tile tileN;
      Layer l = Ptmx.this.layer[m];
      int xstart = parent.max(0, parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin));
      int xstop = parent.min(Ptmx.this.mapwidth, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
      int ystart = parent.max(0, parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
      int ystop = parent.min(Ptmx.this.mapheight, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
      for(int ny = ystart; ny < ystop; ny++) for(int nx = xstart; nx < xstop; nx++){
        n = l.data[nx + ny * Ptmx.this.mapwidth] - 1;
        if(n >= 0){
          tileN = Ptmx.this.tile[n];
          p = Ptmx.this.mapToCam(nx, ny);
          x = p.x - Ptmx.this.tilewidth / 2 + tileN.offsetx + l.offsetx;
          y = p.y - Ptmx.this.tileheight / 2 + tileN.offsety + l.offsety + (Ptmx.this.tileheight - tileN.image.height);;
          pg.image(tileN.image, x, y);
        }
      }
    }
  }

  private class IsometricMap extends Xmap{
    public PVector mapToCanvas(float nx, float ny){
      float x = (Ptmx.this.mapwidth + Ptmx.this.mapheight) * Ptmx.this.tilewidth / 4 + (nx - ny) * Ptmx.this.tilewidth / 2;
      float y = (nx + ny + 1) * Ptmx.this.tileheight / 2;
      return new PVector(x , y);
    }
    public PVector canvasToMap(float x, float y){
      float dif = ((float)Ptmx.this.mapwidth + Ptmx.this.mapheight) * Ptmx.this.tilewidth / 4;
      float nx = y / Ptmx.this.tileheight + ((x - dif) / Ptmx.this.tilewidth) - (float)0.5;
      float ny = y / Ptmx.this.tileheight - ((x - dif) / Ptmx.this.tilewidth) - (float)0.5;
      return new PVector(nx, ny);
    }
    public void drawTileLayer(PGraphics pg, int m){
      int n;
      float x, y;
      PVector p;
      Tile tileN;
      Layer l = Ptmx.this.layer[m];
      int xstart = parent.max(0, parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin));
      int xstop = parent.min(Ptmx.this.mapwidth, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
      int ystart = parent.max(0, parent.floor(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
      int ystop = parent.min(Ptmx.this.mapheight, parent.ceil(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
      for(int ny = ystart; ny < ystop; ny++) for(int nx = xstart; nx < xstop; nx++){
        n = l.data[nx + ny * Ptmx.this.mapwidth] - 1;
        if(n >= 0){
          tileN = Ptmx.this.tile[n];
          p = Ptmx.this.mapToCam(nx, ny);
          x = p.x - Ptmx.this.tilewidth / 2 + tileN.offsetx + l.offsetx;
          y = p.y - Ptmx.this.tileheight / 2 + tileN.offsety + l.offsety + (Ptmx.this.tileheight - tileN.image.height);;
          pg.image(tileN.image, x, y);
        }
      }
    }
  }

  private class StaggeredXMap extends Xmap{
    public PVector mapToCanvas(float nx, float ny){
      float x = (nx * (Ptmx.this.hexsidelength + Ptmx.this.tilewidth) + Ptmx.this.tilewidth) / 2;
      float y = (ny + (float)0.5 + Ptmx.this.staggerindex / 2) * Ptmx.this.tileheight + (Ptmx.this.staggerindex * 2 - 1) * (parent.abs(parent.abs(nx) % 2 - 1) - 1) * (Ptmx.this.tileheight) / 2;
      return new PVector(x , y);
    }
    public PVector canvasToMap(float x, float y){
      float ny;
      float nx = (x * 2 - Ptmx.this.tilewidth) / (Ptmx.this.hexsidelength + Ptmx.this.tilewidth);
      if(Ptmx.this.staggerindex == 0) ny = y / Ptmx.this.tileheight - 1 + (parent.abs(parent.abs(nx) % 2 - 1)) / 2;
      else ny = y / Ptmx.this.tileheight - (float)0.5 - (parent.abs(parent.abs(nx) % 2 - 1)) / 2;
      return new PVector(nx, ny);
    }
    public void drawTileLayer(PGraphics pg, int m){
      int n;
      float x, y;
      PVector p;
      Tile tileN;
      Layer l = Ptmx.this.layer[m];
      int xstart = parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin);
      xstart = parent.max(0, xstart - xstart % 2);
      int xstop = parent.min(Ptmx.this.mapwidth, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
      int ystart = parent.max(0, parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
      int ystop = parent.min(Ptmx.this.mapheight, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
      for(int ny = ystart; ny < ystop; ny++) for(int nx = xstart; nx < xstop; nx++){
        n = l.data[nx + ny * Ptmx.this.mapwidth] - 1;
        if(n >= 0){
          tileN = Ptmx.this.tile[n];
          p = Ptmx.this.mapToCam(nx, ny);
          x = p.x - Ptmx.this.tilewidth / 2 + tileN.offsetx + l.offsetx;
          y = p.y - Ptmx.this.tileheight / 2 + tileN.offsety + l.offsety + (Ptmx.this.tileheight - tileN.image.height);;
          pg.image(tileN.image, x, y);
        }
      }
    }
  }

  private class StaggeredYMap extends Xmap{
    public PVector mapToCanvas(float nx, float ny){
      float y = ny * (Ptmx.this.hexsidelength + Ptmx.this.tileheight) / 2 + Ptmx.this.tileheight / 2;
      float x = (nx + (float)0.5 + Ptmx.this.staggerindex / 2) * Ptmx.this.tilewidth + (Ptmx.this.staggerindex * 2 - 1) * (parent.abs(parent.abs(y) % 2 - 1) - 1) * (Ptmx.this.tilewidth) / 2;
      return new PVector(x , y);
    }
    public PVector canvasToMap(float x, float y){
      float nx;
      float ny = (y * 2 - Ptmx.this.tileheight) / (Ptmx.this.hexsidelength + Ptmx.this.tileheight);
      if (Ptmx.this.staggerindex == 0) nx = x / Ptmx.this.tilewidth - 1 + (parent.abs(parent.abs(ny) % 2 - 1)) / 2;
      else nx = x / Ptmx.this.tilewidth - (float)0.5 - (parent.abs(parent.abs(ny) % 2 - 1)) / 2;
      return new PVector(nx, ny);
    }
    public void drawTileLayer(PGraphics pg, int m){
      int n;
      float x, y;
      PVector p;
      Tile tileN;
      Layer l = Ptmx.this.layer[m];
      int xstart = parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).x - Ptmx.this.drawmargin);
      xstart = parent.max(0, xstart - xstart % 2);
      int xstop = parent.min(Ptmx.this.mapwidth, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).x + Ptmx.this.drawmargin));
      int ystart = parent.max(0, parent.floor(this.canvasToMap(Ptmx.this.camleft, Ptmx.this.camtop).y - Ptmx.this.drawmargin));
      int ystop = parent.min(Ptmx.this.mapheight, parent.ceil(this.canvasToMap(Ptmx.this.camleft + pg.width, Ptmx.this.camtop + pg.height).y + Ptmx.this.drawmargin));
      for(int ny = ystart; ny < ystop; ny++) for(int nx = xstart; nx < xstop; nx++){
        n = l.data[nx + ny * Ptmx.this.mapwidth] - 1;
        if(n >= 0){
          tileN = Ptmx.this.tile[n];
          p = Ptmx.this.mapToCam(nx, ny);
          x = p.x - Ptmx.this.tilewidth / 2 + tileN.offsetx + l.offsetx;
          y = p.y - Ptmx.this.tileheight / 2 + tileN.offsety + l.offsety + (Ptmx.this.tileheight - tileN.image.height);;
          pg.image(tileN.image, x, y);
        }
      }
    }
  }

  private class Tile{
    private PImage image;
    private int offsetx, offsety;
    Tile(PImage image, int offsetx, int offsety){
      this.image = image;
      this.offsetx = offsetx;
      this.offsety = offsety;
    }
  }

  private class Layer{
    private String type, name;
    private boolean visible;
    private float offsetx, offsety, opacity;
    private int objcolor;
    private int [] data;
    private PImage image;
    private StringDict [] objects;
    private StringDict properties;
    Layer(String type, String name, boolean visible, float offsetx, float offsety, float opacity, int[] data, PImage image, StringDict [] objects, int objcolor, StringDict properties){
      this.type = type;
      this.name = name;
      this.visible = visible;
      this.offsetx = offsetx;
      this.offsety = offsety;
      this.opacity = opacity;
      this.data = data;
      this.image = image;
      this.objects = objects;
      this.objcolor = objcolor;
      this.properties = properties;
    }
  }

  public Ptmx(PApplet p, String filename){
    this.parent = p;
    this.camwidth = p.width;
    this.camheight = p.height;
    this.filename = filename;
    XML xml = parent.loadXML(filename);
    if(xml == null) throw new RuntimeException("Not a valid XML File or not UTF-8 encoded");
    if(xml.getName() != "map") throw new RuntimeException("Not a Tmx file (missing 'map' element)");
    loadTmxProperties(xml);
    switch(this.orientation){
      case "orthogonal":
        map = new OrthogonalMap();
        break;
      case "isometric":
        map = new IsometricMap();
        break;
      case "staggered":
        this.hexsidelength = 0;
      case "hexagonal":
        if(this.staggeraxis.equals("x")){
          map = new StaggeredXMap();
      }
        else{
          map = new StaggeredYMap();
      }
        break;
    }
    XML childs[] = xml.getChildren("tileset");
    for(XML e: childs) loadTileset(e);
    childs = xml.getChildren();
    for(XML e: childs) switch(e.getName()){
      case "layer":
      case "imagelayer":
      case "objectgroup":
        loadLayer(e);
        break;
    }
  }

  private void loadTmxProperties(XML e){
    this.version = e.getFloat("version");
    this.orientation = e.getString("orientation");
    this.renderorder = e.getString("renderorder");
    this.mapwidth = e.getInt("width");
    this.mapheight = e.getInt("height");
    this.tilewidth = e.getInt("tilewidth");
    this.tileheight = e.getInt("tileheight");
    this.staggerindex = 0; if(e.hasAttribute("staggerindex")) if(e.getString("staggerindex").equals("even")) this.staggerindex = 1;
    this.staggeraxis = "x"; if(e.hasAttribute("staggeraxis")) this.staggeraxis = e.getString("staggeraxis");
    this.hexsidelength = 0; if(e.hasAttribute("hexsidelength")) this.hexsidelength = e.getInt("hexsidelength");
    this.backgroundcolor = parent.color(80, 80, 80); if(e.hasAttribute("backgroundcolor")) this.backgroundcolor = this.readColor(e.getString("backgroundcolor"));
  }

  private void loadTileset(XML e){
    XML c;
    if(e.hasAttribute("source") && e.getChild("image") == null){
      loadTileset(parent.loadXML(e.getString("source")));
      return;
    }
    int tilewidth = e.getInt("tilewidth");
    int tileheight = e.getInt("tileheight");
    int tilecount = 0;
    int columns = 0;
    int spacing = 0;
    int margin = 0;
    int tileoffsetx = 0;
    int tileoffsety = 0;
    PImage source, image;
    if(e.hasAttribute("tilecount")) tilecount = e.getInt("tilecount");
    if(e.hasAttribute("columns")) columns = e.getInt("columns");
    if(e.hasAttribute("spacing")) spacing = e.getInt("spacing");
    if(e.hasAttribute("margin")) margin = e.getInt("margin");
    c = e.getChild("tileoffset");
    if(c != null) {
      tileoffsetx = c.getInt("x");
      tileoffsety = c.getInt("y");
    }
    c = e.getChild("image");
    if(c != null) {
      source = parent.loadImage(c.getString("source"));
      if(columns == 0) columns = parent.floor((source.width - margin) / (tilewidth + spacing));
      if(tilecount == 0) tilecount = columns * parent.floor((source.height - margin) / (tileheight + spacing));
      if(c.hasAttribute("trans")) this.applyTransColor(source, c.getString("trans"));
      for(int n = 0; n < tilecount; n++){
        image = source.get(margin + (n % columns) * (tilewidth + spacing), margin + parent.floor(n / columns) * (tileheight + spacing), tilewidth, tileheight);
        this.tile = (Tile[])parent.append(this.tile, new Tile(image, tileoffsetx, tileoffsety));
      }
    }
    else{
      XML cs[] = e.getChildren("tile");
      if(cs != null){
        for(XML t: cs){
          image = parent.loadImage(t.getChild("image").getString("source"));
          this.tile = (Tile[])parent.append(this.tile, new Tile(image, tileoffsetx, tileoffsety));
        }
      }
    }
  }

  private void loadLayer(XML e){
    String type = "layer";
    String name = e.getString("name");
    boolean visible = true; if(e.hasAttribute("visible")) visible = e.getInt("visible") == 1;
    float offsetx = 0; if(e.hasAttribute("offsetx")) offsetx = e.getFloat("offsetx");
    float offsety = 0; if(e.hasAttribute("offsety")) offsety = e.getFloat("offsety");
    float opacity = 1; if(e.hasAttribute("opacity")) opacity = e.getFloat("opacity");
    int objcolor = parent.color(0); if(e.hasAttribute("color")) objcolor = this.readColor(e.getString("color"));
    XML c;
    int[] content = null;
    PImage image = null;
    StringDict [] objects = new StringDict[0];
    StringDict properties = new StringDict();
    switch(e.getName()){
      case "layer":
        c = e.getChild("data");
        if(!(c.getString("encoding").equals("csv"))) throw new RuntimeException("Tmx can only handle CSV encoding");
        content = parent.parseInt(parent.split(c.getContent().replace("\n", ""), ","));
        break;
      case "imagelayer":
        type = "imagelayer";
        c = e.getChild("image");
        image = parent.loadImage(c.getString("source"));
        if(c.hasAttribute("trans")) this.applyTransColor(image, c.getString("trans"));
        break;
      case "objectgroup":
        type = "objectgroup";
        XML xo[] = e.getChildren("object");
        for(XML o : xo){
          StringDict prop = new StringDict(); //<>//
          if(o.hasAttribute("gid")){
            prop.set("object", "tile");
            prop.set("gid", o.getString("gid"));
          }
          else if(o.getChild("ellipse") != null) prop.set("object", "ellipse");
          else if(o.getChild("polyline") != null){
            prop.set("object", "polyline");
            prop.set("points", o.getChild("polyline").getString("points"));
          }
          else if(o.getChild("polygon") != null){ //<>//
            prop.set("object", "polygon");
            prop.set("points", o.getChild("polygon").getString("points"));
          }
          else prop.set("object", "rectangle");
          if(o.hasAttribute("id")) prop.set("id", o.getString("id"));
          if(o.hasAttribute("name")) prop.set("name", o.getString("name"));
          if(o.hasAttribute("type")) prop.set("type", o.getString("type"));
          if(o.hasAttribute("visible")) prop.set("visible", o.getString("visible"));
          if(o.hasAttribute("x")) prop.set("x", o.getString("x"));
          if(o.hasAttribute("y")) prop.set("y", o.getString("y"));
          if(o.hasAttribute("width")) prop.set("width", o.getString("width"));
          if(o.hasAttribute("height")) prop.set("height", o.getString("height"));
          if(o.hasAttribute("rotation")) prop.set("rotation", o.getString("rotation"));
          if(o.getChild("properties") != null){
            XML childs[] = o.getChild("properties").getChildren("property");
            for(XML p: childs) prop.set(p.getString("name"), p.getString("value"));
          }
          objects = (StringDict[])parent.append(objects, prop);
        }
        break;
    }
    if(e.getChild("properties") != null){
      XML childs[] = e.getChild("properties").getChildren("property");
      for(XML p: childs) properties.set(p.getString("name"), p.getString("value"));
    }
    this.layer = (Layer[])parent.append(this.layer, new Layer(type, name, visible, offsetx, offsety, opacity, content, image, objects, objcolor, properties));
  }

  private int readColor(String s){
    if(s.charAt(0) == '#') s = s.substring(1);
    if(s.length() == 6) s = "FF" + s;
    return parent.unhex(s);
  }

  private void applyTransColor(PImage source, String c){
    int trans = this.readColor(c);
    source.loadPixels();
    for (int p = 0; p < source.pixels.length; p++) if(source.pixels[p] == trans) source.pixels[p] = parent.color(255, 1);
    source.updatePixels();
  }

  private void prepareDraw(PGraphics pg, float left, float top){
    this.camwidth = pg.width;
    this.camheight = pg.height;
    if(this.positionmode.equals("MAP")){
      PVector p = this.mapToCanvas(left, top);
      left = p.x;
      top = p.y;
    }
    if(this.drawmode == 3){
      this.camleft = parent.floor(left - this.camwidth / 2);
      this.camtop = parent.floor(top - this.camheight / 2);
    }
    else{
      this.camleft = parent.floor(left);
      this.camtop = parent.floor(top);
    }
    if(pg != parent.g) pg.beginDraw();
    pg.pushMatrix();
    pg.resetMatrix();
    pg.pushStyle();
    pg.imageMode(parent.CORNER);
    switch(this.backgroundmode) {
      case "COLOR":
        pg.background(this.backgroundcolor);
        break;
      case "CLEAR":
        if(pg != parent.g) pg.clear();
        else pg.background(this.backgroundcolor);
        break;
    }
  }

  private void finishDraw(PGraphics pg){
    pg.popStyle();
    pg.popMatrix();
    if(pg != parent.g) pg.endDraw();
  }

  private void drawLayer(PGraphics pg, int m){
    int tileoffsetx, tileoffsety, n, dif;
    float x = 0, y = 0;
    Layer l = this.layer[m];
    switch(l.type){
      case "layer":
        map.drawTileLayer(pg, m);
        break;
      case "imagelayer":
        pg.image(l.image, -this.camleft + l.offsetx, -this.camtop + l.offsety);
        break;
      case "objectgroup":
        pg.fill(l.objcolor);pg.stroke(l.objcolor); pg.strokeWeight(1);
        for(StringDict o: l.objects){
          if(!o.hasKey("visible")){
            pg.pushMatrix();
            pg.resetMatrix();
            pg.pushStyle();
            pg.ellipseMode(parent.CORNER);
            pg.translate(parent.parseFloat(o.get("x")) - l.offsetx - this.camleft, parent.parseFloat(o.get("y"))- l.offsety - this.camtop);
            if(o.hasKey("rotation")) pg.rotate(parent.parseFloat(o.get("rotation")) * parent.PI / 180);
            switch(o.get("object")){
              case "rectangle":
                pg.rect(0, 0, parent.parseFloat(o.get("width")), parent.parseFloat(o.get("height")));
                break;
              case "ellipse":
                pg.ellipse(0, 0, parent.parseFloat(o.get("width")), parent.parseFloat(o.get("height")));
                break;
              case "tile":
                if(o.hasKey("rotation")) pg.rotate(-parent.parseFloat(o.get("rotation")) * parent.PI / 180);
                pg.translate(0, -this.tile[parent.parseInt(o.get("gid")) - 1].image.height);
                if(o.hasKey("rotation")) pg.rotate(parent.parseFloat(o.get("rotation")) * parent.PI / 180);
                pg.image(this.tile[parent.parseInt(o.get("gid")) - 1].image, this.tile[parent.parseInt(o.get("gid")) - 1].offsetx, this.tile[parent.parseInt(o.get("gid")) - 1].offsety, parent.parseFloat(o.get("width")), parent.parseFloat(o.get("height")));
                break;
              case "polygon":
              case "polyline":
                if(o.get("object").equals("polyline")) pg.noFill();
                pg.beginShape();
                for(String s: parent.split(o.get("points"), " ")){
                  float [] p = parent.parseFloat(parent.split(s, ","));
                  pg.vertex(p[0], p[1]);
                }
                if(o.get("object").equals("polyline")) pg.endShape(); else pg.endShape(parent.CLOSE);
                break;
            }
            pg.popStyle();
            pg.popMatrix();
          }
        }
        break;
    }
    if(pg != parent.g && l.opacity < 1){//applyOpacity
      pg.loadPixels();
      int a = parent.parseInt(parent.map(l.opacity, 0, 1, 1, 255));
      for (int p = 0; p < pg.pixels.length; p++) if(parent.alpha(pg.pixels[p]) > a) pg.pixels[p] = parent.color(parent.red(pg.pixels[p]), parent.green(pg.pixels[p]), parent.blue(pg.pixels[p]), a);
      pg.updatePixels();
    }
  }

//Public methods
//Draw Methods

  public void draw(){
    this.draw(parent.g, 0, 0);
  }

  public void draw(PVector p){
    this.draw(parent.g, parent.floor(p.x), parent.floor(p.y));
  }

  public void draw(float left, float top){
    this.draw(parent.g, left, top);
  }

  public void draw(int n, PVector p){
    this.draw(parent.g, n, p.x, p.y);
  }

  public void draw(int n, float left, float top){
    this.draw(parent.g, n, left, top);
  }

  public void draw(PGraphics pg){
    this.draw(pg, 0, 0);
  }

  public void draw(PGraphics pg, PVector p){
    this.draw(pg, p.x, p.y);
  }

  public void draw(PGraphics pg, float left, float top){
    this.prepareDraw(pg, left, top);
    for(int n = 0; n < layer.length; n++) if(this.layer[n].visible) this.drawLayer(pg, n);
    this.finishDraw(pg);
  }

  public void draw(PGraphics pg, int n, PVector p){
    this.draw(pg, n, p.x, p.y);
  }

  public void draw(PGraphics pg, int n, float left, float top){
    if(n >= 0 && n < this.layer.length){
      this.prepareDraw(pg, left, top);
      this.drawLayer(pg, n);
      this.finishDraw(pg);
    }
  }

//Layers methods

  public String getName(int n){
    if(n >= 0 && n < this.layer.length) return this.layer[n].name;
    else return null;
  }

  public String getType(int n){
    if(n >= 0 && n < this.layer.length) return this.layer[n].type;
    else return null;
  }

  public boolean getVisible(int n){
    if(n >= 0 && n < this.layer.length) return this.layer[n].visible;
    else return false;
  }

  public void setVisible(int n, boolean v){
    if(n >= 0 && n < this.layer.length) this.layer[n].visible = v;
  }

  public PImage getImage(int n){
    if(n >= 0 && n < this.layer.length && this.layer[n].type.equals("imagelayer")) return this.layer[n].image;
    else return null;
  }

  public StringDict[] getObjects(int n){
    if(n >= 0 && n < this.layer.length && this.layer[n].type.equals("objectgroup")) return this.layer[n].objects;
    else return null;
  }

  public int getObjectsColor(int n){
    if(n >= 0 && n < this.layer.length && this.layer[n].type.equals("objectgroup")) return this.layer[n].objcolor;
    else return 0;
  }

  public int[] getData(int n){
    if(n >= 0 && n < this.layer.length && this.layer[n].type.equals("layer")) return this.layer[n].data;
    else return null;
  }

  public StringDict getCustomProperties(int n){
    if(n >= 0 && n < this.layer.length) return this.layer[n].properties;
    else return null;
  }

  public float getOpacity(int n){
    if(n >= 0 && n < this.layer.length) return this.layer[n].opacity;
    else return 0;
  }

  public void setOpacity(int n, float o){
    if(n >= 0 && n < this.layer.length && o >= 0 && o <=1) this.layer[n].opacity = parent.min(parent.max(0, o), 1);
  }

  public int getTileIndex(int n, int x, int y){
    if(n >= 0 && n < this.layer.length && x >= 0 && y >= 0 && x < this.mapwidth && y < this.mapheight && this.layer[n].type.equals("layer")) return this.layer[n].data[x + y * this.mapwidth] - 1;
    else return -2;
  }

  public void setTileIndex(int n, int x, int y, int v){
    if(n >= 0 && n < this.layer.length && x >= 0 && y >= 0 && x < this.mapwidth && y < this.mapheight && this.layer[n].type.equals("layer") && v >= -1) this.layer[n].data[x + y * this.mapwidth] = v + 1;
  }

  public void toImage(int n){
    if(n >= 0 && n < this.layer.length){
      Layer l = this.layer[n];
      int fw = this.camwidth;
      int fh = this.camheight;
      int ml = this.camleft;
      int mt = this.camtop;
      boolean v = l.visible;
      PGraphics conv;
      l.visible = true;
      this.camleft = 0;
      this.camtop = 0;
      switch(this.orientation){
        case "orthogonal":
          this.camwidth = this.mapwidth * this.tilewidth;
          this.camheight = this.mapheight * this.tileheight;
          break;
        case "isometric":
          this.camwidth = (this.mapwidth + this.mapheight) * this.tilewidth / 2;
          this.camheight = (this.mapwidth + this.mapheight) * this.tileheight / 2;
          break;
        case "staggered":
        case "hexagonal":
          if(this.staggeraxis.equals("x")){
            this.camwidth = (this.mapwidth + 1) * (this.hexsidelength + this.tilewidth) / 2;
            this.camheight = (this.mapheight + 1) * (this.hexsidelength + this.tileheight);
          }
          else{
            this.camwidth = (this.mapwidth + 1) * (this.hexsidelength + this.tilewidth);
            this.camheight = (this.mapheight + 1) * (this.hexsidelength + this.tileheight) / 2;
          }
          break;
      }
      conv = parent.createGraphics(this.camwidth, this.camheight);
      conv.beginDraw();
      this.drawLayer(conv, n);
      conv.endDraw();
      l.image = conv;
      l.data = null;
      l.objects = null;
      l.type = "imagelayer";
      l.visible = v;
      l.offsetx = 0;
      l.offsety = 0;
      this.camwidth = fw;
      this.camheight = fh;
      this.camleft = ml;
      this.camtop = mt;
    }
  }

//Map methods

  public String getFilename(){
	    return this.filename;
	  }

  public float getVersion(){
    return this.version;
  }

  public int getBackgroundColor(){
    return this.backgroundcolor;
  }

  public PVector getMapSize(){
    return new PVector(this.mapwidth, this.mapheight);
  }

  public PVector getTileSize(){
    return new PVector(this.tilewidth, this.tileheight);
  }

  public int getHexSideLength(){
    return this.hexsidelength;
  }

  public PVector getCamCorner(){
    if(this.positionmode.equals("MAP")) return this.canvasToMap(this.camleft, this.camtop);
    else return new PVector(this.camleft, this.camtop);
  }

  public PVector getCamCenter(){
    if(this.positionmode.equals("MAP")) return this.canvasToMap(this.camleft + this.camwidth / 2, this.camtop + this.camheight / 2);
    else return new PVector(this.camleft + this.camwidth / 2, this.camtop + this.camheight / 2);
  }

  public PVector getCamSize(){
    return new PVector(this.camwidth, this.camheight);
  }

  public void setCamSize(PVector s){ //Only useful for some pre-draw calculations, since size is always the last PGraphics used.
    this.setCamSize(parent.floor(s.x), parent.floor(s.y));
  }

  public void setCamSize(int w, int h){
    this.camwidth = w;
    this.camheight = h;
  }

  public int getDrawMargin(){
    return this.drawmargin;
  }

  public void setDrawMargin(int n){ //for orthogonal maps
    this.drawmargin = parent.max(1, n);
  }

  public int getDrawMode(){//CORNER or CENTER
    return this.drawmode;
  }

  public void setDrawMode(int s){
    if(s == parent.CORNER || s == parent.CENTER) this.drawmode = s;
  }

  public String getPositionMode(){//"CANVAS" or "MAP"
    return this.positionmode;
  }

  public void setPositionMode(String s){
    if(s.equals("CANVAS") || s.equals("MAP")) this.positionmode = s;
  }

  public PVector getPosition(){
    if(this.drawmode == parent.CORNER) return this.getCamCorner();
    else return this.getCamCenter();
  }

  public String getBackgroundMode(){//"COLOR", "CLEAR" or "NONE"
    return this.backgroundmode;
  }

  public void setBackgroundMode(String s){
    if(s.equals("COLOR") || s.equals("CLEAR") || s.equals("NONE")) this.backgroundmode = s;
  }

//Coordinate methods

  public PVector canvasToMap(PVector p){
    return this.canvasToMap(p.x, p.y);
  }

  public PVector canvasToMap(float x, float y){
    return map.canvasToMap(x, y);
  }

  public PVector mapToCanvas(PVector p){
    return this.mapToCanvas(p.x, p.y);
  }

  public PVector mapToCanvas(float nx, float ny){
    return map.mapToCanvas(nx, ny);
  }

  public PVector camToCanvas(PVector p){
    return this.camToCanvas(p.x, p.y);
  }

  public PVector camToCanvas(float x, float y){
    return new PVector(x + this.camleft, y + this.camtop);
  }

  public PVector canvasToCam(PVector p){
    return this.canvasToCam(p.x, p.y);
  }

  public PVector canvasToCam(float x, float y){
    return new PVector(x - this.camleft, y - this.camtop);
  }

  public PVector camToMap(PVector p){
    return this.camToMap(p.x, p.y);
  }

  public PVector camToMap(float x, float y){
    return this.canvasToMap(this.camToCanvas(x, y));
  }

  public PVector mapToCam(PVector p){
    return this.mapToCam(p.x, p.y);
  }

  public PVector mapToCam(float nx, float ny){
    return this.canvasToCam(this.mapToCanvas(nx, ny));
  }
}
