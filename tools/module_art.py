"""Slice original generated emblem masters and compose native Minecraft item textures.
Source masters are shipped so the exact texture build is reproducible offline.
"""
from pathlib import Path
from PIL import Image,ImageDraw
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
 icon=icon.resize((40,40),Image.Resampling.LANCZOS).quantize(colors=48,method=Image.Quantize.FASTOCTREE).convert('RGBA')
 # A 64px native item atlas retains small mechanism detail at larger GUI scales.
 t=Image.new('RGBA',(64,64));d=ImageDraw.Draw(t)
 d.polygon([(12,2),(51,2),(60,12),(60,48),(50,58),(12,58),(2,48),(2,12)],fill='#30281f')
 d.rounded_rectangle((6,4,57,55),8,fill='#987444',outline='#f0d39a',width=2)
 d.line((12,6,50,6),fill='#fff0be',width=2);d.line((8,50,54,50),fill='#594529',width=2)
 d.rectangle((10,9,53,51),fill='#172b29',outline='#4d6256',width=2)
 t.alpha_composite(icon,(12,11));d=ImageDraw.Draw(t)
 for x in [8,54]:
  for y in [10,46]:d.ellipse((x-1,y-1,x+2,y+2),fill='#e6c989');d.line((x,y+1,x+1,y),fill='#59452d')
 for x in range(16,50,6):d.rectangle((x,56,x+3,63),fill='#deb965');d.line((x,56,x,61),fill='#fff0a8')
 d.rectangle((18,8,44,9),fill='#f6dfaa');d.rectangle((14,52,48,54),fill='#534d3b')
 t.save(base/('module_'+name+'.png'))
preview=Image.new('RGB',(680,3*120),'#292725');d=ImageDraw.Draw(preview)
for i,name in enumerate(names+['garden']):
 x=(i%6)*112;y=(i//6)*120;tex=Image.open(base/('module_'+name+'.png')).resize((96,96),Image.Resampling.NEAREST);preview.paste(tex,(x+8,y+2),tex);d.text((x+8,y+101),name,fill='#e4ce9b')
preview.save(p/'modules-preview.png')
# Refresh the installed-garden GUI example from the actual final item texture.
f=p/'gui-preview-unlocked.png'
if f.exists():
 opened=Image.open(f).convert('RGBA');opened.alpha_composite(Image.open(base/'module_garden.png').convert('RGBA'),(89*4,135*4));opened.save(f)
