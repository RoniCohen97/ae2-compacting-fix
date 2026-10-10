"""Draws logo.png: a compacting drawer (block, ingot, nugget) in a fluix-coloured frame, with a check badge.

Pixel art on a 40x40 grid, scaled 10x to 400x400 (CurseForge and Modrinth want at least 400x400).
Run from the project root: python tools/logo.py
"""
from PIL import Image

GRID = 40
SCALE = 10

BACKGROUND = (33, 38, 46)
WOOD_DARK = (74, 50, 28)
WOOD = (128, 88, 50)
WOOD_LIGHT = (168, 120, 70)
SLOT = (36, 25, 16)
SLOT_EDGE = (56, 39, 24)
IRON_DARK = (120, 124, 130)
IRON = (196, 200, 206)
IRON_LIGHT = (236, 238, 240)
FLUIX_PURPLE = (132, 82, 214)
FLUIX_CYAN = (96, 214, 230)
FLUIX_PURPLE_DARK = (78, 44, 140)
BADGE_DARK = (40, 120, 56)
BADGE = (74, 178, 92)
WHITE = (245, 248, 245)

img = Image.new('RGBA', (GRID, GRID), BACKGROUND + (255,))
px = img.load()


def rect(x0, y0, x1, y1, color):
    """Fills the inclusive rectangle."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = color + (255,) if len(color) == 3 else color


def frame(x0, y0, x1, y1, color):
    """Draws the inclusive rectangle's outline."""
    for x in range(x0, x1 + 1):
        px[x, y0] = color + (255,)
        px[x, y1] = color + (255,)
    for y in range(y0, y1 + 1):
        px[x0, y] = color + (255,)
        px[x1, y] = color + (255,)


# Fluix frame: a purple ring with a cyan glow inside
frame(2, 2, 37, 37, FLUIX_PURPLE_DARK)
frame(3, 3, 36, 36, FLUIX_PURPLE)
frame(4, 4, 35, 35, FLUIX_CYAN)

# Drawer body: wood with a lit top-left edge and a dark bottom-right edge
rect(6, 6, 33, 33, WOOD)
for i in range(6, 34):
    px[i, 6] = WOOD_LIGHT + (255,)
    px[6, i] = WOOD_LIGHT + (255,)
    px[i, 33] = WOOD_DARK + (255,)
    px[33, i] = WOOD_DARK + (255,)


def slot(x0, y0, x1, y1):
    rect(x0, y0, x1, y1, SLOT)
    frame(x0 - 1, y0 - 1, x1 + 1, y1 + 1, WOOD_DARK)
    for x in range(x0, x1 + 1):
        px[x, y1] = SLOT_EDGE + (255,)


# Compacting drawer layout: one wide slot on top, two below
slot(9, 9, 30, 19)
slot(9, 23, 18, 30)
slot(21, 23, 30, 30)

# Top slot: iron block (9x9), lit top-left, shaded bottom-right
rect(15, 10, 23, 18, IRON)
frame(15, 10, 23, 18, IRON_DARK)
for i in range(16, 23):
    px[i, 11] = IRON_LIGHT + (255,)
for i in range(11, 18):
    px[16, i] = IRON_LIGHT + (255,)
for x, y in [(19, 14), (20, 15), (18, 15), (19, 16)]:
    px[x, y] = IRON_DARK + (255,)

# Bottom left: iron ingot, a slanted bar
for row, (start, end) in enumerate([(12, 16), (11, 16), (10, 16), (10, 15)]):
    y = 25 + row
    for x in range(start, end + 1):
        px[x, y] = IRON + (255,)
for x in range(12, 17):
    px[x, 25] = IRON_LIGHT + (255,)
px[11, 26] = IRON_LIGHT + (255,)
for x in range(10, 16):
    px[x, 28] = IRON_DARK + (255,)
px[16, 27] = IRON_DARK + (255,)
px[16, 26] = IRON_DARK + (255,)

# Bottom right: iron nugget, a small blob
for x, y, c in [(25, 25, IRON_LIGHT), (26, 25, IRON), (24, 26, IRON_LIGHT), (25, 26, IRON), (26, 26, IRON),
                (27, 26, IRON_DARK), (24, 27, IRON), (25, 27, IRON), (26, 27, IRON_DARK), (25, 28, IRON_DARK)]:
    px[x, y] = c + (255,)

# Check badge, bottom right, overlapping the frame
cx, cy, r = 32, 32, 4.6
for y in range(cy - 5, cy + 6):
    for x in range(cx - 5, cx + 6):
        d = (x - cx) ** 2 + (y - cy) ** 2
        if d <= r * r:
            px[x, y] = (BADGE_DARK if d > (r - 1.2) ** 2 else BADGE) + (255,)
# A check mark: short stroke down-right, long stroke up-right, two pixels thick
for x, y in [(29, 32), (30, 33), (31, 34), (32, 33), (33, 32), (34, 31), (35, 30)]:
    px[x, y] = WHITE + (255,)
    if x < 35:
        px[x + 1, y] = WHITE + (255,)

img.resize((GRID * SCALE, GRID * SCALE), Image.NEAREST).save('logo.png')
print('logo.png written')
