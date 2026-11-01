"""Slice original generated emblem masters and compose native Minecraft item textures.
Source masters are shipped so the exact texture build is reproducible offline.
"""
from pathlib import Path
from PIL import Image,ImageDraw
from matte_finish import matte
p=Path(__file__).resolve().parents[1]
a=p/'tools/art';base=p/'src/main/resources/assets/advancedapiary/textures/items'
names=['speed','productivity','specialty','parallel','export','focus','sorter','heater','cooler','humidifier','dryer','rain','light','dark','sky','climate']
if not (a/'module-emblems.png').exists() or not (a/'garden-emblem.png').exists():
 raise RuntimeError('Missing emblem source masters in tools/art')
sheet=Image.open(a/'module-emblems.png').convert('RGBA')
for i,name in enumerate(names+['garden']):
 if name=='garden':icon=Image.open(a/'garden-emblem.png').convert('RGBA')
 else:
  w,h=sheet.size;x=i%4;y=i//4;icon=sheet.crop((x*w//4,y*h//4,(x+1)*w//4,(y+1)*h//4))
 icon=matte(icon.resize((17,17),Image.Resampling.LANCZOS),24).resize((34,34),Image.Resampling.NEAREST)
 # Shared shaded chassis is an original asset, composited into a normal item PNG.
 t=Image.new('RGBA',(64,64));ImageDraw.Draw(t).rectangle((13,13,50,46),fill='#83785a')
 t.alpha_composite(Image.open(a/'module-chassis-matte.png').convert('RGBA').resize((32,32),Image.Resampling.LANCZOS).resize((64,64),Image.Resampling.NEAREST))
 t.alpha_composite(icon,(15,13))
 t.save(base/('module_'+name+'.png'))
preview=Image.new('RGB',(680,3*120),'#292725');d=ImageDraw.Draw(preview)
for i,name in enumerate(names+['garden']):
 x=(i%6)*112;y=(i//6)*120;tex=Image.open(base/('module_'+name+'.png')).resize((96,96),Image.Resampling.NEAREST);preview.paste(tex,(x+8,y+2),tex);d.text((x+8,y+101),name,fill='#e4ce9b')
preview.save(p/'modules-preview.png')
# Refresh the installed-garden GUI example from the actual final item texture.
f=p/'gui-preview-unlocked.png'
if f.exists():
 opened=Image.open(f).convert('RGBA')
 atlas=Image.open(p/'src/main/resources/assets/advancedapiary/textures/gui/advanced_apiary.png').convert('RGBA')
 unit=atlas.width//256
 opened.paste(atlas.crop((89*unit,135*unit,105*unit,151*unit)).resize((64,64),Image.Resampling.NEAREST),(89*4,135*4))
 opened.alpha_composite(Image.open(base/'module_garden.png').convert('RGBA'),(89*4,135*4));opened.save(f)
