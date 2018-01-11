# Ptmx

Add Tiled maps to your sketch.

(c) Caldas Lopes 2018

This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

# How to use it:

Read the examples.

# Reference:

## class Ptmx

Constructor:

### Ptmx(PApplet applet, String filename)
File must be in data folder.

Example: Ptmx map = new Ptmx(this, "desert.tmx");

## Draw methods

### void draw()
Draw map at position (0, 0), just for a quick test.
  
### void draw(PGraphics pg)
Draw map on a given PGraphics at position (0, 0).

### void draw(PVector position)
### void draw(float left, float top)
Draw map at position (left, top).
  
### void draw(PGraphics pg, PVector position)
### void draw(PGraphics pg, float left, float top)
Draw map on a given PGraphics at position (left, top).
  
### void draw(int layer, PVector position)
### void draw(int layer, float left, float top)
Draw a specific layer at position (left, top).

### void draw(PGraphics pg, int layer, PVector position)
### void draw(PGraphics pg, int layer, float left, float top)
Draw a specific layer on a given PGraphics at position (left, top).


## Layers methods
All this methods need a layer index

### String getType(int layer)
Return "layer", "imagelayer" or "objectgroup".

### boolean getVisible(int layer)
### void setVisible(int layer, boolean visible)
Visible property is ignored when drawing individual layers.

### PImage getImage(int layer)
Return null if layer is not a image layer or index is out of bounds

### StringDict[] getObjects(int layer)
Return null if layer is not a object layer or index is out of bounds

### int getObjectsColor(int layer)
Return black if not defined, layer is not a image layer or index is out of bounds

### int[] getData(int layer)
Return null if layer is not a tile layer or index is out of bounds
Attention: Tiled store tiles index as index + 1, so 0 is a "no tile". This is internal data, and looks different than what you get if you use set/getTileIndex.

### StringDict getCustomProperties(int layer)

### float getOpacity(int layer)
### void setOpacity(int layer, float opacity)
Opacity is used when drawing individual layers (and on toImage method).

### int getTileIndex(int layer, int x, int y)
### void setTileIndex(int layer, int x, int y, int value)
Tile indexes are the same you see on Tiled.
-1 for "no tile".
Return -2 if coordinates are out of bounds.

### void toImage(int layer)
Turn a object or tile layer into a image layer. May be useful if you don't need tile or object info, and want a specific opacity on a layer. Opacity is used when creating the image. The created image is truncated to map limits.
  
## Map methods

### String getFilename()

### float getVersion()

### int getBackgroundColor()

### PVector getMapSize()
Return dimension as number of tiles

### PVector getTileSize()

### int getHexSideLength()
Used in hexagonal maps.

### PVector getCamCorner()

### PVector getCamCenter()

### PVector getCamSize()

### void setCamSize(PVector s)
### void setCamSize(int w, int h)
Only useful if you want to do some pre-draw calculations, since size is always the size of the last PGraphics (or window).

### int getDrawMargin()
### void setDrawMargin(int n)
Default 2. Increase it if tiles are big.

### int getDrawMode()
### void setDrawMode(int mode)
CORNER or CENTER. Default: CORNER.
Affect draw coordinates. Draw position corresponds to left-top corner or graphics/window center.

### String getPositionMode
### setPositionMode(String mode)
"CANVAS" or "MAP". Default: "CANVAS".
Draw coordinates can be relative to the image created when drawing tiles and objects ("CANVAS"). Think as distance in pixels from the left-top corner.
In "MAP" PositionMode, coordinates are relative to tiles, so (5, 4) points to the 5th tile from the left, 4th tile from top. Diferences are notorious and useful when using isometric and hexagonal maps.

### String getBackgroundMode
### setBackgroundMode(String mode)
"COLOR", "CLEAR" or "NONE". Default: "COLOR".
Define what happens to the background before draw.
"COLOR" - Draw Map background color.
"CLEAR" - Drawing to Canvas: Same as "COLOR". Drawing to PGraphics: Clear PGraphics (transparent).
"NONE" - Do nothing.

### PVector getPosition()
Return last draw position. Dependent of DrawMode and PositionMode

## Conversion of Coordinates methods

### PVector canvasToMap(PVector p)
### PVector canvasToMap(float x, float y)

### PVector mapToCanvas(PVector p)
### PVector mapToCanvas(float nx, float ny)

### PVector camToCanvas(PVector p)
### PVector camToCanvas(float x, float y)

### PVector canvasToCam(PVector p)
### PVector canvasToCam(float x, float y)

### PVector camToMap(PVector p)
### PVector camToMap(float x, float y)

### PVector mapToCam(PVector p)
### PVector mapToCam(float nx, float ny)

