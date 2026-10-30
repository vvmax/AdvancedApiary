"""Original brass cartridges and carved hive textures; no borrowed mod assets."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageEnhance
p=Path(__file__).resolve().parents[1];base=p/'src/main/resources/assets/advancedapiary/textures'
# Retain the semantic glyphs, rebuild the cartridge casing at 32px.
for f in sorted((base/'items').glob('module_*.png')):
 old=Image.open(f).convert('RGBA');icon=old.crop((3,3,13,13)).resize((14,14),Image.Resampling.NEAREST)
 t=Image.new('RGBA',(32,32));d=ImageDraw.Draw(t)
 d.polygon([(6,1),(25,1),(30,6),(30,24),(25,29),(6,29),(1,24),(1,6)],fill='#30281f')
 d.rounded_rectangle((3,2,28,27),4,fill='#987444',outline='#f0d39a',width=1)
 d.line((6,3,25,3),fill='#fff0be');d.line((4,25,27,25),fill='#594529')
 d.rectangle((6,5,25,24),fill='#172b29',outline='#4d6256');d.line((7,8,7,21,10,21),fill='#6b8562');d.line((24,9,24,22,21,22),fill='#6b8562');t.alpha_composite(icon,(9,8))
 d=ImageDraw.Draw(t)
 for x in [4,27]:
  for y in [5,23]:d.rectangle((x,y,x+1,y+1),fill='#e6c989');d.point((x+1,y+1),fill='#59452d')
 for x in range(8,25,3):d.rectangle((x,28,x+1,31),fill='#deb965');d.point((x,28),fill='#fff0a8')
 d.rectangle((9,4,22,4),fill='#f6dfaa');d.rectangle((7,25,24,26),fill='#534d3b')
 t.save(f)
for name in ['front','side','top']:
 t=Image.new('RGBA',(32,32),'#714d32');d=ImageDraw.Draw(t)
 for y in range(32):
  for x in range(32):
   v=((x*17+y*7+(y//5)*11)%9)-4;t.putpixel((x,y),(116+v,79+v,49+v,255))
 for y in [1,15,30]:d.line((0,y,31,y),fill='#443629');d.line((0,y+1,31,y+1),fill='#b28a52')
 for x in [1,28]:d.rectangle((x,0,x+2,31),fill='#ba9254');d.line((x,0,x,31),fill='#f0d397')
 if name=='front':
  for y in [6,19]:
   for x in [5,13,21]:
    d.rounded_rectangle((x-1,y-1,x+6,y+8),2,fill='#d1ad66');d.rectangle((x,y,x+5,y+6),fill='#292e27');d.line((x,y,x+5,y),fill='#675d3c');d.rectangle((x+1,y+2,x+4,y+4),fill='#b48337');d.point((x+1,y+2),fill='#f5d774')
 elif name=='side':
  d.rounded_rectangle((5,5,26,26),3,fill='#b18b50',outline='#efd29a');d.rectangle((7,7,24,24),fill='#313a30')
  for y in range(9,24,3):d.line((9,y,22,y),fill='#927c4c');d.line((9,y+1,22,y+1),fill='#192820')
 else:
  d.rounded_rectangle((6,5,25,26),4,fill='#d6b46d',outline='#f3d9a1');d.rounded_rectangle((8,7,23,24),3,fill='#4e5840')
  for y in range(10,23,5):
   for x in range(11,23,6):d.regular_polygon((x,y,3),6,fill='#ae873e',outline='#e8c879')
 for x in [2,29]:
  for y in [2,29]:d.point((x,y),fill='#fff0ba');d.point((x,y+1),fill='#5b492c')
 t.save(base/'blocks'/('apiary_'+name+'.png'))
# Replace placeholder symbols with detailed emblem assets.
import module_art
# Contact sheet of final actual resources, at nearest-neighbour scale.
files=list(sorted((base/'items').glob('module_*.png')))+list(sorted((base/'blocks').glob('*.png')))
out=Image.new('RGB',(640,((len(files)+7)//8)*96),'#292725');d=ImageDraw.Draw(out)
for i,f in enumerate(files):
 x=(i%8)*80;y=(i//8)*96;tex=Image.open(f).convert('RGBA').resize((64,64),Image.Resampling.NEAREST);out.paste(tex,(x+8,y+4),tex);d.text((x+2,y+72),f.stem.replace('module_','').replace('apiary_',''),fill='#e4ce9b')
out.save(p/'assets-preview.png')
